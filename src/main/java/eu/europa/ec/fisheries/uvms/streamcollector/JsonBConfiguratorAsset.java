package eu.europa.ec.fisheries.uvms.streamcollector;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.streamcollector.dto.SearchBranchDeserializer;

public class JsonBConfiguratorAsset extends JsonBConfigurator {

    public JsonBConfiguratorAsset() {
        super();
        config.withDeserializers(new SearchBranchDeserializer());
    }
}
