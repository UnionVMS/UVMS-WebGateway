package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.StatusDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.RelatedObjectType;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.StatusEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollAndIncidentDto;

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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Stateless
public class IncidentService {

    private WebTarget assetWebTarget;

    private WebTarget incidentWebTarget;

    private WebTarget exchangeWebTarget;

    @Resource(name = "java:global/asset_endpoint")
    private String assetEndpoint;

    @Resource(name = "java:global/incident_endpoint")
    private String incidentEndpoint;

    @Resource(name = "java:global/exchange_endpoint")
    private String exchangeEndpoint;

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
        exchangeWebTarget = client.target(exchangeEndpoint);
    }


    public ExtendedIncidentLogDto incidentLogForIncident(String incidentId, String auth){
        List<IncidentLogDto> dto = incidentWebTarget
                .path("incident")
                .path("incidentLogForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(new GenericType<List<IncidentLogDto>>() {});

        ExtendedIncidentLogDto response = new ExtendedIncidentLogDto(dto.size());
        for (IncidentLogDto logDto : dto) {

            response.getIncidentLogs().put(logDto.getId(), logDto);

            if(RelatedObjectType.NOTE.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                Note note = getAssetNote(logDto.getRelatedObjectId(), auth);
                response.getRelatedObjects().getNotes().put(logDto.getRelatedObjectId().toString(), note);

            }else if(RelatedObjectType.MOVEMENT.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                MicroMovement microMovement = movementClient.getMicroMovementById(logDto.getRelatedObjectId());
                response.getRelatedObjects().getPositions().put(logDto.getRelatedObjectId().toString(), microMovement);

            }else if(RelatedObjectType.POLL.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                ExchangeLogStatusType pollStatus = getPollStatus(logDto.getRelatedObjectId(), auth);
                response.getRelatedObjects().getPolls().put(logDto.getRelatedObjectId().toString(), pollStatus);
            }

        }

        return response;
    }

    private Note getAssetNote(UUID noteId, String auth){
        Note note = assetWebTarget
                .path("asset")
                .path("note")
                .path(noteId.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(Note.class);

        return note;
    }

    private ExchangeLogStatusType getPollStatus(UUID pollId, String auth){
        ExchangeLogStatusType pollStatus = exchangeWebTarget
                .path("exchange")
                .path("poll")
                .path(pollId.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(ExchangeLogStatusType.class);

        return pollStatus;
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

    public PollAndIncidentDto addSimplePollToIncident(String incidentId, String auth, String username, String comment){

        IncidentDto incident = getIncident(incidentId, auth);
        UUID assetId = incident.getAssetId();

        String pollId = assetClient.createPollForAsset(assetId, username, comment);

        StatusDto status = new StatusDto();
        status.setRelatedObjectId(UUID.fromString(pollId));
        status.setEventType(EventTypeEnum.POLL_CREATED);
        status.setStatus(StatusEnum.POLL_INITIATED);

        IncidentDto updatedIncident = updateIncidentStatus(incidentId, status, auth);

        PollAndIncidentDto response = new PollAndIncidentDto(pollId, updatedIncident);

        return response;
    }

    private IncidentDto getIncident(String incidentId, String auth){
        IncidentDto dto = incidentWebTarget
                .path("incident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(IncidentDto.class);

        return dto;
    }

    public PollAndIncidentDto addPollToIncident(String incidentId, PollRequestType pollRequest, String auth){

        String pollId = createPollForAsset(pollRequest, auth);

        StatusDto status = new StatusDto();
        status.setRelatedObjectId(UUID.fromString(pollId));
        status.setEventType(EventTypeEnum.POLL_CREATED);
        status.setStatus(StatusEnum.POLL_INITIATED);

        IncidentDto updatedIncident = updateIncidentStatus(incidentId, status, auth);

        PollAndIncidentDto response = new PollAndIncidentDto(pollId, updatedIncident);

        return response;
    }

    private String createPollForAsset(PollRequestType pollRequest, String auth){
        CreatePollResultDto createdPollResponse = assetWebTarget
                .path("poll")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(pollRequest), CreatePollResultDto.class);

        if(createdPollResponse.isUnsentPoll()){
            return createdPollResponse.getUnsentPolls().get(0);
        }else{
            return createdPollResponse.getSentPolls().get(0);
        }

    }

}
