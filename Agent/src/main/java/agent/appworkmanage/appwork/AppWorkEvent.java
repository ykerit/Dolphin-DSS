package agent.appworkmanage.appwork;

import common.event.AbstractEvent;
import common.struct.AppWorkId;

public class AppWorkEvent extends AbstractEvent<AppWorkEventType> {

    private final AppWorkId appWorkId;

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public AppWorkEvent(AppWorkId appWorkId, AppWorkEventType appWorkEventType) {
        super(appWorkEventType);
        this.appWorkId = appWorkId;
    }
}
