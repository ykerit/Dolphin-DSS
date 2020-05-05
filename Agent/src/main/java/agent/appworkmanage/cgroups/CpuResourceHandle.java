package agent.appworkmanage.cgroups;

import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkExecType;
import common.Privileged.PrivilegedOperation;
import common.exception.ResourceHandleException;
import common.resource.Resource;
import common.resource.ResourceCollector;
import common.util.HardwareUtils;
import config.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CpuResourceHandle implements ResourceHandler {

    static final Logger log = LogManager.getLogger(CpuResourceHandle.class.getName());

    private CGroupsHandler cGroupsHandler;
    private float dolphinProcessors;
    private int vCore;
    private static final CGroupsHandler.CGroupController CPU = CGroupsHandler.CGroupController.CPU;

    static final int MAX_QUOTA_US = 1000 * 1000;
    static final int MIN_PERIOD_US = 1000;
    static final int CPU_DEFAULT_WEIGHT = 1024;
    static final int CPU_DEFAULT_WEIGHT_OPPORTUNISTIC = 2;

    CpuResourceHandle(CGroupsHandler cGroupsHandler) {
        this.cGroupsHandler = cGroupsHandler;
    }

    List<PrivilegedOperation> bootstrap(ResourceCollector collector, Configuration configuration) throws ResourceHandleException {
        this.cGroupsHandler.initializeCGroupController(CPU);
        vCore = HardwareUtils.getVCore(collector, configuration);

        dolphinProcessors = HardwareUtils.getAppWorksCPUs(collector, configuration);
        int systemProcessors = HardwareUtils.getNodeCPUs(collector, configuration);
        boolean existingCpuLimits;
        try {
            existingCpuLimits = cpuLimitsExist(cGroupsHandler.getPathForCGroup(CPU, ""));
        } catch (IOException e) {
            throw new ResourceHandleException(e);
        }

        if ((int) dolphinProcessors != systemProcessors) {
            int[] limits = getOverallLimits(dolphinProcessors);
            cGroupsHandler.updateCGroupParam(CPU, "",
                    CGroupsHandler.CGROUP_CPU_PERIOD_US, String.valueOf(limits[0]));
            cGroupsHandler.updateCGroupParam(CPU, "",
                    CGroupsHandler.CGROUP_CPU_QUOTA_US, String.valueOf(limits[1]));
        } else if (existingCpuLimits) {
            cGroupsHandler
                    .updateCGroupParam(CPU, "", CGroupsHandler.CGROUP_CPU_QUOTA_US,
                            String.valueOf(-1));
        }
        return null;
    }

    public static int[] getOverallLimits(float dolphinProcessors) {

        int[] ret = new int[2];

        if (dolphinProcessors < 0.01f) {
            throw new IllegalArgumentException("Number of processors can't be <= 0.");
        }

        int quotaUS = MAX_QUOTA_US;
        int periodUS = (int) (MAX_QUOTA_US / dolphinProcessors);
        if (dolphinProcessors < 1.0f) {
            periodUS = MAX_QUOTA_US;
            quotaUS = (int) (periodUS * dolphinProcessors);
            if (quotaUS < MIN_PERIOD_US) {
                log.warn("The quota calculated for the cgroup was too low."
                        + " The minimum value is " + MIN_PERIOD_US
                        + ", calculated value is " + quotaUS
                        + ". Setting quota to minimum value.");
                quotaUS = MIN_PERIOD_US;
            }
        }

        // cfs_period_us can't be less than 1000 microseconds
        // if the value of periodUS is less than 1000, we can't really use cgroups
        // to limit cpu
        if (periodUS < MIN_PERIOD_US) {
            log.warn("The period calculated for the cgroup was too low."
                    + " The minimum value is " + MIN_PERIOD_US
                    + ", calculated value is " + periodUS
                    + ". Using all available CPU.");
            periodUS = MAX_QUOTA_US;
            quotaUS = -1;
        }

        ret[0] = periodUS;
        ret[1] = quotaUS;
        return ret;
    }

    @Override
    public List<PrivilegedOperation> bootstrap(Configuration configuration) throws ResourceHandleException {
        return bootstrap(new ResourceCollector(), configuration);
    }

    @Override
    public List<PrivilegedOperation> preStart(AppWork appWork) throws ResourceHandleException {
        String cgroupId = appWork.getAppWorkId();
        cGroupsHandler.createCGroup(CPU, cgroupId);
        updateAppWork(appWork);
        List<PrivilegedOperation> ret = new ArrayList<>();
        ret.add(new PrivilegedOperation(PrivilegedOperation.OperationType.ADD_PID_TO_CGROUP,
                PrivilegedOperation.CGROUP_ARG_PREFIX +
                        cGroupsHandler.getPathForCGroupTasks(CPU, cgroupId)));
        return ret;
    }

    @Override
    public List<PrivilegedOperation> reacquireAppWork(String appWorkId) throws ResourceHandleException {
        return null;
    }

    @Override
    public List<PrivilegedOperation> updateAppWork(AppWork appWork) throws ResourceHandleException {
        Resource appWorkResource = appWork.getResource();
        String cgroupId = appWork.getAppWorkId();
        File cgroup = new File(cGroupsHandler.getPathForCGroup(CPU, cgroupId));
        if (cgroup.exists()) {
            int appWorkVCores = appWorkResource.getVCore();
            if (appWork.getExecType() == AppWorkExecType.OPPORTUNISTIC) {
                cGroupsHandler.updateCGroupParam(CPU, cgroupId, CGroupsHandler.CGROUP_CPU_QUOTA_US,
                        String.valueOf(CPU_DEFAULT_WEIGHT_OPPORTUNISTIC));
            } else {
                int cpuShares = CPU_DEFAULT_WEIGHT * appWorkVCores;
                cGroupsHandler.updateCGroupParam(CPU, cgroupId, CGroupsHandler.CGROUP_CPU_SHARES, String.valueOf(cpuShares));
            }
        }
        return null;
    }

    @Override
    public List<PrivilegedOperation> postComplete(String appWorKId) throws ResourceHandleException {
        cGroupsHandler.deleteCGroup(CPU, appWorKId);
        return null;
    }

    @Override
    public List<PrivilegedOperation> tearDown() throws ResourceHandleException {
        return null;
    }

    public static boolean cpuLimitsExist(String path) throws IOException {
        File quotaFile = new File(path, CPU.getName() + "." + CGroupsHandler.CGROUP_CPU_QUOTA_US);
        if (quotaFile.exists()) {
            String contents = FileUtils.readFileToString(quotaFile, "UTF-8");
            int quotaUS = Integer.parseInt(contents.trim());
            if (quotaUS != -1) {
                return true;
            }
        }
        return false;
    }
}
