package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.*;
import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.exchange.client.ExchangeRestClient;
import eu.europa.ec.fisheries.uvms.incident.model.dto.EventCreationDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.UpdateIncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.IncidentType;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.RelatedObjectType;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.StatusEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollInfoDto;
import eu.europa.ec.fisheries.uvms.webgateway.filter.AppError;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Stateless
public class IncidentService {

    private static List<IncidentType> INCIDENT_PARKED_GROUP = Arrays.asList(IncidentType.OWNERSHIP_TRANSFER ,IncidentType.SEASONAL_FISHING ,IncidentType.PARKED);

    private WebTarget assetWebTarget;

    private WebTarget incidentWebTarget;

    private WebTarget exchangeWebTarget;

    private WebTarget mrWebTarget;

    private Jsonb json;

    @Resource(name = "java:global/asset_endpoint")
    private String assetEndpoint;

    @Resource(name = "java:global/incident_endpoint")
    private String incidentEndpoint;

    @Resource(name = "java:global/exchange_endpoint")
    private String exchangeEndpoint;

    @Resource(name = "java:global/movement-rules_endpoint")
    private String mrEndpoint;

    @Inject
    private AssetClient assetClient;

    @Inject
    private MovementRestClient movementClient;

    @Inject
    ExchangeRestClient exchangeRestClient;

    @Inject
    PollService pollService;

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
        mrWebTarget = client.target(mrEndpoint);

        json = new JsonBConfiguratorWebGateway().getContext(null);
    }

    public ExtendedIncidentLogDto incidentLogForIncident(String incidentId, String auth){
        Map<Long, IncidentLogDto> dto = getIncidentLogForIncident(incidentId, auth);

        ExtendedIncidentLogDto response = new ExtendedIncidentLogDto(dto.size());
        response.setIncidentLogs(dto);
        for (IncidentLogDto logDto : dto.values()) {

            if(RelatedObjectType.NOTE.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                Note note = getAssetNote(logDto.getRelatedObjectId(), auth);
                response.getRelatedObjects().getNotes().put(logDto.getRelatedObjectId().toString(), note);

            }else if(RelatedObjectType.MOVEMENT.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                MovementDto microMovement = movementClient.getMovementById(logDto.getRelatedObjectId());
                response.getRelatedObjects().getPositions().put(logDto.getRelatedObjectId().toString(), microMovement);

            }else if(RelatedObjectType.POLL.equals(logDto.getRelatedObjectType()) && logDto.getRelatedObjectId() != null) {
                PollInfoDto pollInfo = pollService.getPollInfo(logDto.getRelatedObjectId());
                response.getRelatedObjects().getPolls().put(logDto.getRelatedObjectId().toString(), pollInfo);
            }

        }

        return response;
    }

    private Map<Long, IncidentLogDto> getIncidentLogForIncident(String incidentId, String auth){
        String jsonResponse = incidentWebTarget
                .path("incident")
                .path("incidentLogForIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(String.class);

        if(jsonResponse.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonResponse, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        return json.fromJson(jsonResponse, new HashMap<Long, IncidentLogDto>(){}.getClass().getGenericSuperclass());
    }

    private Note getAssetNote(UUID noteId, String auth){
        String jsonNote = assetWebTarget
                .path("asset")
                .path("note")
                .path(noteId.toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(String.class);

        if(jsonNote.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonNote, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        return json.fromJson(jsonNote, Note.class);
    }

    public Note addNoteToIncident(String incidentId, String auth, Note note){
        Note createdNote = addNoteToAsset(note, auth);

        EventCreationDto eventCreation = new EventCreationDto(EventTypeEnum.NOTE_CREATED, createdNote.getId());
        addEventToIncident(incidentId, eventCreation, auth);

        return createdNote;
    }

    private Note addNoteToAsset(Note note, String auth){
        String jsonCreatedNote = assetWebTarget
                .path("asset")
                .path("notes")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(note), String.class);

        if(jsonCreatedNote.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonCreatedNote, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }

        return json.fromJson(jsonCreatedNote, Note.class);
    }

    private IncidentDto createIncident(IncidentDto incident, String auth){
        String jsonDto = incidentWebTarget
                .path("incident")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(incident), String.class);

        if(jsonDto.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonDto, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        return json.fromJson(jsonDto, IncidentDto.class);
    }

    public IncidentDto updateIncidentType(UpdateIncidentDto update, String auth, String user){
        IncidentDto originalIncident = getIncident("" + update.getIncidentId(), auth);
        IncidentDto updatedIncident = updateIncident(update, Constants.UPDATE_INCIDENT_TYPE_ADDRESS, auth);

        if(isIncidentTypeInParkedGroup(updatedIncident)){
            if(!originalIncident.getType().equals(updatedIncident.getType())) {
                setParkedOnAsset(updatedIncident.getAssetId().toString(), user, true);
                removeAssetFromPreviousReport(updatedIncident.getAssetId().toString(), auth);
            }
        }

        return updatedIncident;
    }

    public IncidentDto updateIncidentStatus(UpdateIncidentDto update, String auth, String user){
        IncidentDto updatedIncident = updateIncident(update, Constants.UPDATE_INCIDENT_STATUS_ADDRESS, auth);

        if(updatedIncident.getStatus().equals(StatusEnum.RESOLVED)){
            if(isIncidentTypeInParkedGroup(updatedIncident)){
                setParkedOnAsset(updatedIncident.getAssetId().toString(), user, false);
            }
        }

        return updatedIncident;
    }

    public IncidentDto updateIncidentExpiry(UpdateIncidentDto update, String auth){
        IncidentDto updatedIncident = updateIncident(update, Constants.UPDATE_INCIDENT_EXPIRY_ADDRESS, auth);

        return updatedIncident;
    }

    private IncidentDto updateIncident(UpdateIncidentDto update, String updateAddress, String auth){
        String jsonDto = incidentWebTarget
                .path("incident")
                .path(updateAddress)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .put(Entity.json(update), String.class);

        if(jsonDto.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonDto, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        return json.fromJson(jsonDto, IncidentDto.class);
    }


    private boolean isIncidentTypeInParkedGroup(IncidentDto incident){
        return INCIDENT_PARKED_GROUP.contains(incident.getType());
    }

    public void addEventToIncident(String incidentId, EventCreationDto eventCreation, String auth){
        Response response = incidentWebTarget
                .path("incident")
                .path("addEventToIncident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(eventCreation), Response.class);
        String jsonDto = response.readEntity(String.class);

        if(jsonDto.contains("\"code\":")){
            String errorDescription = json.fromJson(jsonDto, AppError.class).description;
            throw new RuntimeException(errorDescription);
        }
    }


    public String addSimplePollToIncident(String incidentId, String auth, String username, String comment){

        IncidentDto incident = getIncident(incidentId, auth);
        UUID assetId = incident.getAssetId();

        String pollId = assetClient.createPollForAsset(assetId, username, comment, PollType.MANUAL_POLL);

        EventCreationDto eventCreation = new EventCreationDto();
        eventCreation.setRelatedObjectId(UUID.fromString(pollId));
        eventCreation.setEventType(EventTypeEnum.POLL_CREATED);

        addEventToIncident(incidentId, eventCreation, auth);

        return pollId;
    }

    private IncidentDto getIncident(String incidentId, String auth){
        String jsonDto = incidentWebTarget
                .path("incident")
                .path(incidentId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .get(String.class);

        if(jsonDto.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonDto, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        return json.fromJson(jsonDto, IncidentDto.class);
    }

    public String addPollToIncident(String incidentId, PollRequestType pollRequest, String auth){

        String pollId = createPollForAsset(pollRequest, auth);

        EventCreationDto eventCreation = new EventCreationDto();
        eventCreation.setRelatedObjectId(UUID.fromString(pollId));
        eventCreation.setEventType(EventTypeEnum.POLL_CREATED);

        addEventToIncident(incidentId, eventCreation, auth);

        return pollId;
    }

    private String createPollForAsset(PollRequestType pollRequest, String auth){
        String jsonCreatedPollResponse = assetWebTarget
                .path("poll")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .post(Entity.json(pollRequest), String.class);

        if(jsonCreatedPollResponse.contains("\"code\":")){
            String errorDeskription = json.fromJson(jsonCreatedPollResponse, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
        CreatePollResultDto createdPollResponse = json.fromJson(jsonCreatedPollResponse, CreatePollResultDto.class);

        if(createdPollResponse.isUnsentPoll()){
            return createdPollResponse.getUnsentPolls().get(0);
        }else{
            return createdPollResponse.getSentPolls().get(0);
        }

    }

    public IncidentDto createIncident(IncidentDto incident, String auth, String user) {
        IncidentDto updatedIncident = createIncident(incident, auth);
        if(updatedIncident.getId() == null){
            return null;
        }

        if(!updatedIncident.getType().equals(IncidentType.MANUAL_POSITION_MODE) && !updatedIncident.getType().equals(IncidentType.ASSET_NOT_SENDING)){
            removeAssetFromPreviousReport(updatedIncident.getAssetId().toString(), auth);
            setParkedOnAsset(updatedIncident.getAssetId().toString(), user, true);
        }

        return updatedIncident;
    }

    private void removeAssetFromPreviousReport(String assetId, String auth) {
        Response response = mrWebTarget
                .path("previousReports")
                .path("byAsset")
                .path(assetId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .delete(Response.class);
        String responseString = response.readEntity(String.class);
        if(response.getStatus() != 200 || responseString.contains("\"code\":")){
            String errorDeskription = json.fromJson(responseString, AppError.class).description;
            throw new RuntimeException(errorDeskription);
        }
    }

    private void setParkedOnAsset(String assetId, String user, boolean parked){
        AssetDTO assetById = assetClient.getAssetById(AssetIdentifier.GUID, assetId);
        assetById.setParked(parked);
        assetById.setUpdatedBy(user);
        AssetBO bo = new AssetBO();
        bo.setAsset(assetById);
        assetClient.upsertAsset(bo);
    }

}
