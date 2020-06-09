package common.struct;

import java.io.Serializable;
import java.util.Objects;

public class AgentId implements Serializable, Comparable<AgentId> {
    private String localIP;
    private String hostname;
    private int commandPort;

    public String getIP() {
        return localIP;
    }

    public void setIP(String localIP) {
        this.localIP = localIP;
    }

    public String getHostname() {
        return hostname;
    }

    public int getCommandPort() {
        return commandPort;
    }

    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return hostname + ":" + commandPort;
    }

    @Override
    public int compareTo(AgentId o) {
        int cpt = hostname.compareTo(o.hostname);
        if (cpt == 0) {
            if (this.getCommandPort() > o.getCommandPort()) {
                return 1;
            } else if (this.getCommandPort() < o.getCommandPort()) {
                return -1;
            }
            return 0;
        }
        return cpt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentId agentId = (AgentId) o;
        return commandPort == agentId.commandPort &&
                Objects.equals(localIP, agentId.localIP) &&
                Objects.equals(hostname, agentId.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandPort, localIP, hostname);
    }
}
