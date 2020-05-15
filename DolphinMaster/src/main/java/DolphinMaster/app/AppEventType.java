package DolphinMaster.app;

public enum AppEventType {
    START,
    RECOVER,
    KILL,

    APP_REJECTED,
    APP_ACCEPTED,

    APP_RUNNING_ON_NODE,

    EXPIRE,

    // AppWork
    APP_WORK_ALLOCATE,
    APP_WORK_FINISHED,

    // AppMaster Service
    REGISTER,
    STATUS_UPDATE,
    UNREGISTER
}
