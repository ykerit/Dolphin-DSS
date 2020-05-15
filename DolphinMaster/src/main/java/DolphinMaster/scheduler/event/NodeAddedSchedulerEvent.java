package DolphinMaster.scheduler.event;

import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerEvent;
import DolphinMaster.scheduler.SchedulerEventType;
import agent.status.AppWorkStatus;

import java.util.List;

public class NodeAddedSchedulerEvent extends SchedulerEvent {
    private final Node node;
    private final List<AppWorkStatus> appWorkReport;

    public NodeAddedSchedulerEvent(Node node) {
        super(SchedulerEventType.NODE_ADDED);
        this.node = node;
        this.appWorkReport = null;
    }

    public NodeAddedSchedulerEvent(Node node, List<AppWorkStatus> appWorkReport) {
        super(SchedulerEventType.NODE_ADDED);
        this.node = node;
        this.appWorkReport = appWorkReport;
    }

    public List<AppWorkStatus> getAppWorkReport() {
        return appWorkReport;
    }

    public Node getNode() {
        return node;
    }
}
