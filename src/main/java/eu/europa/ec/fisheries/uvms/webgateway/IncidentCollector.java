package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentDto;
import eu.europa.ec.fisheries.uvms.incident.model.dto.StatusDto;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CommentDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollAndIncidentDto;
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
public class IncidentCollector {

    private final static Logger LOG = LoggerFactory.getLogger(IncidentCollector.class);

    @Inject
    IncidentService incidentService;


    @GET
    @Path("incidentLogForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response incidentLogForIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId)  {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        ExtendedIncidentLogDto response = incidentService.incidentLogForIncident(incidentId, auth);

        return Response.ok(response).build();
    }


    @POST
    @Path("addNoteToIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response addNoteToIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId, Note note)  {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        NoteAndIncidentDto response = incidentService.addNoteToIncident(incidentId, auth, note);

        return Response.ok(response).build();
    }

    @POST
    @Path("createSimplePollForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createPollForIncident(@Context HttpServletRequest request, @PathParam("incidentId") String incidentId, CommentDto pollDto) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        PollAndIncidentDto response = incidentService.addSimplePollToIncident(incidentId, auth, request.getRemoteUser(), pollDto.getComment());
        return Response.ok(response).build();
    }

    @POST
    @Path("createPollForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createPollForAsset(@Context HttpServletRequest request, @PathParam("incidentId") String incidentId, PollRequestType pollRequest) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        PollAndIncidentDto response = incidentService.addPollToIncident(incidentId, pollRequest, auth);
        return Response.ok(response).build();
    }

    @POST
    @Path("updateStatusForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response updateStatusForIncident(@Context HttpServletRequest request, @PathParam("incidentId") String incidentId, StatusDto status) {
        try {
            String user = request.getRemoteUser();
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            IncidentDto response = incidentService.updateStatusForIncident(incidentId, status, auth, user);
            return Response.ok(response).build();
        }catch (Exception e){
            LOG.error("Error while updating incident status: {}", e.getMessage(), e);
            throw e;

        }
    }

}
