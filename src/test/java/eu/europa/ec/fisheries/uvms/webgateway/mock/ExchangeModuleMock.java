package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.exchange.v1.*;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.client.model.AssetListResponse;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Path("/exchange/rest")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class ExchangeModuleMock {

    @GET
    @Path(value = "exchange/poll/{typeRefGuid}")
    public Response getPollStatus(@PathParam("typeRefGuid") String typeRefGuid) {
        if (typeRefGuid == null) {
            throw new IllegalArgumentException("Invalid id");
        }
        ExchangeLogStatusType response = new ExchangeLogStatusType();
        response.setGuid(UUID.randomUUID().toString());
        response.setIdentifier("Log recipient");

        LogRefType logRefType = new LogRefType();
        logRefType.setMessage("LogRefTyp message");
        logRefType.setRefGuid(typeRefGuid);
        logRefType.setType(TypeRefType.POLL);
        response.setTypeRef(logRefType);

        response.getHistory().add(createExchangeLogStatusHistory(ExchangeLogStatusTypeType.SENT));
        response.getHistory().add(createExchangeLogStatusHistory(ExchangeLogStatusTypeType.PENDING));
        response.getHistory().add(createExchangeLogStatusHistory(ExchangeLogStatusTypeType.SUCCESSFUL));

        return Response.ok(response).build();
    }

    private ExchangeLogStatusHistoryType createExchangeLogStatusHistory(ExchangeLogStatusTypeType status){
        ExchangeLogStatusHistoryType statusHistory = new ExchangeLogStatusHistoryType();
        statusHistory.setStatus(status);
        statusHistory.setTimestamp(new Date());

        return statusHistory;
    }

}
