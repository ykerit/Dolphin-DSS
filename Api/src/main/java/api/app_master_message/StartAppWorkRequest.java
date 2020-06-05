package api.app_master_message;

import api.MessageID;
import common.context.AppWorkLaunchContext;
import org.greatfree.message.container.Request;

public class StartAppWorkRequest extends Request {
    private final AppWorkLaunchContext appWorkLaunchContext;
    public StartAppWorkRequest(AppWorkLaunchContext context) {
        super(MessageID.START_APP_WORK_REQUEST);
        appWorkLaunchContext = context;
    }

    public AppWorkLaunchContext getAppWorkLaunchContext() {
        return appWorkLaunchContext;
    }
}
