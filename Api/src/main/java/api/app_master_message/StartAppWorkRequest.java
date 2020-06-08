package api.app_master_message;

import common.context.AppWorkLaunchContext;
import common.struct.AppWorkId;

import java.io.Serializable;

public class StartAppWorkRequest implements Serializable {
    private final AppWorkLaunchContext appWorkLaunchContext;
    private final AppWorkId appWorkId;
    private final String applicationSubmitter;

    public StartAppWorkRequest(AppWorkId appWorkId, AppWorkLaunchContext context, String submitter) {
        this.appWorkId = appWorkId;
        appWorkLaunchContext = context;
        this.applicationSubmitter = submitter;
    }

    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return appWorkLaunchContext;
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }

    public String getApplicationSubmitter() {
        return applicationSubmitter;
    }
}
