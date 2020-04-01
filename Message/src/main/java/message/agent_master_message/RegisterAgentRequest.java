package message.agent_master_message;

import common.struct.AgentID;
import message.MessageID;
import org.greatfree.message.container.Request;

public class RegisterAgentRequest extends Request {
    private AgentID host;
    private double maxAvailableCPU;
    private double maxAvailableMem;

    public RegisterAgentRequest(AgentID host, double maxAvailableCPU, double maxAvailableMem) {
        super(MessageID.REGISTER_AGENT_REQUEST);
        this.host = host;
        this.maxAvailableCPU = maxAvailableCPU;
        this.maxAvailableMem = maxAvailableMem;
    }

    public AgentID getHost() {
        return host;
    }

    public double getMaxAvailableCPU() {
        return maxAvailableCPU;
    }

    public double getMaxAvailableMem() {
        return maxAvailableMem;
    }
}
