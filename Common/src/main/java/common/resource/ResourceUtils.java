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

//    public static Resource getResourceTypesMinimumAllocation() {
//        Resource ret = Resource.newInstance(0, 0);
//        for (ResourceInformation entry : resourceTypesArray) {
//            String name = entry.getName();
//            if (name.equals(ResourceInformation.MEMORY_MB.getName())) {
//                ret.setMemorySize(entry.getMinimumAllocation());
//            } else if (name.equals(ResourceInformation.VCORES.getName())) {
//                Long tmp = entry.getMinimumAllocation();
//                if (tmp > Integer.MAX_VALUE) {
//                    tmp = (long) Integer.MAX_VALUE;
//                }
//                ret.setVirtualCores(tmp.intValue());
//            } else {
//                ret.setResourceValue(name, entry.getMinimumAllocation());
//            }
//        }
//        return ret;
//    }

}
