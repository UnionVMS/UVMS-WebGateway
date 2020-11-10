package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.UvmsStatistics;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class StatisticsRestResourceTest extends BuildStreamCollectorDeployment {

    @Test
    @OperateOnDeployment("collector")
    public void uvmsStatistics() throws InterruptedException {

        Response response = getWebTarget()
                .path("statistics")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(Response.class);
        assertEquals(200, response.getStatus());
        UvmsStatistics output = response.readEntity(UvmsStatistics.class);

        assertEquals(Long.valueOf(42), output.getAssetStatistics().getAmountOfVMSAsset());
        assertEquals(Long.valueOf(32), output.getAssetStatistics().getAmountOfVMSAssetsWithLicense());
        assertEquals(Long.valueOf(10), output.getAssetStatistics().getAmountOfVMSAssetsWithInactiveLicense());

    }
}
