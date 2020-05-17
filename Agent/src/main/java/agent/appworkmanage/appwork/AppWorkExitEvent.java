package agent.appworkmanage.appwork;

import common.struct.AppWorkId;

public class AppWorkExitEvent extends AppWorkEvent {
    private int exitCode;
    private final String tips;
    public AppWorkExitEvent(AppWorkId appWorkId, AppWorkEventType appWorkEventType, int exitCode, String tips) {
        super(appWorkId, appWorkEventType);
        this.exitCode = exitCode;
        this.tips = tips;
    }

    public String getTips() {
        return tips;
    }

    public int getExitCode() {
        return exitCode;
    }
}
