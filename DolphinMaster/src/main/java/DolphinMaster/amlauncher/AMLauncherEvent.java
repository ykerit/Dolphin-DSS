package DolphinMaster.amlauncher;

import common.event.AbstractEvent;

public class AMLauncherEvent extends AbstractEvent<AMLauncherEventType> {
    public AMLauncherEvent(AMLauncherEventType amLauncherEventType) {
        super(amLauncherEventType);
    }
}
