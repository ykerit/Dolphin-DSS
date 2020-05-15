package DolphinMaster.node;

public enum NodeEventType {
    STARTED,

    // agent tracker
    STATUS_UPDATE,
    RESOURCE_UPDATE,

    // AppWork
    APP_WORK_ALLOCATED,
    CLEANUP_APP_WORK,
    UPDATE_APP_WORK,


    EXPIRE
}
