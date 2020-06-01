package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webgateway.dto.ExtendedIncidentLogDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.NoteAndIncidentDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("incidents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncidentCollector {

    @Inject
    IncidentService incidentService;


    @GET
    @Path("incidentLogForIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response incidentLogForIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId)  {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        List<ExtendedIncidentLogDto> responseList = incidentService.incidentLogForIncident(incidentId, auth);

        return Response.ok(responseList).build();
    }


    @POST
    @Path("addNoteToIncident/{incidentId}")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response addNoteToIncident(@Context HttpServletRequest request,@PathParam("incidentId") String incidentId, Note note)  {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        NoteAndIncidentDto response = incidentService.addNoteToIncident(incidentId, auth, note);

        return Response.ok(response).build();
    }



}
