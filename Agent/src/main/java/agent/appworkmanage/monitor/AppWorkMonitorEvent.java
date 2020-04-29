package agent.appworkmanage.monitor;

import common.event.AbstractEvent;

public class AppWorkMonitorEvent extends AbstractEvent<AppWorkMonitorEventType> {
    public AppWorkMonitorEvent(AppWorkMonitorEventType appWorkMonitorEventType) {
        super(appWorkMonitorEventType);
    }
}
