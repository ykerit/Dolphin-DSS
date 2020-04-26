package DolphinMaster.agentmanage;

import common.util.LivelinessMonitor;
import config.Configuration;

public class AgentLivelinessMonitor extends LivelinessMonitor {

    public AgentLivelinessMonitor(Configuration configuration, final AgentListManage listManage) {
        super(AgentLivelinessMonitor.class.getName(),
                configuration.getAgentMonitorInterval(),
                configuration.getAgentHeartBeatTimeOut(),
                configuration.getAgentHeartBeatTimeOutFrequency(), new CallBack() {
                    @Override
                    public void handle(long id) {
                        listManage.moveToExclude(id);
                    }
                });
    }
}
