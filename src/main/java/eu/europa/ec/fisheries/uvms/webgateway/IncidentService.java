package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.StatusDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.StatusEnum;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Stateless
public class IncidentService {

    private WebTarget assetWebTarget;

    private WebTarget incidentWebTarget;

    @Resource(name = "java:global/asset_endpoint")
    private String assetEndpoint;

    @Resource(name = "java:global/incident_endpoint")
    private String incidentEndpoint;

    @Inject
    private AssetClient assetClient;

    @Inject
    private MovementRestClient movementClient;

    @PostConstruct
    private void setUpClient() {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        Client client = clientBuilder.build();
        client.register(JsonBConfigurator.class);
        assetWebTarget = client.target(assetEndpoint);
        incidentWebTarget = client.target(incidentEndpoint);
    }


    public List<ExtendedIncidentLogDto> incidentLogForIncident(String incidentId, String auth){
        List<IncidentLogDto> dto = incidentWebTarget
                .path("incident")
                .path("incidentLogForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(new GenericType<List<IncidentLogDto>>() {});

        List<ExtendedIncidentLogDto> responseList = new ArrayList<>(dto.size());
        for (IncidentLogDto logDto : dto) {
            Object relatedObject = null;
            if(EventTypeEnum.NOTE_CREATED.equals(logDto.getEventType())){
                relatedObject = getAssetNote(logDto.getRelatedObjectId(), auth);
            }

            responseList.add(new ExtendedIncidentLogDto(logDto, relatedObject));
        }

        return responseList;
    }

    private Note getAssetNote(UUID noteId, String auth){
        Note createdNote = assetWebTarget
                .path("asset")
                .path("note")
                .path(noteId.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(Note.class);

        return createdNote;
    }


    public NoteAndIncidentDto addNoteToIncident(String incidentId, String auth, Note note){
        Note createdNote = addNoteToAsset(note, auth);

        StatusDto incidentStatus = new StatusDto(StatusEnum.NOTE_ADDED, EventTypeEnum.NOTE_CREATED, createdNote.getId());
        IncidentDto incidentDto = updateIncidentStatus(incidentId, incidentStatus, auth);

        NoteAndIncidentDto response = new NoteAndIncidentDto();
        response.setNote(createdNote);
        response.setIncident(incidentDto);

        return response;
    }

    private Note addNoteToAsset(Note note, String auth){
        Note createdNote = assetWebTarget
                .path("asset")
                .path("notes")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(note), Note.class);

        return createdNote;
    }

    private IncidentDto updateIncidentStatus(String incidentId, StatusDto statusDto, String auth){
        IncidentDto dto = incidentWebTarget
                .path("incident")
                .path("updateStatusForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(statusDto), IncidentDto.class);

        return dto;
    }


}
