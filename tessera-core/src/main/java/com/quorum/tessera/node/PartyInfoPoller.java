package com.quorum.tessera.node;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.URI;
import java.util.Objects;

/**
 * Polls every so often to all known nodes for any new discoverable nodes This
 * keeps all nodes up-to date and discoverable by other nodes
 */
public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final PartyInfoParser partyInfoParser;

    private final P2pClient p2pClient;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final PartyInfoParser partyInfoParser,
                           final P2pClient p2pClient) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.p2pClient = Objects.requireNonNull(p2pClient);
    }

    /**
     * Iterates over all known parties and contacts them for the current state
     * of their known node discovery list
     * <p>
     * It then updates this nodes list of data with any new information
     * collected
     */
    @Override
    public void run() {
        LOGGER.debug("Polling {}", getClass().getSimpleName());

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();

        final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

        partyInfo
            .getParties()
            .stream()
            .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
            .map(Party::getUrl)
            .map(URI::create)
            .map(url -> pollSingleParty(url, encodedPartyInfo))
            .filter(Objects::nonNull)
            .map(partyInfoParser::from)
            .forEach(partyInfoService::updatePartyInfo);

        LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
    }

    /**
     * Sends a request for node information to a single target If it cannot
     * connect to the target, it returns null, otherwise throws any exception
     * that can be thrown from {@link javax.ws.rs.client.Client}
     *
     * @param target           the target URL to call
     * @param encodedPartyInfo the encoded current party information
     * @return the encoded partyinfo from the target node, or null is the node
     * could not be reached
     */
    private byte[] pollSingleParty(final URI target, final byte[] encodedPartyInfo) {

        try {
            return p2pClient.getPartyInfo(target, encodedPartyInfo);
        } catch (final Exception ex) {

            if (ConnectException.class.isInstance(ex.getCause())) {
                LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), target);
                LOGGER.debug(null, ex);
                return null;
            } else {
                LOGGER.error("Error thrown while executing poller. ", ex);
                throw ex;
            }

        }

    }

}
