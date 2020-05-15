package DolphinMaster.app;

import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.struct.ApplicationId;
import common.util.LivelinessMonitor;
import config.Configuration;

public class AMLiveLinessMonitor extends LivelinessMonitor<ApplicationId> {

    private EventProcessor dispatcher;
    private Configuration config;

    public AMLiveLinessMonitor(Configuration configuration, EventDispatcher dispatcher) {
        super("AMLivelinessMonitor");
        this.dispatcher = dispatcher.getEventProcessor();
        this.config = configuration;
    }

    @Override
    protected void serviceInit() throws Exception {
        setExpireFrequency(config.getAgentHeartBeatTimeOutFrequency());
        setExpireInterval(config.getAgentHeartBeatTimeOut());
        setMonitorInterval(config.getAgentMonitorInterval());
        super.serviceInit();
    }

    @Override
    protected void expire(ApplicationId key) {
        dispatcher.process(new AppEvent(key, AppEventType.EXPIRE));
    }
}
