package com.github.tessera.server;

import com.github.tessera.config.ServerConfig;
import javax.ws.rs.core.Application;
import java.net.URI;

public class JerseyServerFactory implements RestServerFactory{

    @Override
    public RestServer createServer(URI uri, Application aplctn, ServerConfig serverConfig) {
        return new JerseyServer(uri, aplctn, serverConfig);
    }
    
}
