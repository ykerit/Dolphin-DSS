package DolphinMaster.node;

import DolphinMaster.DolphinContext;
import agent.message.AgentHeartBeatResponse;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.struct.AgentId;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;

import java.util.Collection;
import java.util.List;

public interface Node {
    AgentId getNodeId();

    String getHostName();

    String getNodeAddress();

    Resource getTotalCapability();

    Resource getPhysicalResource();

    void rsyncCapability();

    NodeState getState();

    DolphinContext getContext();

    int getCommandPort();

    String getRackName();

    ResourceUtilization getAppWorksUtilization();

    ResourceUtilization getNodeUtilization();

    List<ApplicationId> getAppsToCleanup();

    List<ApplicationId> getRunningApps();

    List<AppWorkId> getAppWorkToCleanup();

    void setAndUpdateAgentHeartbeatResponse(AgentHeartBeatResponse response);

    Collection<RemoteAppWork> getToBeUpdateAppWorks();

    List<UpdateAppWorkInfo> pullAppWorkUpdates();

}
