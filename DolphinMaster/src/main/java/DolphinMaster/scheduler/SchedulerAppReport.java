package DolphinMaster.scheduler;

import DolphinMaster.app.App;
import DolphinMaster.schedulerunit.SchedulerUnit;

import java.util.Collection;

public class SchedulerAppReport {
    private final Collection<SchedulerUnit> live;
    private final Collection<SchedulerUnit> reserved;
    private final boolean pending;

    public SchedulerAppReport(SchedulerApplication app) {
        this.live = null;
        this.reserved = null;
        this.pending = false;
    }

    public Collection<SchedulerUnit> getLiveAppWorks() {
        return live;
    }

    public Collection<SchedulerUnit> getReservedAppWorks() {
        return reserved;
    }

    public boolean isPending() {
        return pending;
    }
}
