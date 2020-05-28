package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.webgateway.dto.SearchBranchDeserializer;

public class JsonBConfiguratorWebGateway extends JsonBConfigurator {

    public JsonBConfiguratorWebGateway() {
        super();
        config.withDeserializers(new SearchBranchDeserializer());
    }
}
