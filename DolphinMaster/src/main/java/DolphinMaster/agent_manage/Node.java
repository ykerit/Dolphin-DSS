package DolphinMaster.agent_manage;

public class Node {
    private String ID;
    private String IP;
    private int port;

    public String getID() {
        return ID;
    }

    public void setKey(String key) {
        this.ID = key;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
