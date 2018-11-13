package com.quorum.tessera.node;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;

    private final ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final ConfigService configService,
                                final Enclave enclave) {
        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);
        this.configService = Objects.requireNonNull(configService);

        final String advertisedUrl = configService.getServerUri() + "/";

        final Set<Recipient> ourKeys = enclave
            .getPublicKeys()
            .stream()
            .map(key -> PublicKey.from(key.getKeyBytes()))
            .map(key -> new Recipient(key, advertisedUrl))
            .collect(toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, ourKeys, Collections.emptySet()));
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        if (!configService.isDisablePeerDiscovery()) {
            //auto-discovery is on, we can accept all input to us
            this.partyInfoStore.store(partyInfo);
            return partyInfoStore.getPartyInfo();
        }

        //auto-discovery is off

        final Set<String> peerUrls = configService
            .getPeers()
            .stream()
            .map(Peer::getUrl)
            .collect(Collectors.toSet());

        LOGGER.debug("Known peers: {}", peerUrls);

        //check the caller is allowed to update our party info, which it can do
        //if it one of our known peers
        final String incomingUrl = partyInfo.getUrl();

        //TODO: should we just check peer is the same or with +"/", instead of just starts with?
        if (peerUrls.stream().noneMatch(incomingUrl::startsWith)) {
            final String message = String.format("Peer %s not found in known peer list", partyInfo.getUrl());
            throw new AutoDiscoveryDisabledException(message);
        }

        //filter out all keys that aren't from that node
        final Set<Recipient> knownRecipients = partyInfo
            .getRecipients()
            .stream()
            .filter(recipient -> Objects.equals(recipient.getUrl(), incomingUrl))
            .collect(Collectors.toSet());

        // TODO NL - check if we should add the unsaved parties to the resend party store (in the same way in which we are doing it in PartyInfoPoller)

        //TODO: instead of adding the peers every time, if a new peer is added at runtime then this should be added separately
        final Set<Party> parties = peerUrls.stream().map(Party::new).collect(toSet());

        partyInfoStore.store(new PartyInfo(partyInfo.getUrl(), knownRecipients, parties));

        return this.getPartyInfo();
    }

    @Override
    public String getURLFromRecipientKey(final PublicKey key) {

        final Recipient retrievedRecipientFromStore = partyInfoStore
                .getPartyInfo()
                .getRecipients()
                .stream()
                .filter(recipient -> key.equals(recipient.getKey()))
                .findAny()
                .orElseThrow(() -> new KeyNotFoundException("Recipient not found for key: "+ key.encodeToBase64()));

        return retrievedRecipientFromStore.getUrl();
    }

    @Override
    public Set<Party> findUnsavedParties(final PartyInfo partyInfoWithUnsavedRecipients) {
        final Set<Party> knownHosts = this.getPartyInfo().getParties();

        final Set<Party> incomingRecipients = new HashSet<>(partyInfoWithUnsavedRecipients.getParties());
        incomingRecipients.removeAll(knownHosts);

        return Collections.unmodifiableSet(incomingRecipients);
    }

}
