package DolphinMaster.scheduler;

import common.resource.Resource;
import common.resource.Resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllocationConfiguration {
    private final Map<String, Resource> minPoolResources;
    final Map<String, ConfigurableResource> maxPoolResources;
    private final Map<String, ConfigurableResource> maxChildPoolResource;
    private final Map<String, Float> poolWeights;
    final Map<String, Integer> poolMaxApps;
    private final int poolMaxAppDefault;
    private final ConfigurableResource poolMaxResourceDefault;
    final Map<String, Float> poolMaxAMShares;
    private final float poolMaxAMShareDefault;
    private final Map<String, Long> minSharePreemptionTimeouts;
    private final Map<String, Long> fairSharePreemptionTimeouts;
    private final Map<String, Float> fairSharePreemptionThresholds;
    private final Set<String> reservablePools;
    private final Map<String, Resource> poolMaxSchedulerUnitAllocationMap;
    Map<PoolType, Set<String>> configuredPools;
    private final Set<String> nonPreemptablePools;


    public AllocationConfiguration(FairScheduler fairScheduler) {
        minPoolResources = new HashMap<>();
        maxChildPoolResource = new HashMap<>();
        maxPoolResources = new HashMap<>();
        poolWeights = new HashMap<>();
        poolMaxApps = new HashMap<>();
        poolMaxAMShares = new HashMap<>();
        poolMaxAppDefault = Integer.MAX_VALUE;
        poolMaxResourceDefault = new ConfigurableResource(Resources.unbounded());
        poolMaxAMShareDefault = 0.5f;
        minSharePreemptionTimeouts = new HashMap<>();
        fairSharePreemptionThresholds = new HashMap<>();
        fairSharePreemptionTimeouts = new HashMap<>();
        reservablePools = new HashSet<>();
        poolMaxSchedulerUnitAllocationMap = new HashMap<>();
        configuredPools = new HashMap<>();
        nonPreemptablePools = new HashSet<>();
    }

    public long getMinSharePreemptionTimeout(String poolName) {
        Long minSharePreemptionTimeout = minSharePreemptionTimeouts.get(poolName);
        return minSharePreemptionTimeout == null ? -1 : minSharePreemptionTimeout;
    }

    public long getFairSharePreemptionTimeout(String poolName) {
        Long fairSharePreemptionTimeout = fairSharePreemptionTimeouts.get(poolName);
        return fairSharePreemptionTimeout == null ? -1 : fairSharePreemptionTimeout;
    }

    public float getFairSharePreemptionThreshold(String poolName) {
        Float fairSharePreemptionThreshold= fairSharePreemptionThresholds.get(poolName);
        return fairSharePreemptionThreshold == null ? -1 : fairSharePreemptionThreshold;
    }

    public boolean isPreemptable(String poolName) {
        return !nonPreemptablePools.contains(poolName);
    }

    private float getPoolWeight(String poolName) {
        Float weight = poolWeights.get(poolName);
        return weight == null ? 1.0f : weight;
    }

    int getPoolMaxApps(String pool) {
        Integer maxApps = poolMaxApps.get(pool);
        return maxApps == null ? poolMaxAppDefault : maxApps;
    }

    public int getPoolMaxAppDefault() {
        return poolMaxAppDefault;
    }

    float getPoolMaxAMShare(String pool) {
        Float maxAmShare = poolMaxAMShares.get(pool);
        return maxAmShare == null ? poolMaxAMShareDefault : maxAmShare;
    }

    public float getPoolMaxAMShareDefault() {
        return poolMaxAMShareDefault;
    }

    Resource getMinResources(String pool) {
        Resource minResource = minPoolResources.get(pool);
        return minResource == null ? Resources.none() : minResource;
    }

    ConfigurableResource getMaxResources(String pool) {
        ConfigurableResource maxResource = maxPoolResources.get(pool);
        return maxResource == null ? poolMaxResourceDefault: maxResource;
    }

    Resource getPoolMaxSchedulerUnitAllocation(String pool) {
        Resource resource = poolMaxSchedulerUnitAllocationMap.get(pool);
        return resource == null ? Resources.unbounded() : resource;
    }

    ConfigurableResource getMaxChildResources(String pool) {
        return maxChildPoolResource.get(pool);
    }

    public Map<PoolType, Set<String>> getConfiguredPools() {
        return configuredPools;
    }

    public boolean isReservable(String pool) {
        return reservablePools.contains(pool);
    }

    public long getReservationWindow(String pool) {
        return 0;
    }

    public void initPool(ResourcePoolImp pool) {
        String name = pool.getName();
        pool.setWeight(getPoolWeight(name));
        pool.setMinShare(getMinResources(name));
        pool.setMaxShare(getMaxResources(name));
        pool.setMaxRunningApp(getPoolMaxApps(name));
        pool.setMaxAMShare(getPoolMaxAMShare(name));
        pool.setMaxChildQueueResource(getMaxChildResources(name));
        pool.setMaxSchedulerUnitAllocation(getPoolMaxSchedulerUnitAllocation(name));
    }
}
