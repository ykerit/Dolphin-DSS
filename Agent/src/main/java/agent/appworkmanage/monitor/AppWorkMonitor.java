package agent.appworkmanage.monitor;

import common.event.EventProcessor;
import common.service.Service;

public interface AppWorkMonitor extends Service, EventProcessor<AppWorkMonitorEvent> {

}
