
package com.github.nexus.api;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
https://docs.oracle.com/javaee/7/api/javax/ws/rs/NameBinding.html
*/
@Logged
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter  {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
        
        LOGGER.debug("Log around : {}",crc);
    }

    @Override
    public void filter(ContainerRequestContext crc, ContainerResponseContext crc1) throws IOException {
        LOGGER.debug("Log around : {} : {}",crc,crc1);
    }
    
}
