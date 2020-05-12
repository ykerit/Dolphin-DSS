package DolphinMaster.scheduler;

import common.resource.Resource;
import common.struct.Priority;

public interface Schedulable {
    String getName();

    Resource getMinShare();

    Resource getMaxShare();

    float getWeight();

    Priority getPriority();

    long getStartTime();

    Resource getResourceUsage();

    Resource getDemand();

    void updateDemand();

    Resource assignSchedulerUnit(FSSchedulerNode node);

    void setFairShare(Resource fairShare);

    boolean isPreemptable();
}
