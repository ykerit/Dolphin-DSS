package agent.appworkmanage.cgroups;

import agent.appworkmanage.appwork.AppWork;
import common.Privileged.PrivilegedOperation;
import common.exception.ResourceHandleException;
import config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceHandlerCenter implements ResourceHandler {

    private final List<ResourceHandler> resourceHandlers;

    public ResourceHandlerCenter(List<ResourceHandler> resourceHandlers) {
        this.resourceHandlers = resourceHandlers;
    }

    @Override
    public List<PrivilegedOperation> bootstrap(Configuration configuration) throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new ArrayList<>();
        for (ResourceHandler handler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperation = handler.bootstrap(configuration);
            if (handlerOperation != null) {
                allOperations.addAll(handlerOperation);
            }
        }
        return allOperations;
    }

    @Override
    public List<PrivilegedOperation> preStart(AppWork appWork) throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new ArrayList<>();
        for (ResourceHandler handler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperation = handler.preStart(appWork);
            if (handlerOperation != null) {
                allOperations.addAll(handlerOperation);
            }
        }
        return allOperations;
    }

    @Override
    public List<PrivilegedOperation> reacquireAppWork(String appWorkId) throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new
                ArrayList<PrivilegedOperation>();

        for (ResourceHandler resourceHandler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperations =
                    resourceHandler.reacquireAppWork(appWorkId);

            if (handlerOperations != null) {
                allOperations.addAll(handlerOperations);
            }

        }
        return allOperations;
    }

    @Override
    public List<PrivilegedOperation> updateAppWork(AppWork appWork) throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new
                ArrayList<PrivilegedOperation>();

        for (ResourceHandler resourceHandler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperations =
                    resourceHandler.updateAppWork(appWork);

            if (handlerOperations != null) {
                allOperations.addAll(handlerOperations);
            }

        }
        return allOperations;
    }

    @Override
    public List<PrivilegedOperation> postComplete(String appWorKId) throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new
                ArrayList<PrivilegedOperation>();

        for (ResourceHandler resourceHandler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperations =
                    resourceHandler.postComplete(appWorKId);

            if (handlerOperations != null) {
                allOperations.addAll(handlerOperations);
            }

        }
        return allOperations;
    }

    @Override
    public List<PrivilegedOperation> tearDown() throws ResourceHandleException {
        List<PrivilegedOperation> allOperations = new
                ArrayList<PrivilegedOperation>();

        for (ResourceHandler resourceHandler : resourceHandlers) {
            List<PrivilegedOperation> handlerOperations =
                    resourceHandler.tearDown();

            if (handlerOperations != null) {
                allOperations.addAll(handlerOperations);
            }

        }
        return allOperations;
    }

    public List<ResourceHandler> getResourceHandlers() {
        return Collections.unmodifiableList(resourceHandlers);
    }
}
