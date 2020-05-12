package DolphinMaster.schedulerunit;

import common.event.AbstractEvent;

public class SchedulerUnitEvent extends AbstractEvent<SchedulerUnitEventType> {
    private final String appWorkId;
    public SchedulerUnitEvent(String appWorkId, SchedulerUnitEventType schedulerUnitEventType) {
        super(schedulerUnitEventType);
        this.appWorkId = appWorkId;
    }

    public String getAppWorkId() {
        return appWorkId;
    }
}
