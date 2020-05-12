package DolphinMaster.scheduler;

import common.resource.Resources;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class StarvedApps {
    private PriorityBlockingQueue<AppDescribe> appsToProcess;
    private AppDescribe appBeingProcessed;

    StarvedApps() {
        appsToProcess = new PriorityBlockingQueue<>(10, new StarvationComparator());
    }

    void addStarvedApp(AppDescribe app) {
        if (!app.equals(appBeingProcessed) && !appsToProcess.contains(app)) {
            appsToProcess.add(app);
        }
    }

    AppDescribe take() throws InterruptedException {
        appBeingProcessed = null;
        AppDescribe app = appsToProcess.take();
        appBeingProcessed =app;
        return app;
    }

    private static class StarvationComparator implements Comparator<AppDescribe>, Serializable {

        @Override
        public int compare(AppDescribe o1, AppDescribe o2) {
            int ret = 1;
            if (Resources.fitsIn(o1.getStarvation(), o2.getStarvation())) {
                ret = -1;
            }
            return ret;
        }
    }
}
