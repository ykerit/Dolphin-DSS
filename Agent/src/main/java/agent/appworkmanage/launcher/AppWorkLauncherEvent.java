package agent.appworkmanage.launcher;

import agent.appworkmanage.AppWork;
import common.event.AbstractEvent;

public class AppWorkLauncherEvent extends AbstractEvent<AppWorkLauncherEventType> {
    private final AppWork appWork;

    public AppWorkLauncherEvent(AppWork appWork, AppWorkLauncherEventType eventType) {
        super(eventType);
        this.appWork = appWork;
    }

    public AppWork getAppWork() {
        return appWork;
    }
}
