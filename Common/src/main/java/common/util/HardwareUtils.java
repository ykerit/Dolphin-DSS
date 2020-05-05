package common.util;

import common.exception.DolphinRuntimeException;
import common.resource.Resource;
import common.resource.ResourceCollector;
import common.resource.ResourceInformation;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class HardwareUtils {
    private static final Logger log = LogManager.getLogger("HardwareUtils");
    public static int getNodeCPUs(Configuration configuration) {
        ResourceCollector collector = new ResourceCollector();
        return getNodeCPUs(collector, configuration);
    }

    public static int getNodeCPUs(ResourceCollector collector, Configuration configuration) {
        int numProcesses = collector.getNumProcessors();
        boolean enableLogic = configuration.ENABLE_LOGICAL_PROCESSOR;
        if (!enableLogic) {
            numProcesses = collector.getNumCores();
        }
        return numProcesses;
    }

    public static float getAppWorksCPUs(Configuration configuration) {
        ResourceCollector collector = new ResourceCollector();
        return getAppWorksCPUs(collector, configuration);
    }

    public static float getAppWorksCPUs(ResourceCollector collector, Configuration configuration) {
        int numProcessors = getNodeCPUs(collector, configuration);
        int nodeCpuPercentage = getNodeCpuPercentage();
        return (nodeCpuPercentage * numProcessors) / 100.0f;
    }

    public static int getNodeCpuPercentage() {
        int nodeCpuPercentage = 100;
        return nodeCpuPercentage;
    }

    public static int getVCore(Configuration configuration) {
        ResourceCollector collector = new ResourceCollector();
        return getVCoresInternal(collector, configuration);
    }
    public static int getVCore(ResourceCollector collector, Configuration configuration) {
        return getVCoresInternal(collector, configuration);
    }

    private static int getVCoresInternal(ResourceCollector collector, Configuration configuration) {
        float physicalCores = getAppWorksCPUs(collector, configuration);
        float multiplier = configuration.DEFAULT_CORES_MULTIPLIER;
        int cores;
        if (multiplier > 0) {
            float tmp = physicalCores * multiplier;
            if (tmp > 0 && tmp < 1) {
                // on a single core machine - tmp can be between 0 and 1
                cores = 1;
            } else {
                cores = Math.round(tmp);
            }
        } else {
            throw new IllegalArgumentException("Hyper thread setting error");
        }
        if (cores <= 0) {
            throw new IllegalArgumentException("Cores number must be greater than 0");
        }
        return cores;
    }

    public static long getAppWorkMemoryMB(Configuration conf) {
        ResourceCollector plugin = new ResourceCollector();
        return getAppWorkMemoryMBInternal(plugin, conf);
    }

    private static long getAppWorkMemoryMBInternal(ResourceCollector plugin,
                                                     Configuration conf) {
        long memoryMb;
        long physicalMemoryMB = (plugin.getMemorySize() / (1024 * 1024));
        long HeapSizeMB = (Runtime.getRuntime().maxMemory()
                / (1024 * 1024));
        long containerPhysicalMemoryMB = (long) (0.8f
                * (physicalMemoryMB - (2 * HeapSizeMB)));
        long reservedMemoryMB = conf.SYSTEM_RESERVED_MEM_MB;
        if (reservedMemoryMB != -1) {
            containerPhysicalMemoryMB = physicalMemoryMB - reservedMemoryMB;
        }
        if (containerPhysicalMemoryMB <= 0) {
            log.error("Calculated memory for appWork is too low."
                    + " Node memory is " + physicalMemoryMB
                    + " MB, system reserved memory is " + reservedMemoryMB + " MB.");
        }
        containerPhysicalMemoryMB = Math.max(containerPhysicalMemoryMB, 0);
        memoryMb = containerPhysicalMemoryMB;
        if(memoryMb <= 0) {
            String message = "Illegal value for memory"
                    + ". Value must be greater than 0.";
            throw new IllegalArgumentException(message);
        }
        return memoryMb;
    }


    // not done
    public static Resource getNodeResources(Configuration configuration) {
        String memory = ResourceInformation.MEMORY_MB.getName();
        String vcores = ResourceInformation.VCORES.getName();

        Resource ret = Resource.newInstance(0, 0);
        return ret;
    }
}
