package agent.appworkmanage;

import agent.appworkmanage.monitor.AppWorkMonitor;
import agent.appworkmanage.scheduler.AppWorkScheduler;
import common.event.EventProcessor;

public interface AppWorkManager extends EventProcessor<AppWorkManagerEvent> {
    AppWorkMonitor getAppWorkMonitor();

    AppWorkScheduler getAppWorkScheduler();
}
