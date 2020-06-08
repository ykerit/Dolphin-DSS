package agent.appworkmanage.application;

import common.struct.AppWorkStatus;

public class ApplicationAppWorkFinishedEvent extends ApplicationEvent {
    private final AppWorkStatus appWorkStatus;
    private final long appWorkStartTime;

    public ApplicationAppWorkFinishedEvent(AppWorkStatus appWorkStatus, long appWorkStartTime) {
        super(appWorkStatus.getAppWorkId().getApplicationId(), ApplicationEventType.APPLICATION_APP_WORK_FINISHED);
        this.appWorkStartTime = appWorkStartTime;
        this.appWorkStatus = appWorkStatus;
    }

    public AppWorkStatus getAppWorkStatus() {
        return appWorkStatus;
    }

    public long getAppWorkStartTime() {
        return appWorkStartTime;
    }
}
