package agent.appworkmanage.cgroups;

import agent.appworkmanage.appwork.AppWork;
import common.Privileged.PrivilegedOperation;
import common.exception.ResourceHandleException;
import config.Configuration;

import java.util.List;

public interface ResourceHandler {
    List<PrivilegedOperation> bootstrap(Configuration configuration) throws ResourceHandleException;

    List<PrivilegedOperation> preStart(AppWork appWork) throws ResourceHandleException;

    List<PrivilegedOperation> reacquireAppWork(String appWorkId) throws ResourceHandleException;

    List<PrivilegedOperation> updateAppWork(AppWork appWork) throws ResourceHandleException;

    List<PrivilegedOperation> postComplete(String appWorKId) throws ResourceHandleException;

    List<PrivilegedOperation> tearDown() throws ResourceHandleException;
}
