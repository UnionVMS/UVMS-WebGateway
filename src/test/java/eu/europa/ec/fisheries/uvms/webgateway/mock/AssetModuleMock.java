package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollRequestType;
import eu.europa.ec.fisheries.uvms.asset.client.model.*;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.dto.CreatePollResultDto;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        Note note = null;
        if(!System.getProperty("NOTE_RETURN_NULL", "false").equals("true")){
            note = new Note();
            note.setId(id);
            note.setAssetId(UUID.randomUUID());
            note.setCreatedOn(Instant.now());
            note.setNote("Asset module mock get note FTW");
        }
        return Response.ok(note).header("MDC", MDC.get("requestId")).build();
    }

    @POST
    @Path("internal/createPollForAsset/{id}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response createPollForAsset(@PathParam("id") String assetId, @QueryParam("username") String username, SimpleCreatePoll createPoll) {
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

    @GET
    @Path("internal/asset/guid/{id}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getAssetById(@PathParam("id") String id) {
        System.setProperty("GET_ASSET_REACHED", "true");
        AssetDTO asset = new AssetDTO();
        asset.setId(UUID.fromString(id));
        return Response.ok(asset).build();
    }

    @POST
    @Path("internal/asset")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response upsertAsset(AssetBO assetBo) {
        System.setProperty("UPDATE_ASSET_REACHED", "true");
        return Response.ok(assetBo).build();
    }

    @GET
    @Path("internal/pollListForAsset/{assetId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getPollListByAsset(@PathParam("assetId") String assetId) {
            List<SanePollDto> sanePollDtos = new ArrayList<>();
            SanePollDto pollDto = new SanePollDto();
            pollDto.setAssetId(UUID.fromString(assetId));
            pollDto.setId(UUID.randomUUID());
            sanePollDtos.add(pollDto);
            return Response.ok(sanePollDtos).header("MDC", MDC.get("requestId")).build();
    }

    @GET
    @Path("internal/pollInfo/{pollId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getPollInfo(@PathParam("pollId") String pollId) {
        SanePollDto pollDto = new SanePollDto();
        pollDto.setAssetId(UUID.randomUUID());
        pollDto.setId(UUID.fromString(pollId));
        return Response.ok(pollDto).header("MDC", MDC.get("requestId")).build();
    }
}
