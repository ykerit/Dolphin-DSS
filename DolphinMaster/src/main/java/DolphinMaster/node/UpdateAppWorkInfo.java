package DolphinMaster.node;

import agent.status.AppWorkStatus;
import common.struct.ApplicationId;

import java.util.List;
import java.util.Map;

public class UpdateAppWorkInfo {
    private List<AppWorkStatus> newlyLaunchedAppWorks;
    private List<AppWorkStatus> completedAppWorks;
    private List<Map.Entry<ApplicationId, AppWorkStatus>> updateAppWorks;

    public UpdateAppWorkInfo() {

    }

    public UpdateAppWorkInfo(List<AppWorkStatus> newlyLaunchedAppWorks,
                             List<AppWorkStatus> completedAppWorks,
                             List<Map.Entry<ApplicationId, AppWorkStatus>> updateAppWorks) {
        this.newlyLaunchedAppWorks = newlyLaunchedAppWorks;
        this.completedAppWorks = completedAppWorks;
        this.updateAppWorks = updateAppWorks;
    }

    public List<AppWorkStatus> getCompletedAppWorks() {
        return completedAppWorks;
    }

    public List<AppWorkStatus> getNewlyLaunchedAppWorks() {
        return newlyLaunchedAppWorks;
    }

    public List<Map.Entry<ApplicationId, AppWorkStatus>> getUpdateAppWorks() {
        return updateAppWorks;
    }
}
