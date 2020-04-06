package common.struct;

import java.io.Serializable;

public class AgentID implements Serializable {
    private long agentKey;
    private String localIP;
    private int webPort;

    public AgentID(long agentKey, String localIP, int webPort) {
        this.agentKey = agentKey;
        this.localIP = localIP;
        this.webPort = webPort;
    }

    public void setAgentKey(long agentKey) {
        this.agentKey = agentKey;
    }

    public AgentID(String localIP, int webPort) {

    }

    public long getAgentKey() {
        return agentKey;
    }

    public String getLocalIP() {
        return localIP;
    }

    public int getWebPort() {
        return webPort;
    }

    @Override
    public String toString() {
        return "AgentID{" +
                "agentKey=" + agentKey +
                ", localIP='" + localIP + '\'' +
                ", webPort=" + webPort +
                '}';
    }
}
