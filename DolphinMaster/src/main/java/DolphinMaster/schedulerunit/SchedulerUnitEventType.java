package DolphinMaster.schedulerunit;

public enum SchedulerUnitEventType {
    START,
    ACQUIRED,
    KILL,
    RESERVED,

    ACQUIRED_UPDATE_APP_WORK,
    LAUNCHED,
    FINISHED,

    RELEASED,
    CHANGE_RESOURCE
}
