package DolphinMaster.scheduler.event;

import DolphinMaster.app.AppState;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import common.struct.ApplicationId;

public class AppRemovedSchedulerEvent extends SchedulerEvent {
    private final ApplicationId applicationId;
    private final AppState appState;
    public AppRemovedSchedulerEvent(ApplicationId applicationId, AppState state) {
        super(SchedulerEventType.APP_REMOVED);
        this.applicationId = applicationId;
        this.appState = state;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public AppState getAppState() {
        return appState;
    }
}
