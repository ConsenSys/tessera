package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 *
 *
 * Create a server listening on a Unix Domain Socket for http requests. We
 * create a connection to an HTTP server, and act as a proxy between the socket
 * and the HTTP server. TODO: should possibly support connections from multiple
 * clients
 *
 * FIXME: This object has far too many internal dependencies, making it
 * resistant to effective testing.
 */
public class SocketServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

    private final UnixDomainServerSocket serverUds;

    private HttpProxyFactory httpProxyFactory;

    private HttpProxy httpProxy;

    private final URI serverUri;

    private final ExecutorService executor;

    private final ThreadDelegate threadDelegate = ThreadDelegate.create();
    
    /**
     * Create the unix domain socket and start the listener thread.
     */
    public SocketServer(Configuration config, HttpProxyFactory httpProxyFactory,
            URI serverUri, ExecutorService executor, UnixSocketFactory unixSocketFactory) {

        Objects.requireNonNull(config);
        Objects.requireNonNull(httpProxyFactory);
        Objects.requireNonNull(serverUri);
        Objects.requireNonNull(unixSocketFactory);

        this.executor = Objects.requireNonNull(executor, "Executor service is required");
        this.httpProxyFactory = httpProxyFactory;
        this.serverUri = serverUri;

        serverUds = new UnixDomainServerSocket(unixSocketFactory);
        serverUds.create(config.workdir(), config.socket());
    }

    @PostConstruct
    public void start() {

        executor.submit(this);
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

    /**
     * Run forever, servicing requests received on the socket.
     */
    @Override
    public void run() {

        while (true) {
            //FIXME: Hiding functions in private functions just makes it 
            //harder to navigate code. 
            serveSocketRequest();
        }

    }

    /**
     * Wait for a client connection, then: - create a connection to the HTTP
     * server - read the client HTTP request and forward it to the HTTP server -
     * read the HTTP response and return it to the client Note that Quorum opens
     * a new client connection for each request.
     */
    private void serveSocketRequest() {

        LOGGER.info("Waiting for client connection on unix domain socket...");
        serverUds.connect();
        LOGGER.info("Client connection received");

        //Get a connection to the HTTP server.
        if (createHttpServerConnection()) {

            //Read the request from the socket and send it to the HTTP server
            String message = serverUds.read();
            LOGGER.info("Received message on socket: {}", message);
            httpProxy.sendRequest(new String(message));

            //Return the HTTP response to the socket
            String response = httpProxy.getResponse();
            LOGGER.info("Received http response: {}", response);
            serverUds.write(response);

            httpProxy.disconnect();
        }

    }

    /**
     * Get a connection to the HTTP Server.
     */
    //FIXME: 
    private boolean createHttpServerConnection() {

        httpProxy = httpProxyFactory.create(serverUri);

        // TODO: add configurable number of attempts, instead of looping forever
        boolean connected = false;
        while (!connected) {
            LOGGER.info("Attempting connection to HTTP server...");
            connected = httpProxy.connect();
            try {
                threadDelegate.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.info("Interrupted - exiting");
                return false;
            }
        }
        LOGGER.info("Connected to HTTP server");

        return true;
    }
    



}
