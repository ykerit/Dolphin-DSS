package DolphinMaster.scheduler.event;

import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import common.struct.AgentAppWorkStatus;
import common.struct.AppWorkStatus;

import java.util.List;

public class NodeAddedSchedulerEvent extends SchedulerEvent {
    private final Node node;
    private final List<AgentAppWorkStatus> appWorkReport;

    public NodeAddedSchedulerEvent(Node node) {
        super(SchedulerEventType.NODE_ADDED);
        this.node = node;
        this.appWorkReport = null;
    }

    public NodeAddedSchedulerEvent(Node node, List<AgentAppWorkStatus> appWorkReport) {
        super(SchedulerEventType.NODE_ADDED);
        this.node = node;
        this.appWorkReport = appWorkReport;
    }

    public List<AgentAppWorkStatus> getAppWorkReport() {
        return appWorkReport;
    }

    public Node getNode() {
        return node;
    }
}
