package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.streamcollector.dto.ReportOneRequestDto;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReportCollector {

    @Inject
    AssetClient assetClient;

    @Inject
    MovementRestClient movementClient;

    @POST
    @Path("report1")
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response getAssetList(ReportOneRequestDto request)  {
        List<AssetDTO> assets = assetClient.getAssetList(request.getAssetQuery(), request.getPage(), request.getSize(), request.isDynamic(), request.isIncludeInactivated());
        List<String> assetIds = assets.stream().map(AssetDTO::getId).map(UUID::toString).collect(Collectors.toList());

        return Response.ok(movementClient.getMicroMovementsForConnectIdsBetweenDates(assetIds, DateUtils.stringToDate(request.getStartDate()), DateUtils.stringToDate(request.getEndDate()), request.getSources())).build();
    }


}
