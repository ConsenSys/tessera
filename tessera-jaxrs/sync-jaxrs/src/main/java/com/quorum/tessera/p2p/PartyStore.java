package com.quorum.tessera.p2p;

import com.quorum.tessera.context.RuntimeContext;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;

/*
 * Support legacy collation of all parties to be added to
 * party info responses so nodes learn of nodes.
 */
public interface PartyStore {

    default void loadFromConfigIfEmpty() {
        RuntimeContext runtimeContext = RuntimeContext.getInstance();

        final Set<URI> parties = getParties();

        if (parties.isEmpty() || !parties.stream().anyMatch(runtimeContext.getPeers()::contains)) {
            runtimeContext.getPeers().forEach(this::store);
        }
    }

    Set<URI> getParties();

    PartyStore store(URI party);

    PartyStore remove(URI party);

    static PartyStore getInstance() {
        return ServiceLoader.load(PartyStore.class).findFirst().get();
    }
}
