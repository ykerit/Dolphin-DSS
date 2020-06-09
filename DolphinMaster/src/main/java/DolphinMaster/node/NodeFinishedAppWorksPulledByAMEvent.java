package DolphinMaster.node;

import common.struct.AgentId;
import common.struct.AppWorkId;

import java.util.List;

public class NodeFinishedAppWorksPulledByAMEvent extends NodeEvent{
    private final List<AppWorkId> appWorks;

    public NodeFinishedAppWorksPulledByAMEvent(AgentId id, List<AppWorkId> appWorks) {
        super(id, NodeEventType.FINISHED_APP_WORKS_PULLED_BY_AM);
        this.appWorks = appWorks;
    }

    public List<AppWorkId> getAppWorks() {
        return appWorks;
    }
}
