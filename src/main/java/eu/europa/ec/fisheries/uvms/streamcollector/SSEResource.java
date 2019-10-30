package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApplicationScoped
@Path("sse")
@RequiresFeature(UnionVMSFeature.viewMovements)
public class SSEResource {

    private final static Logger LOG = LoggerFactory.getLogger(SSEResource.class);


    private Sse sse;
    private OutboundSseEvent.Builder eventBuilder;
    private ConcurrentLinkedQueue<UserSseEventSink> userSinks = new ConcurrentLinkedQueue<>();

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
    }

    public void sendSSEEvent(String data, String eventName, List<String> subscriberList) {
        try {
            if (data == null || eventName == null) {
                LOG.error("Not going to send an sse event with a null payload/eventname");
                return;
            }
            OutboundSseEvent sseEvent = createSseEvent(data, eventName);

            userSinks.stream().forEach(userSink -> {
                if (userSink.getEventSink().isClosed()) {
                    userSinks.remove(userSink);
                }
            });

            userSinks.stream().forEach(userSink -> {
                for (String subscription : subscriberList) {
                    if (Constants.ALL.equals(subscription) || userSink.getUser().equals(subscription)) {
                        LOG.debug("Broadcasting to {}", userSink.getUser());
                        userSink.getEventSink().send(sseEvent).whenComplete((object, error) -> {
                            if (error != null) {
                                userSinks.remove(userSink);
                            }
                        });
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
    public void subscribe(@Context SseEventSink sseEventSink, @Context SecurityContext securityContext) {
        sseEventSink.send(sse.newEvent("UVMS SSE Ticket notifications"));
        String user = securityContext.getUserPrincipal().getName();
        userSinks.add(new UserSseEventSink(user, sseEventSink));
        sseEventSink.send(sse.newEvent("User " + user + " is now registered"));
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
        private SseEventSink eventSink;

        public UserSseEventSink(String user, SseEventSink sseEventSink) {
            this.user = user;
            this.eventSink = sseEventSink;
        }

        public String getUser() {
            return user;
        }

        public SseEventSink getEventSink() {
            return eventSink;
        }
    }
}
