package agent.appworkmanage.appwork;

public enum AppWorkState {
    NEW,
    LOCALIZING,
    LOCALIZED,
    LOCALIZATION_FAILED,
    SCHEDULED,
    RUNNING,
    RELAUNCHING,
    REINITIALIZING,
    REINITIALIZING_AWAITING_KILL,
    EXITED_WITH_SUCCESS,
    EXITED_WITH_FAILURE,
    KILLING,
    APP_WORK_CLEANUP_AFTER_KILL,
    DONE
}
