package eu.europa.ec.fisheries.uvms.webgateway.tests;

import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.asset.client.model.search.SearchBranch;
import eu.europa.ec.fisheries.uvms.asset.client.model.search.SearchFields;
import eu.europa.ec.fisheries.uvms.webgateway.BuildStreamCollectorDeployment;
import eu.europa.ec.fisheries.uvms.webgateway.dto.TracksByAssetSearchRequestDto;
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
    public void getTracksByAssetSearchTest() throws InterruptedException {
        TracksByAssetSearchRequestDto request = new TracksByAssetSearchRequestDto();
        SearchBranch query = new SearchBranch();
        query.addNewSearchLeaf(SearchFields.CFR, "Test");
        request.setAssetQuery(query);

       // System.out.println("Now");
       // Thread.sleep(1000 * 60 * 5);

        Response response = getWebTarget()
                .path("reports")
                .path("tracksByAssetSearch")
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
