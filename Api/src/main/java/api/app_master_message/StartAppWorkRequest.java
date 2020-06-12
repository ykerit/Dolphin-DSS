package api.app_master_message;

import common.context.AppWorkLaunchContext;
import common.struct.AppWorkId;
import common.struct.RemoteAppWork;

import java.io.Serializable;

public class StartAppWorkRequest implements Serializable {
    private final AppWorkLaunchContext appWorkLaunchContext;
    private final RemoteAppWork appWork;
    private final String applicationSubmitter;

    public StartAppWorkRequest(RemoteAppWork appWork, AppWorkLaunchContext context, String submitter) {
        this.appWork = appWork;
        appWorkLaunchContext = context;
        applicationSubmitter = submitter;
    }

    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return appWorkLaunchContext;
    }

    public RemoteAppWork getAppWork() {
        return appWork;
    }

    public String getApplicationSubmitter() {
        return applicationSubmitter;
    }
}
