package common.resource;

import config.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceUtils {
    private static volatile boolean initializeNodeResource = false;
    private static volatile Map<String, ResourceInformation> readOnlyNodeResources;

    private ResourceUtils() {}

    public static Map<String, ResourceInformation> getNodeResourceInformation(Configuration configuration) {
        if (!initializeNodeResource) {
            synchronized (ResourceUtils.class) {
                if (!initializeNodeResource) {
                    Map<String, ResourceInformation> nodeResource = initializeResourceInformation(configuration);
                    readOnlyNodeResources = Collections.unmodifiableMap(nodeResource);
                    initializeNodeResource = true;
                }
            }
        }
        return readOnlyNodeResources;
    }

    private static Map<String, ResourceInformation> initializeResourceInformation(Configuration configuration) {
        Map<String, ResourceInformation> ret = new HashMap<>();
        return ret;
    }


}
