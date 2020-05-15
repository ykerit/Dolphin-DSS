package DolphinMaster.scheduler;

import DolphinMaster.schedulerunit.SchedulerUnit;
import common.resource.Resource;
import common.struct.Priority;

public interface ResourcePool {
    String getPoolName();

    PoolMetrics getPoolMetrics();

    PoolInfo getPoolInfo(boolean includeChildPool, boolean recursive);

    void recoverAppWork(Resource clusterResource, SchedulerApplication schedulerApplication, SchedulerUnit schedulerUnit);

    void incPendingResource(String nodeLabel, Resource resourceToInc);

    void decPendingResource(String nodeLabel, Resource resourceToDec);

    Priority getApplicationPriority();

    void incReserveResource(String partition, Resource reserveRes);

    void decReserveResource(String partition, Resource reserveRes);
}
