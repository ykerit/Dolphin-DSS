package DolphinMaster;

import DolphinMaster.agentmanage.AgentListManage;
import DolphinMaster.agentmanage.AgentLivelinessMonitor;
import DolphinMaster.agentmanage.AgentTrackerService;
import DolphinMaster.security.SecurityManager;
import common.event.EventDispatcher;
import common.service.ChaosService;
import common.util.SystemClock;
import config.Configuration;

public class DolphinMaster extends ChaosService {

    private DolphinContext dolphinContext;
    private EventDispatcher eventDispatcher;

    private ClientService clientService;
    private AgentTrackerService agentTrackerService;

    private AgentLivelinessMonitor agentLivelinessMonitor;
    private AgentListManage agentListManage;
    private SecurityManager securityManager;

    public DolphinMaster() {
        super(DolphinMaster.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        this.dolphinContext = new DolphinContext();

        this.dolphinContext.setConfiguration(new Configuration());

        this.securityManager = new SecurityManager();
        this.dolphinContext.setSecurityManager(this.securityManager);

        // -------------Client service start---------------
        this.clientService = createClientService();
        addService(this.clientService);

        // -------------Event Dispatcher Service start
        this.eventDispatcher = createDispatcher();
        addService(this.eventDispatcher);
        this.dolphinContext.setDolphinDispatcher(this.eventDispatcher);

        // -------------Agent Tracker Service start
        this.agentListManage = createAgentListManage();
        addService(this.agentListManage);

        this.agentLivelinessMonitor = createAgentLivelinessMonitor();
        addService(this.agentLivelinessMonitor);

        this.agentTrackerService = createAgentTrackerService();
        addService(this.agentTrackerService);
        this.dolphinContext.setAgentTrackerService(this.agentTrackerService);

        // ---------------Dolphin Master

        this.dolphinContext.setDolphinMaster(this);
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

    private ClientService createClientService() {
        return new ClientService();
    }

    private AgentTrackerService createAgentTrackerService() {
        return new AgentTrackerService(this.dolphinContext, this.agentLivelinessMonitor,
                this.agentListManage, this.securityManager);
    }

    private EventDispatcher createDispatcher() {
        return new EventDispatcher();
    }

    private AgentLivelinessMonitor createAgentLivelinessMonitor() {
        return new AgentLivelinessMonitor(this.dolphinContext.getConfiguration(), this.eventDispatcher);
    }

    private AgentListManage createAgentListManage() {
        return new AgentListManage();
    }

    public static long getClusterTimeStamp() {
        return SystemClock.getInstance().getTime();
    }
}
