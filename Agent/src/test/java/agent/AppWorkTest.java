package agent;

import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkImp;
import common.event.EventDispatcher;
import common.struct.AppWorkId;
import config.Configuration;
import org.junit.Test;

public class AppWorkTest {
    private final AppWorkId appWorkId;
    private final Context context;
    private final Configuration config;
    private final EventDispatcher dispatcher;
    private final AppWork appWork;

    public AppWorkTest() {
        appWorkId = new AppWorkId();
        context = new Context();
        config = new Configuration();
        dispatcher = new EventDispatcher("test");
        appWork = new AppWorkImp(config,
                dispatcher,
                null,
                context,
                System.currentTimeMillis(),
                "root",
                appWorkId);
    }

    @Test
    public void testAppWorkStateMachine() {

    }
}
