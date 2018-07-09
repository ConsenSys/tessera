package com.github.tessera.node;

import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import com.github.tessera.key.KeyManager;
import com.github.tessera.key.exception.KeyNotFoundException;
import com.github.tessera.nacl.Key;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class PartyInfoServiceImpl implements PartyInfoService {

    private final PartyInfoStore partyInfoStore;


    public PartyInfoServiceImpl(final PartyInfoStore partyInfoStore,
                                final Config configuration,
                                final KeyManager keyManager) {

        this.partyInfoStore = Objects.requireNonNull(partyInfoStore);

        Objects.requireNonNull(keyManager);

        final String advertisedUrl = configuration.getServerConfig().getServerUri().toString();

        final Set<Party> parties = configuration.getPeers()
            .stream()
            .map(Peer::getUrl)
            .map(Party::new)
            .collect(Collectors.toSet());

        partyInfoStore.store(new PartyInfo(advertisedUrl, emptySet(), parties));

        registerPublicKeys(advertisedUrl, keyManager.getPublicKeys());

    }

    @Override
    public void registerPublicKeys(final String ourUrl, final Set<Key> publicKeys) {

        final Set<Recipient> ourKeys = publicKeys.stream()
            .map(key -> new Recipient(key, ourUrl))
            .collect(Collectors.toSet());

        final PartyInfo selfPartyInfo = new PartyInfo(ourUrl, ourKeys, emptySet());
        partyInfoStore.store(selfPartyInfo);
    }

    @Override
    public PartyInfo getPartyInfo() {
        return partyInfoStore.getPartyInfo();
    }

    @Override
    public PartyInfo updatePartyInfo(final PartyInfo partyInfo) {

        partyInfoStore.store(partyInfo);

        return partyInfoStore.getPartyInfo();
    }

    @Override
    public String getURLFromRecipientKey(Key key) {
        Recipient retrievedRecipientFromStore = partyInfoStore.getPartyInfo().getRecipients()
            .stream()
            .filter(recipient -> key.equals(recipient.getKey()))
            .findAny()
            .orElseThrow(() -> new KeyNotFoundException("Recipient not found"));
        return retrievedRecipientFromStore.getUrl();
    }
}
