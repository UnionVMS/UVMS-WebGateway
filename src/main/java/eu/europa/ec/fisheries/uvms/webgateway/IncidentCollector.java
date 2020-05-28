package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("incidents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncidentCollector {

    @Inject
    private AssetClient assetClient;

    @Inject
    private MovementRestClient movementClient;

    @POST
    @Path("addNoteToIncident")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getTracksByAssetSearch(Note request)  {





        return Response.ok("response").build();
    }


}
