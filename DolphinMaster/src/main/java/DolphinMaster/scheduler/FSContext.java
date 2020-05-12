package DolphinMaster.scheduler;

import common.resource.Resource;

public class FSContext {
    private boolean preemptionEnabled = false;
    private float preemptionUtilizationThreshold;
    private StarvedApps starvedApps;
    private final FairScheduler scheduler;

    FSContext(FairScheduler scheduler) {
        this.scheduler = scheduler;
    }

    boolean isPreemptionEnabled() {
        return preemptionEnabled;
    }

    void setPreemptionEnabled() {
        this.preemptionEnabled = true;
        if (starvedApps == null) {
            starvedApps = new StarvedApps();
        }
    }

    StarvedApps getStarvedApps() {
        return starvedApps;
    }

    float getPreemptionUtilizationThreshold() {
        return preemptionUtilizationThreshold;
    }

    void setPreemptionUtilizationThreshold(
            float preemptionUtilizationThreshold) {
        this.preemptionUtilizationThreshold = preemptionUtilizationThreshold;
    }

    public Resource getClusterResource() {
        return scheduler.getClusterResource();
    }
}
