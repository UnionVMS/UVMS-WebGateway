package eu.europa.ec.fisheries.uvms.webGateway;

import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webGateway.dto.TracksByAssetSearchRequestDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReportCollector {

    @Inject
    private AssetClient assetClient;

    @Inject
    private MovementRestClient movementClient;

    @POST
    @Path("tracksByAssetSearch")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getTracksByAssetSearch(TracksByAssetSearchRequestDto request)  {

        List<String> assetIds = assetClient.getAssetIdList(request.getAssetQuery(),
                request.getPage(),
                request.getSize(),
                request.isIncludeInactivated());

        String response = movementClient.getMicroMovementsForConnectIdsBetweenDates(assetIds,
                DateUtils.stringToDate(request.getStartDate()),
                DateUtils.stringToDate(request.getEndDate()),
                request.getSources());

        return Response.ok(response).build();
    }


}
