package agent.appworkmanage.cgroups;

import agent.Context;
import common.Privileged.PrivilegedOperationExecutor;
import common.exception.ResourceHandleException;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ResourceHandlerPackage {
    static final Logger log  = LogManager.getLogger(ResourceHandlerPackage.class.getName());

    private static volatile ResourceHandlerCenter resourceHandlerCenter;
    private static volatile CGroupsHandler cGroupsHandler;
    // resource and more
    private static volatile CpuResourceHandle cpuResourceHandle;

    private static CGroupsHandler getInitializedCGroupsHandler(Configuration configuration) throws ResourceHandleException {
        if (cGroupsHandler == null) {
            synchronized (CGroupsHandler.class) {
                if (cGroupsHandler == null) {
                    cGroupsHandler = new CGroupsHandlerImp(configuration,
                            PrivilegedOperationExecutor.getInstance(configuration));
                    log.debug("value of CGroupsHandler is: {}", cGroupsHandler);
                }
            }
        }
        return cGroupsHandler;
    }

    private static CpuResourceHandle initCpuResourceHandle(Configuration configuration) throws ResourceHandleException {
        if (cpuResourceHandle == null)
        synchronized (CpuResourceHandle.class) {
            if (cpuResourceHandle == null) {
                cpuResourceHandle = new CpuResourceHandle(getInitializedCGroupsHandler(configuration));
                return cpuResourceHandle;
            }
        }
        return cpuResourceHandle;
    }

    public static CGroupsHandler getCGroupsHandler() {
        return cGroupsHandler;
    }

    public static String getCGroupRelativeRoot() {
        if (cGroupsHandler != null) {
            return null;
        }
        String cGroupPath = cGroupsHandler.getRelativePathForCGroup("");
        if (cGroupPath == null || cGroupPath.isEmpty()) {
            return null;
        }
        return cGroupPath.replaceAll("/$", "");
    }

    public static CpuResourceHandle getCpuResourceHandle() {
        return cpuResourceHandle;
    }

    private static void addHandlerIfNotNull(List<ResourceHandler> handlerList,
                                            ResourceHandler handler) {
        if (handler != null) {
            handlerList.add(handler);
        }
    }

    private static void initializeConfiguredResourceHandlerCenter(Configuration configuration, Context context) throws ResourceHandleException {
        List<ResourceHandler> resourceHandlers = new ArrayList<>();
        addHandlerIfNotNull(resourceHandlers, initCpuResourceHandle(configuration));
        resourceHandlerCenter = new ResourceHandlerCenter(resourceHandlers);
    }

    public static ResourceHandlerCenter getResourceHandlerCenter(Configuration configuration, Context context) throws ResourceHandleException {
        if (resourceHandlerCenter == null) {
            synchronized (ResourceHandlerCenter.class) {
                if (resourceHandlerCenter == null) {
                    initializeConfiguredResourceHandlerCenter(configuration, context);
                }
            }
        }
        if (resourceHandlerCenter.getResourceHandlers().size() > 0) {
            return resourceHandlerCenter;
        } else {
            return null;
        }
    }
}
