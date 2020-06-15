package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.StatusDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.EventTypeEnum;
import eu.europa.ec.fisheries.uvms.incident.model.dto.enums.RelatedObjectType;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/incident/rest/incident")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class IncidentModuleMock {

    @POST
    @Path("updateStatusForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.manageAlarmsOpenTickets)
    public Response updateIncident(@PathParam("incidentId") long incidentId, StatusDto status) {
        IncidentDto response = new IncidentDto();
        response.setId(incidentId);
        response.setStatus(status.getStatus().name());
        return Response.ok(response).build();
    }

    @GET
    @Path("incidentLogForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewAlarmsOpenTickets)
    public Response getIncidentLogForIncident(@PathParam("incidentId") long incidentId) {
        List<IncidentLogDto> incidentLogs = new ArrayList<>();
        incidentLogs.add(createMockIncidentLog(incidentId, EventTypeEnum.POLL_CREATED));
        incidentLogs.add(createMockIncidentLog(incidentId, EventTypeEnum.NOTE_CREATED));
        incidentLogs.add(createMockIncidentLog(incidentId, EventTypeEnum.MANUAL_POSITION));
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

}
