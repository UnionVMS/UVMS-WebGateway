package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetStatistics;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webgateway.dto.UvmsStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsRestResource {

    private final static Logger LOG = LoggerFactory.getLogger(StatisticsRestResource.class);

    @Inject
    AssetClient assetClient;

    @GET
    @RequiresFeature(UnionVMSFeature.viewVesselsAndMobileTerminals)
    public Response uvmsStatistics(){
        try {
            UvmsStatistics statistics = new UvmsStatistics();
            AssetStatistics assetStatistics = assetClient.assetStatistics();
            statistics.setAssetStatistics(assetStatistics);

            return Response.ok(statistics).header("MDC", MDC.get("requestId")).build();
        }catch (Exception e) {
            LOG.error("Could not get UVMS statistics due to: {}", e);
            throw e;
        }
    }
}
