package agent.appworkmanage.launcher;

import agent.AgentContext;
import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkManagerImp;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.Service;


public interface AbstractAppWorkLauncher extends EventProcessor<AppWorkLauncherPoolEvent>, Service {

    void init(AgentContext context, EventDispatcher dispatcher,
              AppWorkExecutor executor, AppWorkManagerImp appWorkManager);
}
