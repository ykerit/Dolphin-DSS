package DolphinMaster.app;

public enum AppEventType {
    START,
    SCHEDULED,
    RECOVER,
    KILL,

    APP_REJECTED,

    // from: scheduler
    APP_ACCEPTED,

    // from: AppMasterLauncher
    LAUNCHED,
    LAUNCHED_FAILED,

    // from: AgentTracker
    APP_RUNNING_ON_NODE,

    // from: AMLivelinessMonitor
    EXPIRE,

    // from: AppWork
    APP_WORK_ALLOCATE,
    APP_WORK_FINISHED,

    // from AppMasterService
    AM_REGISTER,
    AM_UNREGISTER,
    STATUS_UPDATE,
}
