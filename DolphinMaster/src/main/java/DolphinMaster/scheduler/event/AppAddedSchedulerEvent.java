package DolphinMaster.scheduler.event;

import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import common.struct.ApplicationId;
import common.struct.Priority;

public class AppAddedSchedulerEvent extends SchedulerEvent {
    private final ApplicationId applicationId;
    private final String pool;
    private final String user;
    private final Priority priority;


    public AppAddedSchedulerEvent(ApplicationId applicationId, String pool,
                                  String user, Priority priority) {
        super(SchedulerEventType.APP_ADDED);
        this.applicationId = applicationId;
        this.pool = pool;
        this.user = user;
        this.priority = priority;
    }

    public String getUser() {
        return user;
    }

    public Priority getPriority() {
        return priority;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public String getPool() {
        return pool;
    }
}
