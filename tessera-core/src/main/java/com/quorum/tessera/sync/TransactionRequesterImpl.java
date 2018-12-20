package com.quorum.tessera.sync;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;

public class TransactionRequesterImpl implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequesterImpl.class);

    private final Enclave enclave;

    private final P2pClient client;

    public TransactionRequesterImpl(final Enclave enclave, final P2pClient client) {
        this.enclave = Objects.requireNonNull(enclave);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public boolean requestAllTransactionsFromNode(final URI target) {

        LOGGER.debug("Requesting transactions get resent for {}", target);

        return this.enclave
            .getPublicKeys()
            .stream()
            .map(this::createRequestAllEntity)
            .allMatch(req -> this.makeRequest(target, req));

    }

    /**
     * Will make the desired request until succeeds or max tries has been
     * reached
     *
     * @param target  the URI to call
     * @param request the request object to send
     */
    private boolean makeRequest(final URI target, final ResendRequest request) {
        LOGGER.debug("Requesting a resend for key {}", request.getPublicKey());

        boolean success;
        int numberOfTries = 0;

        do {

            try {
                success = client.makeResendRequest(target, request);
            } catch (final Exception ex) {
                success = false;
                LOGGER.debug("Failed to make resend request to node {} for key {}", target, request.getPublicKey());
            }

            numberOfTries++;

        } while (!success && (numberOfTries < MAX_ATTEMPTS));

        return success;

    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private ResendRequest createRequestAllEntity(final PublicKey key) {

        final ResendRequest request = new ResendRequest();
        request.setPublicKey(key.encodeToBase64());
        request.setType(ResendRequestType.ALL);

        return request;
    }

}
