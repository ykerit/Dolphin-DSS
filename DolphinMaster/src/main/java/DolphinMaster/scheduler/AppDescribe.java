package DolphinMaster.scheduler;

import common.resource.Resource;
import common.struct.Priority;

public class AppDescribe implements Schedulable {

    protected Resource fairShareStarvation() {
        return null;
    }

    public long getApplicationId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Resource getMinShare() {
        return null;
    }

    @Override
    public Resource getMaxShare() {
        return null;
    }

    @Override
    public float getWeight() {
        return 0;
    }

    @Override
    public Priority getPriority() {
        return null;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public Resource getResourceUsage() {
        return null;
    }

    @Override
    public Resource getDemand() {
        return null;
    }

    @Override
    public void updateDemand() {

    }

    @Override
    public Resource assignSchedulerUnit(FSSchedulerNode node) {
        return null;
    }

    @Override
    public void setFairShare(Resource fairShare) {

    }

    @Override
    public boolean isPreemptable() {
        return false;
    }

    public AppDescribeId getApplicationDescribeId() {
        return null;
    }

    public Resource getStarvation() {
        return null;
    }

    public Resource getPendingDemand() {
        return null;
    }

    public Resource getFairShareStarvation() {
        return null;
    }

    public void resetMinShareStarvation() {

    }

    public boolean shouldCheckForStarvation() {
        return true;
    }

    public boolean isPending() {
        return true;
    }

    public boolean isStarvedForFairShare() {
        return true;
    }
}
