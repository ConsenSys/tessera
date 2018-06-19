package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.junixsocket.adapter.UnixSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SocketServerTest {

    private SocketServer socketServer;

    private Configuration config;

    private HttpProxyFactory httpProxyFactory;

    private URI uri;

    private ExecutorService executorService;

    private UnixSocketFactory unixSocketFactory;

    private Path socketFile;

    private ServerSocket serverSocket;

    private Socket socket;

    public SocketServerTest() {
    }

    @Before
    public void setUp() throws URISyntaxException, IOException {

        socketFile = Paths.get(System.getProperty("java.io.tmpdir"), "junit.txt");

        config = mock(Configuration.class);

        when(config.workdir())
                .thenReturn(socketFile.toFile().getParent());

        when(config.socket()).thenReturn(socketFile.toFile().getName());

        httpProxyFactory = mock(HttpProxyFactory.class);
        uri = new URI("http://bogus.com:9819");
        executorService = mock(ExecutorService.class);

        serverSocket = mock(ServerSocket.class);
        socket = mock(Socket.class);

        when(serverSocket.accept()).thenReturn(socket);
        
        unixSocketFactory = mock(UnixSocketFactory.class);

        when(unixSocketFactory.createServerSocket(socketFile)).thenReturn(serverSocket);
        
        socketServer = new SocketServer(config, httpProxyFactory,
                uri, executorService, unixSocketFactory);
    }

    @After
    public void tearDown() throws IOException {
        verifyNoMoreInteractions(httpProxyFactory, executorService);
        Files.deleteIfExists(socketFile);
    }

    @Test
    public void start() {
        socketServer.start();
        verify(executorService).submit(socketServer);

    }

    @Test
    public void stop() {
        socketServer.stop();
        verify(executorService).shutdown();

    }

    /*
    FIXME: The class itself needs refectoring to be easier to test
    */
    @Test
    public void run() throws IOException, InterruptedException {

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);

        when(httpProxyFactory.create(uri)).thenReturn(httpProxy);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());
        
        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);
        
        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE");
        
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(socketServer);
        
        TimeUnit.SECONDS.sleep(2L);
        
        
        executor.shutdown();
        
        //Reset as we dont know how many times anuything has been called
        reset(httpProxyFactory);

    }
    
    

    @Test
    public void runThrowsIOExceptionOnClientSocket() throws IOException, InterruptedException {

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(true);

        when(httpProxyFactory.create(uri)).thenReturn(httpProxy);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());
        
        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);
        
        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE");
        
        doThrow(IOException.class).when(serverSocket).accept();
        
        try {
            socketServer.run();
            failBecauseExceptionWasNotThrown(NexusSocketException.class);
        } catch(NexusSocketException ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(IOException.class);
        }

    }
    
    @Test
    public void runInterrupt() throws Exception {

        HttpProxy httpProxy = mock(HttpProxy.class);
        when(httpProxy.connect()).thenReturn(false);

        when(httpProxyFactory.create(uri)).thenReturn(httpProxy);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("SOMEDATA".getBytes());
        
        when(socket.getInputStream()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(outputStream);
        
        when(httpProxy.getResponse()).thenReturn("SOMERESPONSE");
   
        ThreadDelegate threadDelegate = mock(ThreadDelegate.class);
        doThrow(InterruptedException.class)
        .doThrow(new StopProcess())
                .when(threadDelegate).sleep(anyLong());
        
        Field field = SocketServer.class.getDeclaredField("threadDelegate");
        field.setAccessible(true);
        field.set(socketServer, threadDelegate);
        try {
            socketServer.run();
        } catch(StopProcess ex) {}
        //Reset as we dont know how many times anuything has been called
        reset(httpProxyFactory);

    }
    
 
    static class StopProcess extends RuntimeException {
    }
    
}
