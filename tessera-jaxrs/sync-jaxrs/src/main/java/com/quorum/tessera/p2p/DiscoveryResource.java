package com.quorum.tessera.p2p;

import com.quorum.tessera.discovery.Discovery;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Tag(name = "discovery")
@Path("discovery")
public class DiscoveryResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryResource.class);

  private SseBroadcaster eventBroadcaster;

  private Sse sse;

  private final Discovery discovery;

  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  @Inject
  public DiscoveryResource(Discovery discovery) {
    this.discovery = Objects.requireNonNull(discovery);
  }

  @Context
  public void setSse(Sse sse) {
    this.sse = sse;
    this.eventBroadcaster = sse.newBroadcaster();
  }

  @PostConstruct
  public void onConstruct() {
    LOGGER.info("Constructed {}", eventBroadcaster);

    executorService.scheduleAtFixedRate(() -> {

      JsonObject jsonObject = Json.createObjectBuilder()
        .add("message", "Hellow")
        .add("when", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .build();

      final OutboundSseEvent outboundSseEvent =
        sse.newEventBuilder()
          .data(JsonObject.class, jsonObject)
          .mediaType(MediaType.APPLICATION_JSON_TYPE)
          .build();

      LOGGER.info("Broadcasting {}",outboundSseEvent);
      eventBroadcaster.broadcast(outboundSseEvent);
      LOGGER.info("Broadcasted {}",outboundSseEvent);
    },2,2, TimeUnit.SECONDS);

  }

  @PreDestroy
  public void onDestroy() {
    eventBroadcaster.close();
    executorService.shutdown();
  }

  @GET
  @Consumes(MediaType.SERVER_SENT_EVENTS)
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void onConnect(
    @Context SseEventSink sseEventSink,
    @Context HttpServletRequest httpServletRequest
    ) {
    LOGGER.info("onConnect({})", httpServletRequest.getRemoteAddr());
//    NodeUri clientUri = NodeUri.create(httpServletRequest.getRemoteAddr());
//    NodeInfo currentNodeInfo = discovery.getCurrent();
//    currentNodeInfo.getRecipients().stream()
//      .map(Recipient::getUrl)
//      .map(NodeUri::create)
//      .filter(not(clientUri::equals))
//      .findAny()
//      .ifPresent(nodeUri -> {
//
//        Set<Recipient> recipients = new HashSet<>(currentNodeInfo.getRecipients());
//
//        NodeInfo updated = NodeInfo.Builder.create()
//          .withRecipients(recipients)
//          .build();
//
//        discovery.onUpdate(updated);
//      });

    this.eventBroadcaster.register(sseEventSink);
  }
}
