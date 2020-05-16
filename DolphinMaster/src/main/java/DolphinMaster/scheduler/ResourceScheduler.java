package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import DolphinMaster.schedulerunit.SchedulerUnit;
import api.app_master_message.ResourceRequest;
import common.event.EventProcessor;
import common.service.Service;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.exception.DolphinException;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.struct.AgentId;
import config.Configuration;

import java.io.IOException;
import java.util.List;

public interface ResourceScheduler extends EventProcessor<SchedulerEvent> {

    void setDolphinContext(DolphinContext context);

    void reinitialize(Configuration configuration, DolphinContext context) throws IOException;

    PoolInfo getPoolInfo(String pool, boolean includeChildPools, boolean recursive) throws IOException;

    Resource getClusterResource();

    Resource getMinimumResourceCapability();

    Resource getMaximumResourceCapability();

    Resource getMaximumResourceCapability(String pool);

    ResourceCalculator getResourceCalculator();

    int getNumClusterNodes();

    Allocation allocate(ApplicationId applicationId, List<ResourceRequest> ask, List<AppWorkId> release);

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

    Resource getNormalizedResource(Resource reqRes, Resource maxResourceCapability);

    public SchedulerNode getNode(AgentId agentId);

    PoolMetrics getRootPoolMetrics();
}
