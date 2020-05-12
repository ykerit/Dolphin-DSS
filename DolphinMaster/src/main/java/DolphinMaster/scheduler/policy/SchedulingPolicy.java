package DolphinMaster.scheduler.policy;

import DolphinMaster.scheduler.FSContext;
import DolphinMaster.scheduler.ResourcePoolImp;
import DolphinMaster.scheduler.Schedulable;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.resource.ResourceCollector;

import java.util.Collection;
import java.util.Comparator;

public abstract class SchedulingPolicy {

    public static final SchedulingPolicy DEFAULT_POLICY = new FifoPolicy();

    public abstract Comparator<Schedulable> getComparator();

    public abstract ResourceCalculator getResourceCalculator();

    public abstract void computeShares(Collection<? extends Schedulable> schedulables,
                       Resource totalResources);

    public abstract void computeSteadyShares(Collection<? extends ResourcePoolImp> queues,
                             Resource totalResources);

    public abstract boolean checkIfUsageOverFairShare(Resource usage, Resource fairShare);

    public abstract Resource getHeadroom(Resource queueFairShare, Resource queueUsage,
                         Resource maxAvailable);

    public void initialize(FSContext fsContext) {}

    public boolean isChildPolicyAllowed() {
        return true;
    }
}
