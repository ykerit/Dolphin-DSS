package agent.appworkmanage.launcher;

import agent.appworkmanage.appwork.AppWork;
import common.event.AbstractEvent;

public class AppWorkLauncherPoolEvent extends AbstractEvent<AppWorkLauncherPoolEventType> {
    private final AppWork appWork;

    public AppWorkLauncherPoolEvent(AppWork appWork, AppWorkLauncherPoolEventType eventType) {
        super(eventType);
        this.appWork = appWork;
    }

    public AppWork getAppWork() {
        return appWork;
    }
}
