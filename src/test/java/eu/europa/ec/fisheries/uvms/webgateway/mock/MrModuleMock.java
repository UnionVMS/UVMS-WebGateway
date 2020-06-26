package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/movement-rules/rest/")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class MrModuleMock {

    @DELETE
    @Path("previousReports/byAsset/{assetGuid}")
    @RequiresFeature(UnionVMSFeature.manageGlobalAlarmsRules)
    public Response deletePreviousReportByAssetGuid(@PathParam("assetGuid") String assetGuid){
        System.setProperty("MR_MODULE_REACHED", "true");
        return Response.ok().build();
    }



}
