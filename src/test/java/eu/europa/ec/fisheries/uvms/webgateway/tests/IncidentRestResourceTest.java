package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.asset.client.model.SimpleCreatePoll;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.UpdateIncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.IncidentType;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.StatusEnum;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollIdDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollInfoDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class IncidentRestResourceTest extends BuildStreamCollectorDeployment {

    private String INCIDENT_MODULE_MOCK_ON_ID = "INCIDENT_MODULE_MOCK_ON_ID";

    @Before
    public void clearModuleMockReached(){
        System.clearProperty("MR_MODULE_REACHED");
        System.clearProperty("GET_ASSET_REACHED");
        System.clearProperty("UPDATE_ASSET_REACHED");
        System.clearProperty("NOTE_RETURN_NULL");
    }

    @Test
    @OperateOnDeployment("collector")
    public void addNoteToIncidentTest()  {
        Note note = new Note();
        note.setAssetId(UUID.randomUUID());
        note.setCreatedBy("web gateway tester");
        note.setNote("The actual note");

        System.clearProperty(INCIDENT_MODULE_MOCK_ON_ID);
        String incidentId = "" + ((long) (Math.random() * 10000d));

        Response response = getWebTarget()
                .path("incidents")
                .path("addNoteToIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(note), Response.class);

        assertEquals(200, response.getStatus());
        Note output = response.readEntity(Note.class);

        assertEquals(incidentId, System.getProperty(INCIDENT_MODULE_MOCK_ON_ID));
        assertEquals(note.getCreatedBy(), output.getCreatedBy());
        assertNotNull(output.getId());
        assertEquals(note.getAssetId(), output.getAssetId());

    }

    @Test
    @OperateOnDeployment("collector")
    public void addSimplePollToIncidentTest()  {
        SimpleCreatePoll comment = new SimpleCreatePoll();
        comment.setComment("add simple poll to asset test");

        System.clearProperty(INCIDENT_MODULE_MOCK_ON_ID);
        String incidentId = "" + ((long) (Math.random() * 10000d));

        Response response = getWebTarget()
                .path("incidents")
                .path("createSimplePollForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(comment), Response.class);

        assertEquals(200, response.getStatus());
        String output = response.readEntity(PollIdDto.class).getPollId();
        assertNotNull(output);
        assertNotNull(UUID.fromString(output));

        assertEquals(incidentId, System.getProperty(INCIDENT_MODULE_MOCK_ON_ID));
    }

    @Test
    @OperateOnDeployment("collector")
    public void addPollToIncidentTest()  {
        PollRequestType pollRequest = new PollRequestType();
        pollRequest.setComment("add poll to asset test");
        pollRequest.setPollType(PollType.MANUAL_POLL);
        pollRequest.setUserName("web-gateway tester");

        System.clearProperty(INCIDENT_MODULE_MOCK_ON_ID);
        String incidentId = "" + ((long) (Math.random() * 10000d));

        Response response = getWebTarget()
                .path("incidents")
                .path("createPollForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(pollRequest), Response.class);

        assertEquals(200, response.getStatus());
        String output = response.readEntity(PollIdDto.class).getPollId();
        assertNotNull(output);
        assertNotNull(UUID.fromString(output));

        assertNotNull(output);
        assertEquals(incidentId, System.getProperty(INCIDENT_MODULE_MOCK_ON_ID));
    }

    @Test
    @OperateOnDeployment("collector")
    public void getIncidentLogForIncidentCheckNoteTest()  {
        Response response = getWebTarget()
                .path("incidents")
                .path("incidentLogForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);

        assertEquals(200, response.getStatus());
        ExtendedIncidentLogDto output = response.readEntity(ExtendedIncidentLogDto.class);

        assertFalse(output.getIncidentLogs().isEmpty());
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto != null));
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto.getIncidentId() == 555l));

        Optional<IncidentLogDto> noteIncidentLog = output.getIncidentLogs().values().stream().filter(dto -> dto.getEventType().equals(EventTypeEnum.NOTE_CREATED)).findAny();
        assertTrue(noteIncidentLog.isPresent());

        Note outputNote = output.getRelatedObjects().getNotes().get(noteIncidentLog.get().getRelatedObjectId().toString());
        assertTrue(outputNote != null);

    }

    @Test
    @OperateOnDeployment("collector")
    public void getIncidentLogWithNoteThatDoesNotExist()  {
        System.setProperty("NOTE_RETURN_NULL", "true");
        Response response = getWebTarget()
                .path("incidents")
                .path("incidentLogForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);

        assertEquals(200, response.getStatus());
        ExtendedIncidentLogDto output = response.readEntity(ExtendedIncidentLogDto.class);

        assertFalse(output.getIncidentLogs().isEmpty());
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto != null));
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto.getIncidentId() == 555l));

        Optional<IncidentLogDto> noteIncidentLog = output.getIncidentLogs().values().stream().filter(dto -> dto.getEventType().equals(EventTypeEnum.NOTE_CREATED)).findAny();
        assertTrue(noteIncidentLog.isPresent());

        assertTrue(output.getRelatedObjects().getNotes().isEmpty());

    }

    @Test
    @OperateOnDeployment("collector")
    public void getIncidentLogForIncidentCheckMovementTest()  {
        Response response = getWebTarget()
                .path("incidents")
                .path("incidentLogForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);

        assertEquals(200, response.getStatus());
        ExtendedIncidentLogDto output = response.readEntity(ExtendedIncidentLogDto.class);

        assertFalse(output.getIncidentLogs().isEmpty());
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto != null));
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto.getIncidentId() == 555l));

        Optional<IncidentLogDto> movementIncidentLog = output.getIncidentLogs().values().stream().filter(dto -> dto.getEventType().equals(EventTypeEnum.MANUAL_POSITION)).findAny();
        assertTrue(movementIncidentLog.isPresent());

        MovementDto outputMovement = output.getRelatedObjects().getPositions().get(movementIncidentLog.get().getRelatedObjectId().toString());
        assertTrue(outputMovement != null);

    }


    @Test
    @OperateOnDeployment("collector")
    public void getIncidentLogForIncidentCheckPollStatusTest()  {
        Response response = getWebTarget()
                .path("incidents")
                .path("incidentLogForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);

        assertEquals(200, response.getStatus());
        ExtendedIncidentLogDto output = response.readEntity(ExtendedIncidentLogDto.class);

        assertFalse(output.getIncidentLogs().isEmpty());
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto != null));
        assertTrue(output.getIncidentLogs().values().stream().allMatch(dto -> dto.getIncidentId() == 555l));

        Optional<IncidentLogDto> pollIncidentLog = output.getIncidentLogs().values().stream().filter(dto -> dto.getEventType().equals(EventTypeEnum.POLL_CREATED)).findAny();
        assertTrue(pollIncidentLog.isPresent());

        PollInfoDto outputPollStatus = output.getRelatedObjects().getPolls().get(pollIncidentLog.get().getRelatedObjectId().toString());
        assertTrue(outputPollStatus != null);
        assertNotNull(outputPollStatus.getPollStatus());
        assertNotNull(outputPollStatus.getPollInfo());

    }

    @Test
    @OperateOnDeployment("collector")
    public void createParkedIncident()  {
        IncidentDto incident = createBasicIncidentDto();
        incident.setType(IncidentType.PARKED);
        incident.setExpiryDate(Instant.now().plus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));

        Response response = getWebTarget()
                .path("incidents")
                .path("createIncident")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(incident), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());

        assertEquals("true", System.getProperty("MR_MODULE_REACHED"));
        assertEquals("true", System.getProperty("GET_ASSET_REACHED"));
        assertEquals("true", System.getProperty("UPDATE_ASSET_REACHED"));

    }

    @Test
    @OperateOnDeployment("collector")
    public void createManualModeIncident()  {
        IncidentDto incident = createBasicIncidentDto();
        incident.setType(IncidentType.MANUAL_POSITION_MODE);
        incident.setExpiryDate(Instant.now().plus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));

        Response response = getWebTarget()
                .path("incidents")
                .path("createIncident")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(incident), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());

        assertNull(System.getProperty("MR_MODULE_REACHED"));
        assertNull(System.getProperty("GET_ASSET_REACHED"));
        assertNull(System.getProperty("UPDATE_ASSET_REACHED"));
    }

    @Test
    @OperateOnDeployment("collector")
    public void updateParkedIncidentToResolved()  {
        UpdateIncidentDto update = new UpdateIncidentDto();
        update.setIncidentId(555l);
        update.setType(IncidentType.PARKED);
        update.setStatus(StatusEnum.RESOLVED);

        Response response = getWebTarget()
                .path("incidents")
                .path("updateIncidentStatus")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(update), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());

        assertEquals("true", System.getProperty("GET_ASSET_REACHED"));
        assertEquals("true", System.getProperty("UPDATE_ASSET_REACHED"));
    }

    @Test
    @OperateOnDeployment("collector")
    public void updateIncidentTypeToParked()  {
        UpdateIncidentDto update = new UpdateIncidentDto();
        update.setIncidentId(555l);
        update.setType(IncidentType.PARKED);
        update.setStatus(StatusEnum.PARKED);

        Response response = getWebTarget()
                .path("incidents")
                .path("updateIncidentType")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(update), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());

        assertEquals("true", System.getProperty("GET_ASSET_REACHED"));
        assertEquals("true", System.getProperty("UPDATE_ASSET_REACHED"));
    }

    @Test
    @OperateOnDeployment("collector")
    public void updateManualIncidentToAttempted()  {
        UpdateIncidentDto update = new UpdateIncidentDto();
        update.setIncidentId(555l);
        update.setType(IncidentType.MANUAL_POSITION_MODE);
        update.setStatus(StatusEnum.ATTEMPTED_CONTACT);

        Response response = getWebTarget()
                .path("incidents")
                .path("updateIncidentStatus")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(update), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());

        assertNull(System.getProperty("GET_ASSET_REACHED"));
        assertNull(System.getProperty("UPDATE_ASSET_REACHED"));
    }

    @Test
    @OperateOnDeployment("collector")
    public void updateIncidentExpiry()  {
        UpdateIncidentDto update = new UpdateIncidentDto();
        update.setIncidentId(555l);
        update.setType(IncidentType.MANUAL_POSITION_MODE);
        update.setStatus(StatusEnum.ATTEMPTED_CONTACT);
        update.setExpiryDate(Instant.now());

        Response response = getWebTarget()
                .path("incidents")
                .path("updateIncidentExpiry")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .put(Entity.json(update), Response.class);

        assertEquals(200, response.getStatus());
        IncidentDto output = response.readEntity(IncidentDto.class);
        assertNotNull(output.getAssetId());
        assertNotNull(output.getId());
        assertEquals(update.getExpiryDate().truncatedTo(ChronoUnit.MILLIS), output.getExpiryDate());

    }

    public static IncidentDto createBasicIncidentDto() {
        IncidentDto incidentDto = new IncidentDto();
        incidentDto.setAssetId(UUID.randomUUID());
        incidentDto.setAssetName("Test asset");
        incidentDto.setStatus(StatusEnum.INCIDENT_CREATED);
        incidentDto.setType(IncidentType.PARKED);
        return incidentDto;
    }

}
