package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollInfoDto;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class PollRestResourceTest extends BuildStreamCollectorDeployment {

    @Test
    @OperateOnDeployment("collector")
    public void getAllPollsForAsset() {
        Response response = getWebTarget()
                .path("poll")
                .path("pollsForAsset")
                .path(UUID.randomUUID().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get( Response.class);

        assertEquals(200, response.getStatus());
        Map<String, PollInfoDto> output = response.readEntity(new GenericType<Map<String, PollInfoDto>>() {});

        assertNotNull(output);
        assertFalse(output.isEmpty());

        PollInfoDto pollInfo = output.values().iterator().next();

        assertNotNull(pollInfo.getPollInfo());
        assertNotNull(pollInfo.getPollStatus());
        assertNotNull(pollInfo.getMobileTerminalSnapshot());
        assertNotNull(pollInfo.getMobileTerminalSnapshot().getId());

        assertNotNull(output.get(pollInfo.getPollInfo().getId().toString()));
    }

    @Test
    @OperateOnDeployment("collector")
    public void getAllPollsForAssetWithPosition() {
        Response response = getWebTarget()
                .path("poll")
                .path("pollsForAsset")
                .path(UUID.randomUUID().toString())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get( Response.class);

        assertEquals(200, response.getStatus());
        Map<String, PollInfoDto> output = response.readEntity(new GenericType<Map<String, PollInfoDto>>() {});

        List<PollInfoDto> pollInfos = new ArrayList<>(output.values());
        assertEquals(1, pollInfos.size());

        PollInfoDto pollInfo = pollInfos.get(0);

        assertNotNull(pollInfo.getMovement());
        assertNotNull(pollInfo.getMovement().getId());
    }
}
