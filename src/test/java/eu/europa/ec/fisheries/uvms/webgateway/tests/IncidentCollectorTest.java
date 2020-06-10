package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.StatusEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CommentDto;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollAndIncidentDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class IncidentCollectorTest extends BuildStreamCollectorDeployment {

    @Test
    @OperateOnDeployment("collector")
    public void addNoteToIncidentTest()  {
        Note note = new Note();
        note.setAssetId(UUID.randomUUID());
        note.setCreatedBy("web gateway tester");
        note.setNote("The actual note");

        Response response = getWebTarget()
                .path("incidents")
                .path("addNoteToIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(note), Response.class);

        assertEquals(200, response.getStatus());
        NoteAndIncidentDto output = response.readEntity(NoteAndIncidentDto.class);

        assertEquals(555l, output.getIncident().getId().longValue());
        assertEquals(note.getCreatedBy(), output.getNote().getCreatedBy());
        assertNotNull(output.getNote().getId());
        assertEquals(note.getAssetId(), output.getNote().getAssetId());

    }

    @Test
    @OperateOnDeployment("collector")
    public void addSimplePollToIncidentTest()  {
        CommentDto comment = new CommentDto();
        comment.setComment("add simple poll to asset test");

        Response response = getWebTarget()
                .path("incidents")
                .path("createSimplePollForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(comment), Response.class);

        assertEquals(200, response.getStatus());
        PollAndIncidentDto output = response.readEntity(PollAndIncidentDto.class);

        assertNotNull(output.getPollId());
        assertNotNull(output.getIncident().getId());
        assertEquals(StatusEnum.POLL_INITIATED.name(), output.getIncident().getStatus());
    }

    @Test
    @OperateOnDeployment("collector")
    public void addPollToIncidentTest()  {
        PollRequestType pollRequest = new PollRequestType();
        pollRequest.setComment("add poll to asset test");
        pollRequest.setPollType(PollType.MANUAL_POLL);
        pollRequest.setUserName("web-gateway tester");

        Response response = getWebTarget()
                .path("incidents")
                .path("createPollForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(pollRequest), Response.class);

        assertEquals(200, response.getStatus());
        PollAndIncidentDto output = response.readEntity(PollAndIncidentDto.class);

        assertNotNull(output.getPollId());
        assertNotNull(output.getIncident().getId());
        assertEquals(StatusEnum.POLL_INITIATED.name(), output.getIncident().getStatus());
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

        Note outputNote = output.getNotes().get(noteIncidentLog.get().getRelatedObjectId().toString());
        assertTrue(outputNote != null);

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

        MicroMovement outputMovement = output.getPositions().get(movementIncidentLog.get().getRelatedObjectId().toString());
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

        ExchangeLogStatusType outputPollStatus = output.getPolls().get(pollIncidentLog.get().getRelatedObjectId().toString());
        assertTrue(outputPollStatus != null);

    }

}
