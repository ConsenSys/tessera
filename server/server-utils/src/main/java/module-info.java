module tessera.server.server.utils.main {
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.unixsocket;
    requires org.eclipse.jetty.util;
    requires tessera.config.main;
    requires tessera.security.main;

    exports com.jpmorgan.quorum.server.utils;
}
