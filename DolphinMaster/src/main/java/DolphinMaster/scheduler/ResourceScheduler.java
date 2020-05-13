package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.schedulerunit.SchedulerUnit;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.exception.DolphinException;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.struct.AgentId;
import config.Configuration;

import java.io.IOException;
import java.util.List;

public interface ResourceScheduler {

    void setDolphinContext(DolphinContext context);

    void reinitialize(Configuration configuration, DolphinContext context) throws IOException;

    PoolInfo getPoolInfo(String pool, boolean includeChildPools, boolean recursive) throws IOException;

    Resource getClusterResource();

    Resource getMinimumResourceCapability();

    Resource getMaximumResourceCapability();

    Resource getMaximumResourceCapability(String pool);

    ResourceCalculator getResourceCalculator();

    int getNumClusterNodes();

    Allocation allocate(ApplicationId appDescribeId);

    SchedulerNodeReport getNodeReport(AgentId id);

    SchedulerAppReport getSchedulerAppInfo(ApplicationId appDescribeId);

    List<ApplicationId> getAppsInPool(String pool);

    SchedulerUnit getSchedulerUnit(AppWorkId id);

    String moveApplication(ApplicationId appId, String newPool) throws DolphinException;

    void preValidateMoveApplication(ApplicationId appid, String newPool) throws DolphinException;

    void moveAllApps(String src, String dst) throws DolphinException;

    void killAllAppsInPool(String pool) throws DolphinException;

    void removePool(String pool) throws DolphinException;

    void addPool(ResourcePool pool) throws DolphinException;

    void setClusterMaxPriority() throws DolphinException;

    void getClusterMaxPriority();

    SchedulerNode getSchedulerNode(AgentId agentId);
}
