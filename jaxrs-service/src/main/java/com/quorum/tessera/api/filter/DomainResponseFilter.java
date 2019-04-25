package com.quorum.tessera.api.filter;

import java.io.IOException;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
/*
https://docs.oracle.com/javaee/7/api/javax/ws/rs/NameBinding.html
 */
@DomainFilter
public class DomainResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if ("unixsocket".equals(requestContext.getUriInfo().getBaseUri().toString())) {
            return;
        }

        final String origin = requestContext.getHeaderString("Origin");

        if (Objects.nonNull(origin) && !Objects.equals(origin, "")) {
            MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Headers", 
                requestContext.getHeaderString("Access-Control-Request-Headers"));
        }

    }

}
