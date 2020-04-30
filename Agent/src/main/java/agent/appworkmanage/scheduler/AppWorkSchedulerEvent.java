package agent.appworkmanage.scheduler;

import agent.appworkmanage.appwork.AppWork;
import common.event.AbstractEvent;

public class AppWorkSchedulerEvent extends AbstractEvent<AppWorkSchedulerEventType> {

    private final AppWork appWork;

    public AppWorkSchedulerEvent(AppWork appWork, AppWorkSchedulerEventType appWorkSchedulerEventType) {
        super(appWorkSchedulerEventType);
        this.appWork = appWork;
    }

    public AppWork getAppWork() {
        return appWork;
    }
}
