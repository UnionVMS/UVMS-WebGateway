package eu.europa.ec.fisheries.uvms.webGateway;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Path("sse")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class SSEResource {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResource.class);

    private Sse sse;
    private OutboundSseEvent.Builder eventBuilder;
    private ConcurrentLinkedQueue<UserSseEventSink> userSinks = new ConcurrentLinkedQueue<>();

    @Resource
    private ManagedExecutorService executor;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
    }

    public void sendSSEEvent(String data, String eventName, List<String> subscriberList, MovementSourceType movementSource) {
        try {
            if (data == null || eventName == null) {
                LOG.error("Not going to send an sse event with a null payload/eventname");
                return;
            }
            OutboundSseEvent sseEvent = createSseEvent(data, eventName);

            userSinks.forEach(userSink -> {
                if (userSink.getEventSink().isClosed()) {
                    LOG.debug("Removing user " + userSink.getUser() + " from sse stream");
                    userSink.getEventSink().close();
                    userSinks.remove(userSink);

                }else {
                    for (String subscription : subscriberList) {
                        if ((Constants.ALL.equals(subscription) || userSink.getUser().equals(subscription)) &&
                                (movementSource == null || userSink.getSources().stream().anyMatch(source -> source.equals(movementSource)))) {
                            LOG.debug("Broadcasting to {}", userSink.getUser());
                            try {
                                Callable<Object> task = () -> {
                                    userSink.getEventSink().send(sseEvent).whenComplete((object, error) -> {
                                        if (error != null) {
                                            LOG.error("Removing user " + userSink.getUser() + " from sse stream due to error: " + error.getMessage());
                                            userSinks.remove(userSink);
                                        }
                                    });
                                    return true;
                                };
                                Future<Object> future = executor.submit(task);
                                try {
                                    Object result = future.get(2, TimeUnit.SECONDS);
                                } catch (Exception ex) {
                                    future.cancel(true);
                                    LOG.error("Removing user " + userSink.getUser() + " from sse stream due to being unable to send updates within one second");
                                    userSink.getEventSink().close();
                                    userSinks.remove(userSink);
                                }
                            } catch (IllegalStateException e) {
                                if (e.getMessage().contains("SseEventSink is closed")) {
                                    LOG.debug("Removing user " + userSink.getUser() + " from sse stream due to closed stream");
                                    userSinks.remove(userSink);
                                } else {
                                    throw new IllegalStateException(e);
                                }
                            }
                        }
                    }
                }
            });
            LOG.debug("userSinks size: {}", userSinks.size());
        }catch (Exception e){
            LOG.error("Error while broadcasting SSE: ", e);
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribe(@Context SseEventSink sseEventSink, @Context SecurityContext securityContext, @QueryParam("sources") List<String> sources) {
        List<MovementSourceType> sourceTypes = convertToMovementSourceTypes(sources);
        sseEventSink.send(sse.newEvent("UVMS SSE Ticket notifications"));
        String user = securityContext.getUserPrincipal().getName();
        userSinks.add(new UserSseEventSink(user, sseEventSink, sourceTypes));
        sseEventSink.send(sse.newEvent("User " + user + " is now registered"));
        LOG.info("User " + user + " is now registered");
    }

    private List<MovementSourceType> convertToMovementSourceTypes (List<String> sources) {
        List<MovementSourceType> sourceTypes = new ArrayList<>();
        if (sources == null || sources.isEmpty()) {
            sourceTypes = Arrays.asList(MovementSourceType.values());
        } else {
            for (String source : sources) {
                sourceTypes.add(MovementSourceType.fromValue(source));
            }
        }
        return sourceTypes;
    }

    @Gauge(unit = MetricUnits.NONE, name = "StreamCollector_current_number_of_sse_stream_subscribers", absolute = true)
    public int getCurrentNumberOfSubscribers(){
        return userSinks.size();
    }

    private OutboundSseEvent createSseEvent(String data, String eventName) {
        return eventBuilder
                .name(eventName)
                .id(String.valueOf(System.currentTimeMillis()))
                .mediaType(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                .data(String.class, data)
                .build();
    }

    private class UserSseEventSink {
        private String user;
        private List<MovementSourceType> sources;
        private SseEventSink eventSink;

        public UserSseEventSink(String user, SseEventSink sseEventSink, List<MovementSourceType> sources) {
            this.user = user;
            this.sources = sources;
            this.eventSink = sseEventSink;
        }

        public String getUser() {
            return user;
        }

        public SseEventSink getEventSink() {
            return eventSink;
        }

        public List<MovementSourceType> getSources() {return sources; }

    }
}
