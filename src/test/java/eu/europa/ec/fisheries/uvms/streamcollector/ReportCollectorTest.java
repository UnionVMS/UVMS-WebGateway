package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetQuery;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.streamcollector.dto.ReportOneRequestDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ReportCollectorTest extends BuildStreamCollectorDeployment {

    @Test
    @OperateOnDeployment("collector")
    public void report1Test() throws InterruptedException {
        ReportOneRequestDto request = new ReportOneRequestDto();
        AssetQuery query = new AssetQuery();
        query.setCfr(Arrays.asList("Test"));
        request.setAssetQuery(query);

       // System.out.println("Now");
       // Thread.sleep(1000 * 60 * 5);

        Response response = getWebTarget()
                .path("reports")
                .path("report1")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(request), Response.class);
        assertEquals(200, response.getStatus());
        String output = response.readEntity(String.class);

        assertNotNull(output);
        assertTrue(output.contains("Movement Module Mock"));
        assertTrue(output.contains(MovementSourceType.OTHER.value()));
    }

}
