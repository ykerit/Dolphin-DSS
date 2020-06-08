package agent.appworkmanage.appwork;

import agent.appworkmanage.Localize.LocalResourceRequest;
import common.struct.AppWorkId;

public class AppWorkResourceEvent extends AppWorkEvent {
    private final LocalResourceRequest resourceRequest;
    public AppWorkResourceEvent(AppWorkId appWorkId, AppWorkEventType appWorkEventType, LocalResourceRequest resourceRequest) {
        super(appWorkId, appWorkEventType);
        this.resourceRequest = resourceRequest;
    }

    public LocalResourceRequest getResourceRequest() {
        return resourceRequest;
    }
}
