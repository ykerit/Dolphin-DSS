package DolphinMaster.schedulerunit;

import common.event.AbstractEvent;
import common.struct.AppWorkId;

public class SchedulerUnitEvent extends AbstractEvent<SchedulerUnitEventType> {
    private final AppWorkId appWorkId;
    public SchedulerUnitEvent(AppWorkId appWorkId, SchedulerUnitEventType schedulerUnitEventType) {
        super(schedulerUnitEventType);
        this.appWorkId = appWorkId;
    }

    public AppWorkId getAppWorkId () {
        return appWorkId;
    }
}
