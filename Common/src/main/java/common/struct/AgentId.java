package common.struct;

import java.io.Serializable;

public class AgentId implements Serializable {
    private long agentKey;
    private String localIP;

    public AgentId(String localIP) {
        this.localIP = localIP;
    }

    public void setAgentKey(long agentKey) {
        this.agentKey = agentKey;
    }

    public long getAgentKey() {
        return agentKey;
    }

    public String getLocalIP() {
        return localIP;
    }

    @Override
    public String toString() {
        return "AgentId: " + agentKey + " - localIP:" +  localIP;
    }
}
