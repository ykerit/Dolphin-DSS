package agent.appworkmanage.cgroups;

import common.exception.ResourceHandleException;

import java.util.HashSet;
import java.util.Set;

public interface CGroupsHandler {
    enum CGroupController {
        CPU("cpu"),
        MEMORY("memory"),
        CPUACCT("cpuacct"),
        CPUSET("cpuset");


        private final String name;

        CGroupController(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Set<String> getValidGroups() {
            Set<String> validCgroups = new HashSet<>();

            for (CGroupController controller : CGroupController.values()) {
                validCgroups.add(controller.getName());
            }
            return validCgroups;
        }
    }

    String CGROUP_PROCS_FILE = "cgroup.procs";
    String CGROUP_PARAM_CLASSID = "classid";
    String CGROUP_PARAM_BLKIO_WEIGHT = "weight";

    String CGROUP_PARAM_MEMORY_HARD_LIMIT_BYTES = "limit_in_bytes";
    String CGROUP_PARAM_MEMORY_SWAP_HARD_LIMIT_BYTES = "memsw.limit_in_bytes";
    String CGROUP_PARAM_MEMORY_SOFT_LIMIT_BYTES = "soft_limit_in_bytes";
    String CGROUP_PARAM_MEMORY_OOM_CONTROL = "oom_control";
    String CGROUP_PARAM_MEMORY_SWAPPINESS = "swappiness";
    String CGROUP_PARAM_MEMORY_USAGE_BYTES = "usage_in_bytes";
    String CGROUP_PARAM_MEMORY_MEMSW_USAGE_BYTES = "memsw.usage_in_bytes";
    String CGROUP_NO_LIMIT = "-1";
    String UNDER_OOM = "under_oom 1";


    String CGROUP_CPU_PERIOD_US = "cfs_period_us";
    String CGROUP_CPU_QUOTA_US = "cfs_quota_us";
    String CGROUP_CPU_SHARES = "shares";


    void initializeCGroupController(CGroupController controller) throws ResourceHandleException;

    String createCGroup(CGroupController controller, String cGroupId) throws ResourceHandleException;

    void deleteCGroup(CGroupController controller, String cGroupId) throws ResourceHandleException;

    String getControllerPath(CGroupController controller);

    String getRelativePathForCGroup(String cGroupId);

    String getPathForCGroup(CGroupController controller, String
            cGroupId);

    String getPathForCGroupTasks(CGroupController controller, String
            cGroupId);

    String getPathForCGroupParam(CGroupController controller, String
            cGroupId, String param);

    void updateCGroupParam(CGroupController controller, String cGroupId,
                           String param, String value) throws ResourceHandleException;
    String getCGroupParam(CGroupController controller, String cGroupId,
                          String param) throws ResourceHandleException;
}
