package eu.europa.ec.fisheries.uvms.webgateway.dto;

import eu.europa.ec.fisheries.uvms.asset.client.model.AssetStatistics;

public class UvmsStatistics {
    AssetStatistics assetStatistics;

    public AssetStatistics getAssetStatistics() {
        return assetStatistics;
    }

    public void setAssetStatistics(AssetStatistics assetStatistics) {
        this.assetStatistics = assetStatistics;
    }
}
