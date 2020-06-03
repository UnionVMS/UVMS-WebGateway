package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollType;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetListResponse;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Path("/asset/rest")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class AssetModuleMock {

    @POST
    @Path("internal/query")
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
    @Path("internal/queryIdOnly")
    public Response getMockedAssetId(@DefaultValue("1") @QueryParam("page") int page,
                                      @DefaultValue("100") @QueryParam("size") int size,
                                      @DefaultValue("true") @QueryParam("dynamic") boolean dynamic,
                                      @DefaultValue("false") @QueryParam("includeInactivated") boolean includeInactivated,
                                      String query){

        return Response.ok(Collections.singletonList(UUID.randomUUID())).build();
    }

    @POST
    @Path("asset/notes")
    public Response createNote(Note note){

        note.setId(UUID.randomUUID());
        note.setCreatedOn(Instant.now());

        return Response.ok(note).build();
    }

    @GET
    @Path("asset/note/{id}")
    @RequiresFeature(UnionVMSFeature.manageVessels)
    public Response getNoteById(@PathParam("id") UUID id) {
        Note note = new Note();
        note.setId(id);
        note.setAssetId(UUID.randomUUID());
        note.setCreatedOn(Instant.now());
        note.setNote("Asset module mock get note FTW");
        return Response.ok(note).header("MDC", MDC.get("requestId")).build();
    }

    @POST
    @Path("internal/createPollForAsset/{id}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response createPollForAsset(@PathParam("id") String assetId, @QueryParam("username") String username, @QueryParam("comment") String comment) {
        CreatePollResultDto resultDto = new CreatePollResultDto();
        resultDto.getSentPolls().add(UUID.randomUUID().toString());
        return Response.ok(resultDto).build();
    }

    @POST
    @Path("poll/")
    @RequiresFeature(UnionVMSFeature.managePolls)
    public Response createPoll(PollRequestType createPoll) {
        CreatePollResultDto resultDto = new CreatePollResultDto();
        resultDto.getSentPolls().add(UUID.randomUUID().toString());
        return Response.ok(resultDto).header("MDC", MDC.get("requestId")).build();
    }
}
