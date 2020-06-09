package DolphinMaster.node;

public enum NodeEventType {
    STARTED,

    // from: agent tracker
    STATUS_UPDATE,
    RESOURCE_UPDATE,
    RECONNECTED,
    SHUTDOWN,
    REBOOTING,

    // from: App
    FINISHED_APP_WORKS_PULLED_BY_AM,
    CLEANUP_APP,

    // from: AppWork
    APP_WORK_ALLOCATED,
    CLEANUP_APP_WORK,
    UPDATE_APP_WORK,


    EXPIRE
}
