package dolphinmaster.app;

import DolphinMaster.AppManager.ApplicationManager;
import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import DolphinMaster.app.AppImp;
import DolphinMaster.scheduler.FifoScheduler;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.scheduler.SchedulerEventType;
import api.app_master_message.ResourceRequest;
import common.context.ApplicationSubmission;
import common.event.EventDispatcher;
import common.resource.Resource;
import common.service.Service;
import common.struct.ApplicationId;
import common.struct.Priority;
import config.Configuration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AppTest {
    private final ApplicationId appId;
    private final DolphinContext context;
    private final Configuration config;
    private final Priority priority;
    private final ApplicationSubmission submission;
    private final EventDispatcher dispatcher;
    private final App app;
    ResourceScheduler scheduler;

    public AppTest() throws Exception {
        dispatcher = new EventDispatcher("test");
        appId = new ApplicationId(147823231, 1);
        config = new Configuration();
        context = new DolphinContext(dispatcher, config);
        priority = Priority.newInstance(1);
        submission = new ApplicationSubmission(appId,
                "test",
                priority,
                "default",
                "user",
                "jar",
                null,
                null,
                null);

        scheduler = new FifoScheduler();
        context.setScheduler(scheduler);
        scheduler.setDolphinContext(context);
        List<ResourceRequest> amReqs = new ArrayList<>();
        ResourceRequest rq = new ResourceRequest();
        rq.setCapability(Resource.newInstance(100, 1));
        rq.setPriority(Priority.newInstance(0));
        rq.setNumAppWorks(1);
        amReqs.add(rq);

        app = new AppImp(appId,
                context,
                config,
                "test",
                "user",
                "default",
                submission,
                132323,
                "jar",
                1342,
                null,
                amReqs
                );
        dispatcher.register(AppEventType.class, app);
        dispatcher.register(SchedulerEventType.class, scheduler);
        dispatcher.init();
        ((Service)scheduler).init();
        dispatcher.start();
        ((Service)scheduler).start();
    }

    @Test
    public void AppStateMachineTransTest() {
        dispatcher.getEventProcessor().process(new AppEvent(appId, AppEventType.START));
        while (true) {
            System.out.println(app.getAppState());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
