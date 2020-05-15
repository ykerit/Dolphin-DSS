package DolphinMaster.scheduler;

import DolphinMaster.scheduler.fair.FairSchedulerApplication;
import common.resource.Resources;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class StarvedApps {
    private PriorityBlockingQueue<FairSchedulerApplication> appsToProcess;
    private FairSchedulerApplication appBeingProcessed;

    StarvedApps() {
        appsToProcess = new PriorityBlockingQueue<>(10, new StarvationComparator());
    }

    void addStarvedApp(FairSchedulerApplication app) {
        if (!app.equals(appBeingProcessed) && !appsToProcess.contains(app)) {
            appsToProcess.add(app);
        }
    }

    FairSchedulerApplication take() throws InterruptedException {
        appBeingProcessed = null;
        FairSchedulerApplication app = appsToProcess.take();
        appBeingProcessed =app;
        return app;
    }

    private static class StarvationComparator implements Comparator<FairSchedulerApplication>, Serializable {

        @Override
        public int compare(FairSchedulerApplication o1, FairSchedulerApplication o2) {
            int ret = 1;
            if (Resources.fitsIn(o1.getStarvation(), o2.getStarvation())) {
                ret = -1;
            }
            return ret;
        }
    }
}
