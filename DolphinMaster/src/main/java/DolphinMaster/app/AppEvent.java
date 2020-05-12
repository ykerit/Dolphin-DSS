package DolphinMaster.app;

import common.event.AbstractEvent;
import common.struct.ApplicationId;

public class AppEvent extends AbstractEvent<AppEventType> {

    private final ApplicationId appId;
    private final String tips;

    public AppEvent(ApplicationId appId, AppEventType appEventType) {
        this(appId, appEventType,"");
    }

    public AppEvent(ApplicationId appId, AppEventType appEventType, String tips) {
        super(appEventType);
        this.appId = appId;
        this.tips = tips;
    }

    public AppEvent(ApplicationId appId, AppEventType appEventType, long timestamp) {
        super(appEventType, timestamp);
        this.appId = appId;
        this.tips = "";
    }

    public ApplicationId getAppId() {
        return appId;
    }

    public String getTips() {
        return tips;
    }
}
