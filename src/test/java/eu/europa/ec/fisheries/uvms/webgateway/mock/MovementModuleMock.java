package eu.europa.ec.fisheries.uvms.webgateway.mock;

import eu.europa.ec.fisheries.schema.movement.v1.MovementPoint;
import eu.europa.ec.fisheries.schema.movement.v1.MovementSourceType;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementsForConnectIdsBetweenDatesRequest;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

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
    @Path("/movementsForConnectIdsBetweenDates")
    public Response getMicroMovementsForConnectIdsBetweenDates(MovementsForConnectIdsBetweenDatesRequest request){
        MovementDto mm = new MovementDto();
        mm.setAsset("Movement Module Mock");
        mm.setId(UUID.randomUUID());
        mm.setHeading(0.0f);
        mm.setSpeed(0.0f);
        mm.setSource(MovementSourceType.OTHER);
        mm.setTimestamp(Instant.now());

        MovementPoint mp = new MovementPoint();
        mp.setLatitude(1.0);
        mp.setLongitude(2.0);
        mm.setLocation(mp);

        return Response.ok(mm).build();
    }

    @GET
    @Path("/getMovement/{movementId}")
    @RequiresFeature(UnionVMSFeature.manageInternalRest)
    public Response getMicroMovementById(@PathParam("movementId") UUID movementId) {
        MovementDto micro = new MovementDto();
        micro.setId(movementId);
        micro.setHeading(0.5f);
        micro.setSource(MovementSourceType.MANUAL);
        micro.setSpeed(55.5f);
        micro.setTimestamp(Instant.now());

        MovementPoint point = new MovementPoint();
        point.setLatitude(5.5);
        point.setLongitude(55.5);
        micro.setLocation(point);

        return Response.ok(micro).build();
    }
}
