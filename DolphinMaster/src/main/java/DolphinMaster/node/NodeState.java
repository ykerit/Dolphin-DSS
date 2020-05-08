package DolphinMaster.node;

public enum NodeState {
    /** New node */
    NEW,

    /** Running node */
    RUNNING,

    /** Node is unhealthy */
    UNHEALTHY,

    /** Node is out of service */
    DECOMMISSIONED,

    /** Node has not sent a heartbeat for some configured time threshold*/
    LOST,

    /** Node has rebooted */
    REBOOTED,

    /** Node decommission is in progress */
    DECOMMISSIONING,

    /** Node has shutdown gracefully. */
    SHUTDOWN;

    public boolean isUnusable() {
        return (this == UNHEALTHY || this == DECOMMISSIONED
                || this == LOST || this == SHUTDOWN);
    }

    public boolean isInactiveState() {
        return this == NodeState.DECOMMISSIONED ||
                this == NodeState.LOST || this == NodeState.REBOOTED ||
                this == NodeState.SHUTDOWN;
    }

    public boolean isActiveState() {
        return this == NodeState.NEW ||
                this == NodeState.RUNNING || this == NodeState.UNHEALTHY ||
                this == NodeState.DECOMMISSIONING;
    }
}
