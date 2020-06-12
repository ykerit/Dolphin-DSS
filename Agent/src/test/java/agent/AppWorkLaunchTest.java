package agent;

import agent.appworkmanage.AppWorkExecutor;
import agent.appworkmanage.AppWorkExecutorImp;
import agent.appworkmanage.appwork.AppWork;
import common.context.AppWorkLaunchContext;
import common.event.EventDispatcher;
import common.service.ChaosService;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import config.Configuration;

public class AppWorkLaunchTest {

    class LaunchTest extends ChaosService {
        private AppWorkExecutor executor;
        private ApplicationId applicationId;
        private AppWorkId appWorkId;
        private AppWork appWork;
        private Context context;
        private Configuration config;
        private EventDispatcher dispatcher;
        private AppWorkLaunchContext launchContext;

        public LaunchTest() {
            super(LaunchTest.class.getName());
            config = new Configuration();
            dispatcher = new EventDispatcher("test");
            context = new Context();
            context.setAgentDispatcher(dispatcher);
            executor = new AppWorkExecutorImp();
            context.setAppWorkExecutor(executor);
            context.setConfiguration(config);
            applicationId = new ApplicationId(System.currentTimeMillis(), 0);
            appWorkId = new AppWorkId(applicationId, 0);

            launchContext = new AppWorkLaunchContext(null, null, null, null);
        }
    }


}
