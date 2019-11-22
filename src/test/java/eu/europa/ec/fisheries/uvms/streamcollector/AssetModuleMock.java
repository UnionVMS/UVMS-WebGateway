package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetQuery;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/asset/rest/internal")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class AssetModuleMock {

    @POST
    @Path("query")
    public Response getMockedAssetDto(@DefaultValue("1") @QueryParam("page") int page,
                                      @DefaultValue("100") @QueryParam("size") int size,
                                      @DefaultValue("true") @QueryParam("dynamic") boolean dynamic,
                                      @DefaultValue("false") @QueryParam("includeInactivated") boolean includeInactivated,
                                      AssetQuery query){
        AssetDTO asset = new AssetDTO();
        asset.setId(UUID.randomUUID());

        return Response.ok(asset).build();

    }


}
