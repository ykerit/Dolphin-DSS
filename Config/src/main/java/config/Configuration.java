package config;

import org.greatfree.util.IPAddress;

import java.util.ResourceBundle;

public class Configuration {
    private static final String DOLPHIN_MASTER = "dolphin_master";

    private static final String AGENT_MONITOR_INTERVAL = "agent_monitor_interval";
    private static final String AGENT_HEART_BEAT_TIME_OUT = "agent_heart_beat_time_out";
    private static final String AGENT_HEART_BEAT_TIME_OUT_FREQUENCY = "agent_heart_beat_time_out_frequency";

    private static final String APP_MASTER_MONITOR_INTERVAL = "app_master_monitor_interval";
    private static final String APP_MASTER_HEART_BEAT_TIME_OUT = "app_master_heart_beat_time_out";
    private static final String APP_MASTER_HEART_BEAT_TIME_OUT_FREQUENCY = "app_master_heart_beat_time_out_frequency";

    private static final String DOLPHIN_MASTER_IP = "dolphin_master_ip";
    private static final String DOLPHIN_MASTER_NODE_PORT = "dolphin_master_node_port";
    private static final String DOLPHIN_MASTER_CLIENT_PORT = "dolphin_master_client_port";
    private static final String DOLPHIN_MASTER_WEB_PORT = "dolphin_master_web_port";
    private static final String DOLPHIN_MASTER_ADMIN_PORT = "dolphin_master_admin_port";

    private static final String AGENT_SEND_HEART_BEAT_PERIOD = "agent_send_heart_beat_period";

    private static final String CEPH_CONF_DIR = "ceph_conf_dir";

    private static final String APP_WORK_PRIORITY = "app_work_priority";

    private ResourceBundle resourceBundle;

    private long agentMonitorInterval;
    private long agentHeartBeatTimeOut;
    private long agentHeartBeatTimeOutFrequency;

    private long appMasterMonitorInterval;
    private long appMasterHeartBeatTimeOut;
    private long appMasterHeartBeatTimeOutFrequency;

    private IPAddress dolphinMasterNodeHost;
    private IPAddress dolphinMasterClientHost;
    private IPAddress dolphinMasterWebHost;
    private IPAddress dolphinMasterAdminHost;

    private long agentSendHeartBeatPeriod;

    private String cephConfDir;

    private Integer appWorkPriority;

    public long getAgentSendHeartBeatPeriod() {
        return agentSendHeartBeatPeriod;
    }

    public Configuration() {
        resourceBundle = ResourceBundle.getBundle(DOLPHIN_MASTER);
        configure();
    }

    public String getCephConfDir() {
        return cephConfDir;
    }

    public Integer getAppWorkPriority() {
        return appWorkPriority;
    }

    void configure() {
        agentMonitorInterval = Long.parseLong((String) resourceBundle.getObject(AGENT_MONITOR_INTERVAL));
        agentHeartBeatTimeOut = Long.parseLong((String) resourceBundle.getObject(AGENT_HEART_BEAT_TIME_OUT));
        agentHeartBeatTimeOutFrequency = Long.parseLong((String)resourceBundle.getObject(AGENT_HEART_BEAT_TIME_OUT_FREQUENCY));

        appMasterMonitorInterval = Long.parseLong((String)resourceBundle.getObject(APP_MASTER_HEART_BEAT_TIME_OUT));
        appMasterHeartBeatTimeOut = Long.parseLong((String)resourceBundle.getObject(APP_MASTER_MONITOR_INTERVAL));
        appMasterHeartBeatTimeOutFrequency = Long.parseLong((String)resourceBundle.getObject(APP_MASTER_HEART_BEAT_TIME_OUT_FREQUENCY));

        dolphinMasterNodeHost = getIPAddress(DOLPHIN_MASTER_NODE_PORT);
        dolphinMasterClientHost = getIPAddress(DOLPHIN_MASTER_CLIENT_PORT);
        dolphinMasterWebHost = getIPAddress(DOLPHIN_MASTER_WEB_PORT);
        dolphinMasterAdminHost = getIPAddress(DOLPHIN_MASTER_ADMIN_PORT);

        agentSendHeartBeatPeriod = Long.parseLong(resourceBundle.getString(AGENT_SEND_HEART_BEAT_PERIOD));

        cephConfDir = resourceBundle.getString(CEPH_CONF_DIR);

        appWorkPriority = Integer.parseInt(resourceBundle.getString(APP_WORK_PRIORITY));
    }

    public long getAgentMonitorInterval() {
        return agentMonitorInterval;
    }

    public IPAddress getDolphinMasterClientHost() {
        return dolphinMasterClientHost;
    }

    public IPAddress getDolphinMasterWebHost() {
        return dolphinMasterWebHost;
    }

    public IPAddress getDolphinMasterAdminHost() {
        return dolphinMasterAdminHost;
    }

    public long getAgentHeartBeatTimeOut() {
        return agentHeartBeatTimeOut;
    }

    public long getAgentHeartBeatTimeOutFrequency() {
        return agentHeartBeatTimeOutFrequency;
    }

    public long getAppMasterMonitorInterval() {
        return appMasterMonitorInterval;
    }

    public long getAppMasterHeartBeatTimeOut() {
        return appMasterHeartBeatTimeOut;
    }

    public long getAppMasterHeartBeatTimeOutFrequency() {
        return appMasterHeartBeatTimeOutFrequency;
    }

    public IPAddress getDolphinMasterNodeHost() {
        return dolphinMasterNodeHost;
    }

    private IPAddress getIPAddress(String target) {
        String ip = resourceBundle.getString(DOLPHIN_MASTER_IP);
        int port = Integer.parseInt(resourceBundle.getString(target));
        return new IPAddress(ip, port);
    }
}
