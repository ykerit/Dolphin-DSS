package DolphinMaster.scheduler;

import common.event.AbstractEvent;

public class SchedulerEvent extends AbstractEvent<SchedulerEventType> {
    public SchedulerEvent(SchedulerEventType schedulerEventType) {
        super(schedulerEventType);
    }
}
