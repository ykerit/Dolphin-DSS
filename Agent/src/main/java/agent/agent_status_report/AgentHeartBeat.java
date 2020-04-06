package agent.agent_status_report;

import agent.AgentContext;
import agent.event.ActionEvent;
import agent.event.AgentHeartBeatEvent;
import common.event.ActionType;
import common.event.EventProcessor;
import common.util.CallBack;
import common.util.HeartBeatProvider;
import message.agent_master_message.HeartBeatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.message.ServerMessage;

import java.io.IOException;

public class AgentHeartBeat implements EventProcessor<AgentHeartBeatEvent> {
    private static final Logger log = LogManager.getLogger(AgentHeartBeat.class.getName());
    private final HeartBeatProvider heartBeatProvider;
    private final AgentContext context;

    public AgentHeartBeat(AgentContext context) {
        this.heartBeatProvider = new HeartBeatProvider(
                context.getConfiguration().getAgentSendHeartBeatPeriod());
        this.context = context;
    }

    @Override
    public void process(AgentHeartBeatEvent event) {
        switch (event.getType()) {
            case T_START:
                this.heartBeatProvider.startHeartBeat(
                        new CallBack() {
                            @Override
                            public void handleHeartBeat() {
                                event.getHeartBeatRequest().setToken(context.getToken());
                                HeartBeatResponse response = null;
                                try {
                                    response = (HeartBeatResponse) StandaloneClient.CS().read(context.getRemote().getIP(), context.getRemote().getPort(), event.getHeartBeatRequest());
                                } catch (ClassNotFoundException | RemoteReadException | IOException e) {
                                    e.printStackTrace();
                                }
                                context.getAgentDispatcher().getEventProcessor()
                                        .process(
                                                new ActionEvent(response.getAction(), response.getData()));
                            }
                        });
                break;
            case T_CANCEL:
                this.heartBeatProvider.cancel();
                break;
        }
    }
}
