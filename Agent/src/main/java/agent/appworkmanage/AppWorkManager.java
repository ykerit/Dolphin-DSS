package agent.appworkmanage;

import agent.appworkmanage.monitor.AppWorkMonitor;
import api.app_master_message.*;
import common.event.EventProcessor;
import common.exception.DolphinException;

import java.io.IOException;

public interface AppWorkManager extends EventProcessor<AppWorkManagerEvent> {
    AppWorkMonitor getAppWorkMonitor();

    StartAppWorksResponse startAppWorks(StartAppWorksRequest requests) throws DolphinException, IOException;

    StopAppWorkResponse stopAppWorks(StopAppWorkRequest request) throws DolphinException, IOException;

    GetAppWorkStatusesResponse getAppWorkStatuses(GetAppWorkStatusesRequest request) throws DolphinException, IOException;
}
