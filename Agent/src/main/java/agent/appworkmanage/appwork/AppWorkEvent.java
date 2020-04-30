package agent.appworkmanage.appwork;

import common.event.AbstractEvent;

public class AppWorkEvent extends AbstractEvent<AppWorkEventType> {
    public AppWorkEvent(AppWorkEventType appWorkEventType) {
        super(appWorkEventType);
    }
}
