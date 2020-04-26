package DolphinMaster;

import DolphinMaster.agentmanage.AgentListManage;
import DolphinMaster.agentmanage.AgentLivelinessMonitor;
import DolphinMaster.agentmanage.AgentTrackerService;
import DolphinMaster.security.SecurityManage;
import DolphinMaster.userservice.ClientService;
import common.event.EventDispatcher;
import common.service.ChaosService;
import config.Configuration;

public class DolphinMaster extends ChaosService {

    private DolphinContext dolphinContext;
    private EventDispatcher eventDispatcher;

    private ClientService clientService;
    private AgentTrackerService agentTrackerService;

    private AgentLivelinessMonitor agentLivelinessMonitor;
    private AgentListManage agentListManage;
    private SecurityManage securityManage;

    public DolphinMaster() {
        super(DolphinMaster.class.getName());
    }

    @Override
    protected void serviceInit() {
        this.dolphinContext = new DolphinContext();

        this.dolphinContext.setConfiguration(new Configuration());

        this.securityManage = new SecurityManage();
        this.dolphinContext.setSecurityManage(this.securityManage);

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
    protected void serviceStart() {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        super.serviceStop();
    }

    private ClientService createClientService() {
        return new ClientService();
    }

    private AgentTrackerService createAgentTrackerService() {
        return new AgentTrackerService(this.dolphinContext, this.agentLivelinessMonitor, this.agentListManage);
    }

    private EventDispatcher createDispatcher() {
        return new EventDispatcher();
    }

    private AgentLivelinessMonitor createAgentLivelinessMonitor() {
        return new AgentLivelinessMonitor(this.dolphinContext.getConfiguration(), this.agentListManage);
    }

    private AgentListManage createAgentListManage() {
        return new AgentListManage();
    }
}
