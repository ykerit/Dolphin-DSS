package agent.appworkmanage.appwork;

import common.event.AbstractEvent;

public class AppWorkEvent extends AbstractEvent<AppWorkEventType> {

    private final String appWorkId;

    public String getAppWorkId() {
        return appWorkId;
    }

    public AppWorkEvent(String appWorkId, AppWorkEventType appWorkEventType) {
        super(appWorkEventType);
        this.appWorkId = appWorkId;
    }
}
