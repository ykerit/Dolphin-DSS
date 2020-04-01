package agent.agent_status_report;

import agent.AgentContext;
import agent.event.AgentHeartBeatEvent;
import common.event.EventProcessor;
import common.util.HeartBeatProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentHeartBeat implements EventProcessor<AgentHeartBeatEvent> {
    private static final Logger log = LogManager.getLogger(AgentHeartBeat.class.getName());
    private final HeartBeatProvider heartBeatProvider;
    private final AgentContext context;

    public AgentHeartBeat(AgentContext context) {
        this.heartBeatProvider = new HeartBeatProvider(
                context.getConfiguration().getAgentSendHeartBeatPeriod(),
                2000L);
        this.context = context;
    }

    @Override
    public void process(AgentHeartBeatEvent event) {
        switch (event.getType()) {
            case T_START:
               this.heartBeatProvider.startHeartBeat(event.getHeartBeatRequest(), this.context.getRemote());
               break;
            case T_CANCEL:
                this.heartBeatProvider.cancel();
                break;
        }
    }
}
