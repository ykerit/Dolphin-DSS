package DolphinMaster.node;

import common.struct.AgentId;
import common.struct.AppWorkId;

public class NodeCleanAppWorkEvent extends NodeEvent{
    private AppWorkId appWorkId;

    public NodeCleanAppWorkEvent(AgentId id, AppWorkId appWorkId) {
        super(id, NodeEventType.CLEANUP_APP_WORK);
    }

    public AppWorkId getAppWorkId() {
        return appWorkId;
    }
}
