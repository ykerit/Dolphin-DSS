package DolphinMaster.scheduler.policy;

import DolphinMaster.scheduler.FSContext;
import DolphinMaster.scheduler.ResourcePoolImp;
import DolphinMaster.scheduler.Schedulable;
import common.resource.DefaultResourceCalculator;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.resource.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

public class FifoPolicy extends SchedulingPolicy {
    private static final Logger LOG = LogManager.getLogger(FifoPolicy.class);

    public static final String NAME = "FIFO";
    private static final FifoComparator COMPARATOR = new FifoComparator();
    private static final DefaultResourceCalculator CALCULATOR =
            new DefaultResourceCalculator();

    /**
     * Compare Schedulables in order of priority and then submission time, as in
     * the default FIFO scheduler in Hadoop.
     */
    static class FifoComparator implements Comparator<Schedulable>, Serializable {

        @Override
        public int compare(Schedulable s1, Schedulable s2) {
            int res = s1.getPriority().compareTo(s2.getPriority());
            if (res == 0) {
                res = (int) Math.signum(s1.getStartTime() - s2.getStartTime());
            }
            if (res == 0) {
                // In the rare case where jobs were submitted at the exact same time,
                // compare them by name (which will be the JobID) to get a deterministic
                // ordering, so we don't alternately launch tasks from different jobs.
                res = s1.getName().compareTo(s2.getName());
            }
            return res;
        }
    }

    @Override
    public void initialize(FSContext fsContext) {

    }

    @Override
    public Comparator<Schedulable> getComparator() {
        return COMPARATOR;
    }

    @Override
    public ResourceCalculator getResourceCalculator() {
        return CALCULATOR;
    }

    @Override
    public void computeShares(Collection<? extends Schedulable> schedulables,
                              Resource totalResources) {
        if (schedulables.isEmpty()) {
            return;
        }

        Schedulable earliest = null;
        for (Schedulable schedulable : schedulables) {
            if (earliest == null ||
                    schedulable.getStartTime() < earliest.getStartTime()) {
                earliest = schedulable;
            }
        }

        if (earliest != null) {
            earliest.setFairShare(Resources.clone(totalResources));
        }
    }

    @Override
    public void computeSteadyShares(Collection<? extends ResourcePoolImp> queues,
                                    Resource totalResources) {
        // Nothing needs to do, as leaf queue doesn't have to calculate steady
        // fair shares for applications.
    }

    @Override
    public boolean checkIfUsageOverFairShare(Resource usage, Resource fairShare) {
        throw new UnsupportedOperationException(
                "FifoPolicy doesn't support checkIfUsageOverFairshare operation, " +
                        "as FifoPolicy only works for FSLeafQueue.");
    }

    @Override
    public Resource getHeadroom(Resource queueFairShare,
                                Resource queueUsage, Resource maxAvailable) {
        long queueAvailableMemory = Math.max(
                queueFairShare.getMemorySize() - queueUsage.getMemorySize(), 0);
        Resource headroom = Resources.createResource(
                Math.min(maxAvailable.getMemorySize(), queueAvailableMemory),
                maxAvailable.getVCore());
        return headroom;
    }

    @Override
    public boolean isChildPolicyAllowed() {
        return false;
    }
}
