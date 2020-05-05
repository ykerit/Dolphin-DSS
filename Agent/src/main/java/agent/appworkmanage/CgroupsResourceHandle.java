package agent.appworkmanage;

import common.resource.Resource;
import common.resource.ResourceCollector;
import config.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CgroupsResourceHandle implements ResourceHandle {

    private Configuration conf;
    private String cgroupPrefix;

    private static final String MOUNT_FILE = "/proc/mounts";
    private static final String CGROUPS_TYPE = "cgroup";
    private static final String CONTROLLER_CPU = "cpu";
    private static final String CPU_PERIOD_US = "cfs_period_us";
    private static final String CPU_QUOTA_US = "cfs_quota_us";
    private static final int CPU_DEFAULT_WEIGHT = 102;
    private final Map<String, String> controllerPaths;

    private long deleteCgroupTimeout;
    private long deleteCgroupDelay;

    public CgroupsResourceHandle() {
        this.controllerPaths = new HashMap<>();
    }

    void initConfig() {
        this.cgroupPrefix = conf.DEFAULT_CGROUP_HIERARCHY;
        this.deleteCgroupDelay = conf.DEFAULT_DELETE_CGROUP_DELAY;
        this.deleteCgroupTimeout = conf.DEFAULT_DELETE_CGROUP_TIMEOUT;
        int len = cgroupPrefix.length();
        if (cgroupPrefix.charAt(len-1) == '/') {
            cgroupPrefix = cgroupPrefix.substring(0, len);
        }
    }

    @Override
    public void init(AppWorkExecutorImp executor) throws IOException {
        init(executor, new ResourceCollector());
    }

    void init(AppWorkExecutorImp executor, ResourceCollector collector) throws IOException {
        initConfig();
        List<String> cgroupKVs = new ArrayList<>();
        cgroupKVs.add(CONTROLLER_CPU + "=" +CONTROLLER_CPU );
        executor.mountCgroups(cgroupKVs, cgroupPrefix);
    }

    @Override
    public void preExecute(String appWorkId, Resource appWorkResource) throws IOException {

    }

    @Override
    public void postExecute(String appWorkId) {

    }

    Map<String, Set<String>> parseMountInfo() {
        Map<String, Set<String>> ret = new HashMap<>();
        BufferedReader in = null;
        return null;
    }

    void initializeControllerPaths() {
        String controllerPath;
        Map<String, Set<String>> parsedMountInfo = null;

    }
}
