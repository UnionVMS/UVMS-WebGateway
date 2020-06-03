package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovementExtended;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MicroMovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.UUID;

@Path("movement/rest/internal")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class MovementModuleMock {

    @POST
    @Path("/microMovementsForConnectIdsBetweenDates")
    public Response getMicroMovementsForConnectIdsBetweenDates(MicroMovementsForConnectIdsBetweenDatesRequest request){
        MicroMovementExtended mme = new MicroMovementExtended();
        mme.setAsset("Movement Module Mock");
        MicroMovement mm = new MicroMovement();
        mm.setId(UUID.randomUUID().toString());
        mm.setHeading(0.0);
        mm.setSpeed(0.0);
        mm.setSource(MovementSourceType.OTHER);
        mm.setTimestamp(Instant.now());

        MovementPoint mp = new MovementPoint();
        mp.setLatitude(1.0);
        mp.setLongitude(2.0);
        mm.setLocation(mp);

        mme.setMicroMove(mm);

        return Response.ok(mme).build();
    }

    @GET
    @Path("/getMicroMovement/{movementId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMicroMovementById(@PathParam("movementId") UUID movementId) {
        MicroMovement micro = new MicroMovement();
        micro.setId(movementId.toString());
        micro.setHeading(0.5);
        micro.setSource(MovementSourceType.MANUAL);
        micro.setSpeed(55.5);
        micro.setTimestamp(Instant.now());

        MovementPoint point = new MovementPoint();
        point.setLatitude(5.5);
        point.setLongitude(55.5);
        micro.setLocation(point);

        return Response.ok(micro).build();
    }
}
