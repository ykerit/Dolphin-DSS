package agent.appworkmanage.monitor;

import common.event.EventProcessor;
import common.resource.ResourceUtilization;
import common.service.Service;

public interface AppWorkMonitor extends Service, EventProcessor<AppWorkMonitorEvent> {
    ResourceUtilization getAppWorkUtilization();
}
