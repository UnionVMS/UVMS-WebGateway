package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
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

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ReportCollectorTest extends BuildStreamCollectorDeployment {

    @Test
    @OperateOnDeployment("collector")
    public void report1Test() throws InterruptedException {
        ReportOneRequestDto request = new ReportOneRequestDto();
        request.setAssetQuery("Test");

       // System.out.println("Now");
       // Thread.sleep(1000 * 60 * 5);

        Response response = getWebTarget()
                .path("reports")
                .path("report1")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .post(Entity.json(request), Response.class);
        assertEquals(200, response.getStatus());
        MicroMovementExtended output = response.readEntity(MicroMovementExtended.class);

        assertNotNull(output);
        assertEquals("Movement Module Mock", output.getAsset());
        assertEquals(MovementSourceType.OTHER, output.getMicroMove().getSource());
        assertEquals(2.0, output.getMicroMove().getLocation().getLongitude(), 0.0001d);
    }

}
