package DolphinMaster.scheduler;

public enum  SchedulerEventType {
    // Source: Node
    NODE_ADDED,
    NODE_REMOVED,
    NODE_UPDATE,
    NODE_RESOURCE_UPDATE,
    NODE_LABELS_UPDATE,
    NODE_ATTRIBUTES_UPDATE,

    // Source: RMApp
    APP_ADDED,
    APP_REMOVED,

    // Source: ContainerAllocationExpirer
    CONTAINER_EXPIRED,

    // Source: SchedulerAppAttempt::pullNewlyUpdatedContainer.
    RELEASE_SCHEDULER_UNIT,

    /* Source: SchedulingEditPolicy */
    KILL_RESERVED_CONTAINER,

    // Mark a container for preemption
    MARK_CONTAINER_FOR_PREEMPTION,

    // Mark a for-preemption container killable
    MARK_CONTAINER_FOR_KILLABLE,

    // Cancel a killable container
    MARK_CONTAINER_FOR_NONKILLABLE,

    //Queue Management Change
    MANAGE_QUEUE
}
