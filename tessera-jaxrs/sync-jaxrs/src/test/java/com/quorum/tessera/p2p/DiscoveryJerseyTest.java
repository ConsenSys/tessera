package com.quorum.tessera.p2p;

import com.quorum.tessera.discovery.Discovery;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DiscoveryJerseyTest {

  private JerseyTest jersey;

  private Discovery discovery;

  private HttpServletRequest httpServletRequest;

  @Before
  public void beforeTest() throws Exception {
    httpServletRequest = mock(HttpServletRequest.class);
    discovery = mock(Discovery.class);
    jersey =
        new JerseyTest() {

          @Override
          protected Application configure() {
            return new ResourceConfig(DiscoveryResource.class)
              .register(
                    new AbstractBinder() {
                      @Override
                      protected void configure() {
                        bind(discovery).to(Discovery.class);
                        bind(httpServletRequest).to(HttpServletRequest.class);
                      }
                    })
                .register(JsonProcessingFeature.class);
          }
        };

    jersey.setUp();
  }

  @After
  public void afterTest() throws Exception {
    try {
      verifyNoMoreInteractions(discovery);
    } finally {
      jersey.tearDown();
    }
  }

  @Test
  public void destroyClosesEventBroadcaster() {

    DiscoveryResource discoveryResource = new DiscoveryResource(discovery);
    Sse sse = mock(Sse.class);
    SseBroadcaster sseBroadcaster = mock(SseBroadcaster.class);
    when(sse.newBroadcaster()).thenReturn(sseBroadcaster);

    discoveryResource.setSse(sse);

    discoveryResource.onDestroy();

    verify(sseBroadcaster).close();
    verify(sse).newBroadcaster();
    verifyNoMoreInteractions(sseBroadcaster, sse);
  }

  @Test
  public void doStuff() throws Exception {

    CountDownLatch countDownLatch = new CountDownLatch(1);
    when(httpServletRequest.getRemoteAddr())
      .thenReturn("http://junit:92892");
    WebTarget target = jersey.target("discovery");
    List<InboundSseEvent> responses = new ArrayList<>();
    try (SseEventSource eventSource = SseEventSource.target(target).build()) {
      eventSource.register(
          (inboundSseEvent) -> {
            responses.add(inboundSseEvent);
            countDownLatch.countDown();
          });
      eventSource.open();

      assertThat(countDownLatch.await(20, TimeUnit.SECONDS)).isTrue();

      assertThat(responses).hasSize(1);
      InboundSseEvent event = responses.get(0);
      JsonObject response = event.readData(JsonObject.class, MediaType.APPLICATION_JSON_TYPE);

      assertThat(response.getString("message")).isEqualTo("Hellow");
      assertThat(response.getString("when")).isNotBlank().isNotNull();
    }
  }


}
