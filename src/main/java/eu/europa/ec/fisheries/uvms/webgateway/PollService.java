package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.schema.mobileterminal.polltypes.v1.PollId;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.SanePollDto;
import eu.europa.ec.fisheries.uvms.exchange.client.ExchangeRestClient;
import eu.europa.ec.fisheries.uvms.movement.client.MovementRestClient;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;
import eu.europa.ec.fisheries.uvms.webgateway.dto.PollInfoDto;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class PollService {


    @Inject
    AssetClient assetClient;

    @Inject
    ExchangeRestClient exchangeClient;

    @Inject
    MovementRestClient movementClient;

    public Map<UUID, PollInfoDto> getPollInformationForAssetInTheLastDay(UUID assetId){
        List<SanePollDto> pollsForAsset = assetClient.getPollsForAssetInTheLastDay(assetId);
        Map<UUID, PollInfoDto> returnMap = new HashMap<>(pollsForAsset.size());

        for (SanePollDto pollDto : pollsForAsset) {
            ExchangeLogStatusType pollStatus = exchangeClient.getPollStatus(pollDto.getId().toString());
            MovementDto movement = null;
            if (pollStatus != null && pollStatus.getRelatedLogData() != null) {
                UUID movementId = UUID.fromString(pollStatus.getRelatedLogData().getRefGuid());
                movement = movementClient.getMovementById(movementId);
            }
            returnMap.put(pollDto.getId(), new PollInfoDto(pollDto, pollStatus, movement));
        }

        return returnMap;

    }

    public PollInfoDto getPollInfo(UUID pollId){
        SanePollDto pollInfo = assetClient.getPollInfo(pollId);
        if(pollInfo == null){
            return null;
        }

        ExchangeLogStatusType pollStatus = exchangeClient.getPollStatus(pollId.toString());
        MovementDto movement = null;
        if (pollStatus != null && pollStatus.getRelatedLogData() != null) {
            UUID movementId = UUID.fromString(pollStatus.getRelatedLogData().getRefGuid());
            movement = movementClient.getMovementById(movementId);
        }

        return new PollInfoDto(pollInfo, pollStatus, movement);
    }
}
