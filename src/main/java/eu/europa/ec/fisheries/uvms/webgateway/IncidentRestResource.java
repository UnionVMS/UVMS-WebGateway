package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.asset.client.model.SimpleCreatePoll;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.UpdateIncidentDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollIdDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("incidents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncidentRestResource {

    private final static Logger LOG = LoggerFactory.getLogger(IncidentRestResource.class);

    @Inject
    IncidentService incidentService;


    @GET
    @Path("incidentLogForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response incidentLogForIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId)  {
        try{
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            ExtendedIncidentLogDto response = incidentService.incidentLogForIncident(incidentId, auth);

            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error getting incident log for incident: " ,e.getMessage(), e);
            throw e;
        }
    }


    @POST
    @Path("addNoteToIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response addNoteToIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId, Note note)  {
        try {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            Note response = incidentService.addNoteToIncident(incidentId, auth, note);

            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error adding note to incident: " , e.getMessage(), e);
            throw e;
        }
    }

    @POST
    @Path("createSimplePollForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createSimplePollForIncident(@Context HttpServletRequest request, @PathParam("incidentId") String incidentId, SimpleCreatePoll pollDto) {
        try{
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            String response = incidentService.addSimplePollToIncident(incidentId, auth, request.getRemoteUser(), pollDto.getComment());
            return Response.ok(new PollIdDto(response)).build();
        }catch (Exception e){
            LOG.error("Error creating simple poll for incident: ", e.getMessage(), e);
            throw e;
        }
    }

    @POST
    @Path("createPollForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createPollForIncident(@Context HttpServletRequest request, @PathParam("incidentId") String incidentId, PollRequestType pollRequest) {
        try{
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            String response = incidentService.addPollToIncident(incidentId, pollRequest, auth);
            return Response.ok(new PollIdDto(response)).build();
        }catch (Exception e){
            LOG.error("Error creating poll for incident: ", e.getMessage(), e);
            throw e;
        }
    }

    @POST
    @Path("createIncident/")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createIncident(@Context HttpServletRequest request, IncidentDto incident) {
        try {
            String user = request.getRemoteUser();
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            IncidentDto response = incidentService.createIncident(incident, auth, user);
            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error while creating incident: {}", e.getMessage(), e);
            throw e;

        }
    }

    @PUT
    @Path("updateIncidentType/")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response updateIncidentType(@Context HttpServletRequest request, UpdateIncidentDto update) {
        try {
            String user = request.getRemoteUser();
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            IncidentDto response = incidentService.updateIncidentType(update, auth, user);
            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error while updating incident type: {}", e.getMessage(), e);
            throw e;

        }
    }

    @PUT
    @Path("updateIncidentStatus/")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response updateIncidentStatus(@Context HttpServletRequest request, UpdateIncidentDto update) {
        try {
            String user = request.getRemoteUser();
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            IncidentDto response = incidentService.updateIncidentStatus(update, auth, user);
            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error while updating incident status: {}", e.getMessage(), e);
            throw e;

        }
    }

    @PUT
    @Path("updateIncidentExpiry/")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response updateIncidentExpiry(@Context HttpServletRequest request, UpdateIncidentDto update) {
        try {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            IncidentDto response = incidentService.updateIncidentExpiry(update, auth);
            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error while updating incident expiry date: {}", e.getMessage(), e);
            throw e;

        }
    }

}
