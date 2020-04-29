package agent.appworkmanage;

import common.event.AbstractEvent;

public class AppWorkManagerEvent extends AbstractEvent<AppWorkManagerEventType> {
    public AppWorkManagerEvent(AppWorkManagerEventType appWorkManagerEventType) {
        super(appWorkManagerEventType);
    }
}
