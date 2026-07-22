package com.rupesh.ems.logging;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;

@Provider
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String REQUEST_ID = "requestId";

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    responseContext.getHeaders().add(REQUEST_ID_HEADER, MDC.get(REQUEST_ID));
    MDC.clear();
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }
    MDC.put(REQUEST_ID, requestId);
  }
}
