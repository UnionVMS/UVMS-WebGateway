package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
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
    public void getIncidentLogForIncidentTest()  {
        Response response = getWebTarget()
                .path("incidents")
                .path("incidentLogForIncident")
                .path("555")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);

        assertEquals(200, response.getStatus());
        List<ExtendedIncidentLogDto> output = response.readEntity(new GenericType<List<ExtendedIncidentLogDto>>() {});

        assertFalse(output.isEmpty());
        assertTrue(output.stream().allMatch(dto -> dto.getIncidentLog() != null));
        assertTrue(output.stream().allMatch(dto -> dto.getIncidentLog().getIncidentId() == 555l));
        Optional<ExtendedIncidentLogDto> noteIncidentLog = output.stream().filter(dto -> dto.getIncidentLog().getEventType().equals(EventTypeEnum.NOTE_CREATED)).findAny();
        assertTrue(noteIncidentLog.isPresent());
        assertTrue(noteIncidentLog.get().getRelatedObject() != null);

    }

}
