package com.github.nexus.socket;

import com.github.nexus.config.Config;

import java.util.concurrent.Executors;

public interface SocketServerFactory {

    static SocketServer createSocketServer(final Config config) {
        try {
            return new SocketServer(
                config.getUnixSocketFile(),
                new HttpProxyFactory(config.getServerConfig()),
                Executors.newCachedThreadPool(),
                UnixSocketFactory.create()
            );
        } catch (final Exception ex) {
            throw new NexusSocketException(ex);
        }
    }

}
