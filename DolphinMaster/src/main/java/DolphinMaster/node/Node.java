package DolphinMaster.node;

import DolphinMaster.DolphinContext;
import agent.application.Application;
import agent.appworkmanage.appwork.AppWork;
import agent.message.AgentHeartBeatResponse;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.struct.AgentId;
import common.struct.ApplicationId;

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

    List<String> getAppWorkToCleanup();

    void setAndUpdateAgentHeartbeatResponse(AgentHeartBeatResponse response);

    Collection<AppWork> getToBeUpdateAppWorks();

}
