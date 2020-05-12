package DolphinMaster.scheduler;

import common.resource.Resource;
import common.struct.Priority;

public interface ResourcePool {
    String getPoolName();

    PoolMetrics getPoolMetrics();

    PoolInfo getPoolInfo(boolean includeChildPool, boolean recursive);

    void incPendingResource();

    void decPendingResource();

    Priority getApplicationPriority();

    void incReserveResource(String partition, Resource reserveRes);

    void decReserveResource(String partition, Resource reserveRes);
}
