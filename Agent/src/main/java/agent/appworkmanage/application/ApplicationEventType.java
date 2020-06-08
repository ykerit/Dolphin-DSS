package agent.appworkmanage.application;

public enum  ApplicationEventType {
    // Source: AppWorkManager
    INIT_APPLICATION,
    INIT_APP_WORK,
    FINISH_APPLICATION,
    FINISH_APPLICATION_NONE,

    // Source: ResourceLocalizationService
    APPLICATION_INITED,
    APPLICATION_RESOURCES_CLEANEDUP,

    // Source: Container
    APPLICATION_APP_WORK_FINISHED,
}
