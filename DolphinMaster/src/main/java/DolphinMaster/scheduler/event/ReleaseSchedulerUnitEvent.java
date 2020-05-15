package DolphinMaster.scheduler.event;

import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import DolphinMaster.schedulerunit.SchedulerUnit;

public class ReleaseSchedulerUnitEvent extends SchedulerEvent {

    private final SchedulerUnit schedulerUnit;

    public ReleaseSchedulerUnitEvent(SchedulerUnit schedulerUnit) {
        super(SchedulerEventType.RELEASE_SCHEDULER_UNIT);
        this.schedulerUnit =schedulerUnit;
    }

    public SchedulerUnit getSchedulerUnit() {
        return schedulerUnit;
    }
}
