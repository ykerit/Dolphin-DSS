package DolphinMaster.node;

import common.struct.AppWorkStatus;
import common.struct.ApplicationId;
import common.struct.Pair;

import java.util.List;
import java.util.Map;

public class UpdateAppWorkInfo {
    private List<AppWorkStatus> newlyLaunchedAppWorks;
    private List<AppWorkStatus> completedAppWorks;
    private List<Pair<ApplicationId, AppWorkStatus>> updateAppWorks;

    public UpdateAppWorkInfo() {

    }

    public UpdateAppWorkInfo(List<AppWorkStatus> newlyLaunchedAppWorks,
                             List<AppWorkStatus> completedAppWorks,
                             List<Pair<ApplicationId, AppWorkStatus>> updateAppWorks) {
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

    public List<Pair<ApplicationId, AppWorkStatus>> getUpdateAppWorks() {
        return updateAppWorks;
    }
}
