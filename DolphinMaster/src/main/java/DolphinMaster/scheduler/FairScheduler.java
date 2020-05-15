package DolphinMaster.scheduler;


import common.resource.DefaultResourceCalculator;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.resource.Resources;
import common.util.SystemClock;

import java.util.Comparator;

public class FairScheduler {

    private FSContext context;
    private Resource incAllocation;
    private PoolManager poolManager;

    private static final ResourceCalculator RESOURCE_CALCULATOR = new DefaultResourceCalculator();

    public static final Resource SCHEDULER_UNIT_RESERVED = Resources.createResource(-1);

    private final int UPDATE_DEBUG_FREQUENCY = 25;
    private int updateToSkipForDebuf = UPDATE_DEBUG_FREQUENCY;

    Thread schedulingThread;
    Thread preemptionThread;

    private float reservableNodesRatio;

    protected boolean sizeBasedWeight;
    protected boolean continuousSchedulingSleepMs;
    private Comparator<FSSchedulerNode> nodeAvailableResourceComparator;

    public Resource getClusterResource() {
        return null;
    }

    FSContext getContext() {
        return null;
    }

    SystemClock getClock() {
        return SystemClock.getInstance();
    }

    public PoolManager getPoolManager() {
        return null;
    }

    AllocationConfiguration getAllocationConfiguration() {
        return null;
    }
}
