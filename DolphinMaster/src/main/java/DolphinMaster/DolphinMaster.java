package DolphinMaster;

import DolphinMaster.agent_manage.AgentListManage;
import DolphinMaster.agent_manage.AgentLivelinessMonitor;
import DolphinMaster.agent_manage.AgentTrackerService;
import DolphinMaster.user_service.ClientService;
import common.event.EventDispatcher;
import common.service.ChaosService;
import config.Configuration;
import org.greatfree.util.TerminateSignal;

public class DolphinMaster extends ChaosService {

    private DolphinContext dolphinContext;
    private EventDispatcher eventDispatcher;

    private ClientService clientService;
    private AgentTrackerService agentTrackerService;

    private AgentLivelinessMonitor agentLivelinessMonitor;
    private AgentListManage agentListManage;

    public DolphinMaster() {
        super(DolphinMaster.class.getName());
    }

    @Override
    protected void serviceInit() {
        this.dolphinContext = new DolphinContext();

        this.dolphinContext.setConfiguration(new Configuration());

        // -------------Client service start---------------
        this.clientService = createClientService();
        addService(this.clientService);

        // -------------Event Dispatcher Service start
        this.eventDispatcher = createDispatcher();
        addService(this.eventDispatcher);
        this.dolphinContext.setDolphinDispatcher(this.eventDispatcher);

        // -------------Agent Tracker Service start

        this.agentLivelinessMonitor = createAgentLivelinessMonitor();
        addService(this.agentLivelinessMonitor);

        this.agentListManage = createAgentListManage();
        addService(this.agentListManage);

        this.agentTrackerService = createAgentTrackerService();
        addService(this.agentTrackerService);
        this.dolphinContext.setAgentTrackerService(this.agentTrackerService);

        // ---------------

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
        return new AgentLivelinessMonitor(this.dolphinContext.getConfiguration());
    }

    private AgentListManage createAgentListManage() {
        return new AgentListManage();
    }
}
