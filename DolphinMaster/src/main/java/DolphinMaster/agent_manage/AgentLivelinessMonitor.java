package DolphinMaster.agent_manage;

import common.util.LivelinessMonitor;
import config.Configuration;

public class AgentLivelinessMonitor extends LivelinessMonitor {
    public AgentLivelinessMonitor(Configuration configuration) {
        super(AgentLivelinessMonitor.class.getName(),
                configuration.getAgentMonitorInterval(),
                configuration.getAgentHeartBeatTimeOut(),
                configuration.getAgentHeartBeatTimeOutFrequency());
    }
}
