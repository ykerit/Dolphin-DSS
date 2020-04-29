package agent.appworkmanage;

import agent.appworkmanage.monitor.AppWorkMonitor;
import common.event.EventProcessor;

public interface AppWorkManager extends EventProcessor<AppWorkManagerEvent> {
    AppWorkMonitor getAppWorkMonitor();
}
