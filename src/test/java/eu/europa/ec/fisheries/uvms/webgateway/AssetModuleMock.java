package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetListResponse;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
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
                                      String query) {

        AssetListResponse assetListResponse = new AssetListResponse();
        assetListResponse.setCurrentPage(1);
        assetListResponse.setTotalNumberOfPages(10);

        AssetDTO asset = new AssetDTO();
        asset.setId(UUID.randomUUID());
        assetListResponse.setAssetList(Collections.singletonList(asset));

        return Response.ok(assetListResponse).build();
    }

    @POST
    @Path("queryIdOnly")
    public Response getMockedAssetId(@DefaultValue("1") @QueryParam("page") int page,
                                      @DefaultValue("100") @QueryParam("size") int size,
                                      @DefaultValue("true") @QueryParam("dynamic") boolean dynamic,
                                      @DefaultValue("false") @QueryParam("includeInactivated") boolean includeInactivated,
                                      String query){

        return Response.ok(Collections.singletonList(UUID.randomUUID())).build();
    }

}