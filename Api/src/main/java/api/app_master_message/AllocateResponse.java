package api.app_master_message;

import api.MessageID;
import common.resource.Resource;
import common.struct.AppWorkStatus;
import common.struct.RemoteAppWork;
import org.greatfree.message.ServerMessage;

import java.util.List;

public class AllocateResponse extends ServerMessage {
    private List<AppWorkStatus> completedAppWorks;
    private List<RemoteAppWork> allocatedAppWorks;
    private Resource availResources;
    private int numClusterNodes;

    public AllocateResponse(List<AppWorkStatus> completedAppWorks, List<RemoteAppWork> allocatedAppWorks, Resource availResources, int numClusterNodes) {
        super(MessageID.ALLOCATE_RESPONSE);
        this.completedAppWorks = completedAppWorks;
        this.allocatedAppWorks = allocatedAppWorks;
        this.availResources = availResources;
        this.numClusterNodes = numClusterNodes;
    }

    public void setCompletedAppWorks(List<AppWorkStatus> completedAppWorks) {
        this.completedAppWorks = completedAppWorks;
    }

    public void setAllocatedAppWorks(List<RemoteAppWork> allocatedAppWorks) {
        this.allocatedAppWorks = allocatedAppWorks;
    }

    public void setAvailResources(Resource availResources) {
        this.availResources = availResources;
    }

    public void setNumClusterNodes(int numClusterNodes) {
        this.numClusterNodes = numClusterNodes;
    }
}
