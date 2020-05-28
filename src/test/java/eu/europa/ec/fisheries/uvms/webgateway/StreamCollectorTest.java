package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class StreamCollectorTest extends BuildStreamCollectorDeployment {

    private final static Logger LOG = LoggerFactory.getLogger(StreamCollectorTest.class);

    private static String dataString = "";
    private static String eventString = "";
    private static String errorString = "";

    private Jsonb jsonb;

    @Inject
    private JMSContext context;

    @Before
    public void cleanup() {
        dataString = "";
        eventString = "";
        errorString = "";
        jsonb = new JsonBConfigurator().getContext(null);
    }

    @Test
    @OperateOnDeployment("collector")
    public void worldsBestAndMostUsefulArqTest() {
        assertTrue(true);
    }

    @Test
    @OperateOnDeployment("collector")
    public void connectToSSETest() throws InterruptedException {
        SseEventSource source = createSSEEventSource();
        source.open();
        assertTrue(source.isOpen());
        Thread.sleep(1000);
        assertTrue("dataString: " + dataString, dataString.contains("registered"));
        source.close();
        assertFalse(source.isOpen());
    }

    @Test
    @OperateOnDeployment("collector")
    public void sendMessageOnQueueAndCatchItOnSseStream() throws Exception {
        SseEventSource source = createSSEEventSource();
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, null, null);
        Thread.sleep(100);

        assertTrue(eventString, eventString.contains(testEvent));
        assertTrue(dataString, dataString.contains(testData));
        source.close();
    }

    @Test
    @OperateOnDeployment("collector")
    public void sendMessageWithMovementSourceOnQueueAndCatchItOnSseStream() throws Exception {
        SseEventSource source = createSSEEventSource();
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, null, MovementSourceType.MANUAL.value());
        Thread.sleep(100);

        assertTrue(eventString, eventString.contains(testEvent));
        assertTrue(dataString, dataString.contains(testData));
        source.close();
    }

    @Test
    @OperateOnDeployment("collector")
    public void listenToMovementSourceManualAndCatchMoveSourceManual() throws Exception {
        SseEventSource source = createSSEEventSourceWithMovementSourceParam(MovementSourceType.MANUAL.value());
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, null, MovementSourceType.MANUAL.value());
        Thread.sleep(100);

        assertTrue(eventString, eventString.contains(testEvent));
        assertTrue(dataString, dataString.contains(testData));
        source.close();
    }

    @Test
    @OperateOnDeployment("collector")
    public void sendMessageIncludingSubscriberOnQueueAndCatchItOnSseStream() throws Exception {
        SseEventSource source = createSSEEventSource();
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, Collections.singletonList("user"), null);
        Thread.sleep(100);

        assertTrue(eventString, eventString.contains(testEvent));
        assertTrue(dataString, dataString.contains(testData));
        source.close();
    }

    @Test
    @OperateOnDeployment("collector")
    public void sendMessageIncludingOtherSubscriberOnQueueAndWatchTheSseStreamSoThatItDoesNotAppear() throws Exception {
        SseEventSource source = createSSEEventSource();
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, Collections.singletonList("NOT user"), null);
        Thread.sleep(1000);

        assertFalse(eventString, eventString.contains(testEvent));
        assertFalse(dataString, dataString.contains(testData));
        source.close();
    }

    @Test
    @OperateOnDeployment("collector")
    public void sendMessageIncludingOtherMovementSourceOnQueueAndWatchTheSseStreamSoThatItDoesNotAppear() throws Exception {
        SseEventSource source = createSSEEventSourceWithMovementSourceParam(MovementSourceType.OTHER.value());
        source.open();
        String testData = "test data";
        String testEvent = "test event";
        sendDataAsJMSMessageToStream(testData, testEvent, null, MovementSourceType.MANUAL.value());
        Thread.sleep(1000);

        assertFalse(eventString, eventString.contains(testEvent));
        assertFalse(dataString, dataString.contains(testData));
        source.close();
    }

    private SseEventSource createSSEEventSource() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/test/rest/sse/subscribe");
        AuthorizationHeaderWebTarget jwtTarget = new AuthorizationHeaderWebTarget(target, getToken());

        SseEventSource source = SseEventSource.target(jwtTarget).reconnectingEvery(1, TimeUnit.SECONDS).build();
        source.register(onEvent, onError, onComplete);

        return source;
    }

    private SseEventSource createSSEEventSourceWithMovementSourceParam(String movementSource) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/test/rest/sse/subscribe").queryParam("sources", movementSource);
        AuthorizationHeaderWebTarget jwtTarget = new AuthorizationHeaderWebTarget(target, getToken());

        SseEventSource source = SseEventSource.target(jwtTarget).reconnectingEvery(1, TimeUnit.SECONDS).build();
        source.register(onEvent, onError, onComplete);

        return source;
    }

    private void sendDataAsJMSMessageToStream(String data, String eventName, List<String> subscriberList, String movementSource) throws JMSException {
        TextMessage message = context.createTextMessage(data);
        message.setStringProperty(Constants.EVENT, eventName);
        String subscriberJson = (subscriberList == null || subscriberList.isEmpty() ? null : jsonb.toJson(subscriberList));
        message.setStringProperty(Constants.SUBSCRIBERLIST, subscriberJson);
        message.setStringProperty(Constants.MOVEMENT_SOURCE, movementSource);


        Topic t = context.createTopic(Constants.TOPIC_NAME);

        context.createProducer().send(t, message);
    }

    private static Consumer<InboundSseEvent> onEvent = (inboundSseEvent) -> {
        String data = inboundSseEvent.readData();
        eventString = eventString.concat(inboundSseEvent.getName() == null ? " null name " : inboundSseEvent.getName());
        dataString = dataString.concat(data);
    };

    //Error
    private static Consumer<Throwable> onError = (throwable) -> {
        LOG.error("Error while testing sse: ", throwable);
        errorString = throwable.getMessage();
    };

    //Connection close and there is nothing to receive
    private static Runnable onComplete = () -> {
        System.out.println("Done!");
    };
}
