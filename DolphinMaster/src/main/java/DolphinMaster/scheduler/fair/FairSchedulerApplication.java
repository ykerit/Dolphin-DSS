package DolphinMaster.scheduler.fair;

import DolphinMaster.DolphinContext;
import DolphinMaster.scheduler.FSSchedulerNode;
import DolphinMaster.scheduler.ResourcePool;
import DolphinMaster.scheduler.Schedulable;
import DolphinMaster.scheduler.SchedulerApplication;
import common.resource.Resource;
import common.struct.ApplicationId;
import common.struct.Priority;

public class FairSchedulerApplication extends SchedulerApplication implements Schedulable {
    public FairSchedulerApplication(ApplicationId applicationId, String user, ResourcePool pool, DolphinContext context) {
        super(applicationId, user, pool, context);
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

    public boolean isPending() {
        return false;
    }
    public Resource fairShareStarvation() {
        return null;
    }

    public Resource getStarvation() {
        return null;
    }

    public boolean isStarvedForFairShare() {
        return true;
    }

    public Resource getPendingDemand() {
        return null;
    }

    public boolean shouldCheckForStarvation() {
        return true;
    }

    public Resource getFairShareStarvation() {
        return null;
    }

    public void resetMinShareStarvation() {}

}
