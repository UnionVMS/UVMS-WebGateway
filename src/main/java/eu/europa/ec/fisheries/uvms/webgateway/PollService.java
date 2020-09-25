package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.uvms.asset.client.AssetClient;
import eu.europa.ec.fisheries.uvms.asset.client.model.SanePollDto;
import eu.europa.ec.fisheries.uvms.exchange.client.ExchangeRestClient;
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


    public Map<UUID, PollInfoDto> getPollInformationForAssetInTheLastDay(UUID assetId){
        List<SanePollDto> pollsForAsset = assetClient.getPollsForAssetInTheLastDay(assetId);
        Map<UUID, PollInfoDto> returnMap = new HashMap<>(pollsForAsset.size());

        for (SanePollDto pollDto : pollsForAsset) {
            ExchangeLogStatusType pollStatus = exchangeClient.getPollStatus(pollDto.getId().toString());
            returnMap.put(pollDto.getId(), new PollInfoDto(pollDto, pollStatus));
        }

        return returnMap;

    }
}
