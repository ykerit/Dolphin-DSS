package agent.appworkmanage.launcher;

import agent.Context;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.Service;


public interface AbstractAppWorkLauncher extends EventProcessor<AppWorkLauncherPoolEvent>, Service {

    void init(Context context, EventDispatcher dispatcher,
              AppWorkExecutor executor, AppWorkManagerImp appWorkManager);
}
