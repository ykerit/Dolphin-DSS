package DolphinMaster;

import DolphinMaster.AppManager.ApplicationManager;
import DolphinMaster.AppManager.ApplicationManagerEvent;
import DolphinMaster.AppManager.ApplicationManagerEventType;
import DolphinMaster.agentmanage.AgentListManage;
import DolphinMaster.agentmanage.AgentLivelinessMonitor;
import DolphinMaster.agentmanage.AgentTrackerService;
import DolphinMaster.app.AMLiveLinessMonitor;
import DolphinMaster.app.App;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import DolphinMaster.node.Node;
import DolphinMaster.node.NodeEvent;
import DolphinMaster.node.NodeEventType;
import DolphinMaster.scheduler.FifoScheduler;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import DolphinMaster.security.SecurityManager;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.event.SpecialDispatcher;
import common.service.ChaosService;
import common.struct.AgentId;
import common.struct.ApplicationId;
import common.util.SystemClock;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DolphinMaster extends ChaosService {
    public static long clusterTimestamp = System.currentTimeMillis();
    private static final Logger log = LogManager.getLogger(DolphinMaster.class);

    private ActiveService activeService;

    private DolphinContext dolphinContext;
    private EventDispatcher dolphinDispatcher;

    private ClientService clientService;
    private AgentTrackerService agentTrackerService;

    private AgentLivelinessMonitor agentLivelinessMonitor;
    private AgentListManage agentListManage;
    private SecurityManager securityManagerService;

    private ResourceScheduler scheduler;
    private AppMasterService appMasterService;
    private AMLiveLinessMonitor amLiveLinessMonitor;
    private ApplicationManager applicationManager;

    public DolphinMaster() {
        super(DolphinMaster.class.getName());
    }

    public static long getClusterTimeStamp() {
        return SystemClock.getInstance().getTime();
    }

    @Override
    protected void serviceInit() throws Exception {
        this.dolphinContext = new DolphinContext();
        this.dolphinContext.setDolphinMaster(this);
        this.dolphinContext.setConfiguration(new Configuration());

        dolphinDispatcher = createDolphinDispatcher();
        addService(dolphinDispatcher);
        dolphinContext.setDolphinDispatcher(dolphinDispatcher);

        createAndInitActiveServices();

        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        startActiveServices();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        stopActiveServices();
        super.serviceStop();
    }

    protected void createAndInitActiveServices() throws Exception {
        activeService = new ActiveService(this);
        activeService.init();
    }

    void startActiveServices() throws Exception {
        if (activeService != null) {
            clusterTimestamp = System.currentTimeMillis();
            activeService.start();
        }
    }

    void stopActiveServices() throws Exception {
        if (activeService != null) {
            activeService.stop();
            activeService = null;
        }
    }


    public class ActiveService extends ChaosService {
        private DolphinMaster dolphinMaster;
        private EventProcessor<SchedulerEvent> schedulerDispatcher;

        public ActiveService(DolphinMaster dolphinMaster) {
            super(ActiveService.class.getName());
            this.dolphinMaster = dolphinMaster;
        }

        @Override
        protected void serviceInit() throws Exception {
            securityManagerService = createSecurityManager();
            addService(securityManagerService);

            amLiveLinessMonitor = createAMLiveLinensMonitor();
            addService(amLiveLinessMonitor);
            dolphinContext.setAMLiveLinessMonitor(amLiveLinessMonitor);

            agentListManage = createAgentListManage();
            addService(agentListManage);
            dolphinContext.setAgentListManage(agentListManage);

            scheduler = createScheduler();
            scheduler.setDolphinContext(dolphinContext);
            addIfService(scheduler);
            dolphinContext.setScheduler(scheduler);

            schedulerDispatcher = createSchedulerDispatcher();
            addIfService(schedulerDispatcher);
            dolphinDispatcher.register(SchedulerEventType.class, schedulerDispatcher);

            dolphinDispatcher.register(AppEventType.class, new ApplicationEventDispatcher(dolphinContext));
            dolphinDispatcher.register(NodeEventType.class, new NodeEventDispatcher(dolphinContext));

            agentLivelinessMonitor = createAgentLivelinessMonitor();
            addService(agentLivelinessMonitor);

            agentTrackerService = createAgentTrackerService();
            addService(agentTrackerService);
            dolphinContext.setAgentTrackerService(agentTrackerService);

            appMasterService = createAppMasterService();
            addService(appMasterService);
            dolphinContext.setAppMasterService(appMasterService);

            applicationManager = createApplicationManager();
            dolphinDispatcher.register(ApplicationManagerEventType.class, applicationManager);

            clientService = createClientService();
            addService(clientService);
            dolphinContext.setClientService(clientService);

            super.serviceInit();
        }

        @Override
        protected void serviceStart() throws Exception {
            super.serviceStart();
        }

        @Override
        protected void serviceStop() throws Exception {
            super.serviceStop();
        }
    }

    private EventDispatcher createDolphinDispatcher() {
        return new EventDispatcher("DolphinMaster Event Dispatcher");
    }


    private SecurityManager createSecurityManager() {
        return new SecurityManager();
    }

    private AgentListManage createAgentListManage() {
        return new AgentListManage();
    }

    private AMLiveLinessMonitor createAMLiveLinensMonitor() {
        return new AMLiveLinessMonitor(this.dolphinContext.getConfiguration(), this.dolphinDispatcher);
    }

    private AgentListManage createAgentListManager() {
        return new AgentListManage();
    }

    private ResourceScheduler createScheduler() {
        return new FifoScheduler();
    }

    private EventProcessor<SchedulerEvent> createSchedulerDispatcher() {
        return new SpecialDispatcher<>(this.scheduler, "SchedulerEventDispatcher");
    }

    private AgentLivelinessMonitor createAgentLivelinessMonitor() {
        return new AgentLivelinessMonitor(this.dolphinContext.getConfiguration(), dolphinDispatcher);
    }

    private AppMasterService createAppMasterService() {
        return new AppMasterService(dolphinContext, scheduler);
    }

    private ApplicationManager createApplicationManager() {
        return new ApplicationManager(dolphinContext, scheduler, appMasterService, dolphinContext.getConfiguration());
    }

    private ClientService createClientService() {
        return new ClientService(dolphinContext, scheduler, applicationManager);
    }

    private AgentTrackerService createAgentTrackerService() {
        return new AgentTrackerService(this.dolphinContext, this.agentLivelinessMonitor,
                this.agentListManage, this.securityManagerService);
    }

    public static final class ApplicationEventDispatcher implements
            EventProcessor<AppEvent> {

        private final DolphinContext context;

        public ApplicationEventDispatcher(DolphinContext rmContext) {
            this.context = rmContext;
        }

        @Override
        public void process(AppEvent event) {
            ApplicationId appID = event.getAppId();
            App app = this.context.getApps().get(appID);
            if (app != null) {
                try {
                    app.process(event);
                } catch (Throwable t) {
                    log.error("Error in processing event type " + event.getType()
                            + " for application " + appID, t);
                }
            }
        }
    }

    public static final class NodeEventDispatcher implements EventProcessor<NodeEvent> {

        private final DolphinContext context;

        public NodeEventDispatcher(DolphinContext context) {
            this.context = context;
        }

        @Override
        public void process(NodeEvent event) {
            AgentId nodeId = event.getAgentId();
            Node node = this.context.getNodes().get(nodeId);
            if (node != null) {
                try {
                    ((EventProcessor<NodeEvent>) node).process(event);
                } catch (Throwable t) {
                    log.error("Error in handling event type " + event.getType()
                            + " for node " + nodeId, t);
                }
            }
        }
    }
}
