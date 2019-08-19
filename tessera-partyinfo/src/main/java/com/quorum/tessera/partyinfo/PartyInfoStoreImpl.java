package com.quorum.tessera.partyinfo;

import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.encryption.PublicKey;
import java.net.URI;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Stores a list of all discovered nodes and public keys */
public enum PartyInfoStoreImpl implements PartyInfoStore {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoStoreImpl.class);

    private String advertisedUrl;

    private final Map<PublicKey, Recipient> recipients = new HashMap<>();
    // new HashMap<>();

    private final Set<Party> parties = new HashSet<>();

    @Override
    public PartyInfoStore init(URI advertisedUrl) {
        this.clear();
        this.advertisedUrl = advertisedUrl.toString();
        this.parties.add(new Party(this.advertisedUrl));
        return this;
    }

    /**
     * Merge an incoming {@link PartyInfo} into the current one, adding any new keys or parties to the current store
     *
     * @param newInfo the incoming information that may contain new nodes/keys
     */
    @Override
    public synchronized void store(final PartyInfo newInfo) {

        for (Recipient recipient : newInfo.getRecipients()) {
            PublicKey key = recipient.getKey();
            LOGGER.debug("Storing key {}", key);
            recipients.put(key, recipient);
        }

        parties.addAll(newInfo.getParties());

        // update the sender to have been seen recently
        final Party sender = new Party(newInfo.getUrl());
        sender.setLastContacted(Instant.now());
        parties.remove(sender);
        parties.add(sender);
    }

    /**
     * Fetch a copy of all the currently discovered nodes/keys
     *
     * @return an immutable copy of the current state of the store
     */
    @Override
    public synchronized PartyInfo getPartyInfo() {
        return new PartyInfo(
                advertisedUrl,
                unmodifiableSet(new HashSet<>(recipients.values())),
                unmodifiableSet(new HashSet<>(parties)));
    }

    @Override
    public synchronized PartyInfo removeRecipient(String uri) {
        Optional<PublicKey> key =
                recipients.entrySet().stream()
                        .filter(e -> uri.startsWith(e.getValue().getUrl()))
                        .map(e -> e.getKey())
                        .findFirst();

        key.ifPresent(recipients::remove);

        return getPartyInfo();
    }

    @Override
    public Recipient findRecipientByPublicKey(PublicKey key) {
        LOGGER.debug("Find key {}",key);
        Optional<Recipient> recipient = Optional.ofNullable(recipients.get(key));

        if (!recipient.isPresent()) {
            LOGGER.warn("No recipient found for key {}", key);
        }

        return recipient.orElseThrow(() -> new KeyNotFoundException(key.encodeToBase64() + " not found"));
    }

    @Override
    public synchronized void clear() {
        recipients.clear();
        parties.clear();
    }
}
