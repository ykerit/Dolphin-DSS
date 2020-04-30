package agent.appworkmanage.appwork;

public enum AppWorkEventType {
    // AppWorkManage produce
    INIT_APP_WORK,
    KILL_APP_WORK,
    UPDATE_APP_WORK,
    REINITIALIZE_APP_WORK,
    ROLLBACK_REINIT,

    // Download
    APP_WORK_INITED,
    RESOURCE_LOCALIZED,
    RESOURCE_FAILED,
    RESOURCE_CLEANUP,

    // AppWork Launcher
    APP_WORK_LAUNCHED,
    APP_WORK_EXIT_WITH_SUCCESS,
    APP_WORK_EXIT_WITH_FAILURE,
    APP_WORK_EXIT_KILLED
}
