package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.uvms.incident.model.dto.EventCreationDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.UpdateIncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.RelatedObjectType;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Path("/incident/rest/incident")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class IncidentModuleMock {

    @POST
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response createIncident(IncidentDto incidentDto) {
        incidentDto.setId((long) (Math.random() * 10000d));
        return Response.ok(incidentDto).build();
    }

    @PUT
    @Path("updateType")
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response updateIncidentType(UpdateIncidentDto update) {
        return Response.ok(incidentFromUpdate(update)).build();
    }

    @PUT
    @Path("updateStatus")
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response updateIncidentStatus(UpdateIncidentDto update) {
        return Response.ok(incidentFromUpdate(update)).build();
    }

    @PUT
    @Path("updateExpiry")
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response updateIncidentExpiry(UpdateIncidentDto update) {
        return Response.ok(incidentFromUpdate(update)).build();
    }

    @POST
    @Path("addEventToIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response addEventForToIncident(@PathParam("incidentId") long incidentId, EventCreationDto status) {
        System.setProperty("INCIDENT_MODULE_MOCK_ON_ID", "" + incidentId);
        return Response.ok().build();
    }

    @GET
    @Path("incidentLogForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewAlarmsOpenTickets)
    public Response getIncidentLogForIncident(@PathParam("incidentId") long incidentId) {
        Map<Long, IncidentLogDto> incidentLogs = new TreeMap<>();
        IncidentLogDto pollLog = createMockIncidentLog(incidentId, EventTypeEnum.POLL_CREATED);
        IncidentLogDto noteLog = createMockIncidentLog(incidentId, EventTypeEnum.NOTE_CREATED);
        IncidentLogDto manualLog = createMockIncidentLog(incidentId, EventTypeEnum.MANUAL_POSITION);
        incidentLogs.put(pollLog.getId(), pollLog);
        incidentLogs.put(noteLog.getId(), noteLog);
        incidentLogs.put(manualLog.getId(), manualLog);
        return Response.ok(incidentLogs).build();
    }

    @GET
    @Path("{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewAlarmsOpenTickets)
    public Response getByIncidentId(@PathParam("incidentId") Long incidentId) {
        IncidentDto incident = new IncidentDto();
        incident.setId(incidentId);
        incident.setAssetId(UUID.randomUUID());
        return Response.ok(incident).build();
    }

    private IncidentLogDto createMockIncidentLog(long incidentId, EventTypeEnum eventType){
        IncidentLogDto log = new IncidentLogDto();
        log.setIncidentId(incidentId);
        log.setCreateDate(Instant.now());
        log.setRelatedObjectId(UUID.randomUUID());
        log.setEventType(eventType);
        log.setRelatedObjectType(getRelatedObjectType(eventType));
        log.setMessage("All hail, incident module mocker");
        log.setId((long)(Math.random() * 10000d));

        return log;
    }

    private RelatedObjectType getRelatedObjectType(EventTypeEnum eventType){
        if(EventTypeEnum.NOTE_CREATED.equals(eventType)){
            return RelatedObjectType.NOTE;

        }else if(EventTypeEnum.MANUAL_POSITION.equals(eventType) || EventTypeEnum.INCIDENT_CLOSED.equals(eventType)) {
            return RelatedObjectType.MOVEMENT;

        }else if(EventTypeEnum.POLL_CREATED.equals(eventType) || EventTypeEnum.AUTO_POLL_CREATED.equals(eventType)) {
            return RelatedObjectType.POLL;
        }

        return RelatedObjectType.NONE;
    }

    private IncidentDto incidentFromUpdate(UpdateIncidentDto update){
        IncidentDto dto = new IncidentDto();
        dto.setType(update.getType());
        dto.setStatus(update.getStatus());
        dto.setId(update.getIncidentId());
        dto.setExpiryDate(update.getExpiryDate());
        dto.setAssetId(UUID.randomUUID());

        return dto;
    }
}
