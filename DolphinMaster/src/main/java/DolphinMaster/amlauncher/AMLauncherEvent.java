package DolphinMaster.amlauncher;

import DolphinMaster.app.AppImp;
import common.event.AbstractEvent;

public class AMLauncherEvent extends AbstractEvent<AMLauncherEventType> {

    private final AppImp app;

    public AMLauncherEvent(AMLauncherEventType amLauncherEventType, AppImp app) {
        super(amLauncherEventType);
        this.app = app;
    }

    public AppImp getApp() {
        return app;
    }
}
