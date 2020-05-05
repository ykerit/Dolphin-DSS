package DolphinMaster.servertask;

import DolphinMaster.DolphinContext;
import agent.message.AgentHeartBeatRequest;
import agent.message.RegisterAgentRequest;
import common.exception.AgentException;
import message.MessageID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Notification;
import org.greatfree.message.container.Request;
import org.greatfree.server.container.ServerTask;

public class AgentTask implements ServerTask {
    private static final Logger log = LogManager.getLogger(AgentTask.class.getName());
    private final DolphinContext dolphinContext;
    public AgentTask(DolphinContext dolphinContext) {
        this.dolphinContext = dolphinContext;
    }

    @Override
    public void processNotification(Notification notification) {

    }

    @Override
    public ServerMessage processRequest(Request request) {
        switch (request.getApplicationID()) {
            case MessageID.REGISTER_AGENT_REQUEST:
                log.info("REGISTER_AGENT_REQUEST");
                try {
                    return this.dolphinContext.getAgentTrackerService().registerAgentManage((RegisterAgentRequest) request);
                } catch (AgentException e) {
                    return null;
                }
            case MessageID.HEART_BEAT_REQUEST:
                log.info("HEART_BEAT_REQUEST");
                return this.dolphinContext.getAgentTrackerService().agentHeartBeat((AgentHeartBeatRequest) request);
        }
        return null;
    }
}
