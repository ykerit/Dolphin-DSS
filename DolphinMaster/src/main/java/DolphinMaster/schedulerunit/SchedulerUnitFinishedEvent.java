package DolphinMaster.schedulerunit;

import common.struct.AppWorkStatus;
import common.struct.AppWorkId;

public class SchedulerUnitFinishedEvent extends SchedulerUnitEvent {
    private final AppWorkStatus appWorkStatus;

    public SchedulerUnitFinishedEvent(AppWorkId appWorkId, AppWorkStatus appWorkStatus, SchedulerUnitEventType schedulerUnitEventType) {
        super(appWorkId, schedulerUnitEventType);
        this.appWorkStatus = appWorkStatus;
    }

    public AppWorkStatus getAppWorkStatus() {
        return appWorkStatus;
    }
}
