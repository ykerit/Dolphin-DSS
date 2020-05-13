package agent.appworkmanage.cgroups;

import agent.appworkmanage.appwork.AppWork;
import common.Privileged.PrivilegedOperation;
import common.exception.ResourceHandleException;
import common.struct.AppWorkId;
import config.Configuration;

import java.util.List;

public interface ResourceHandler {
    List<PrivilegedOperation> bootstrap(Configuration configuration) throws ResourceHandleException;

    List<PrivilegedOperation> preStart(AppWork appWork) throws ResourceHandleException;

    List<PrivilegedOperation> reacquireAppWork(AppWorkId appWorkId) throws ResourceHandleException;

    List<PrivilegedOperation> updateAppWork(AppWork appWork) throws ResourceHandleException;

    List<PrivilegedOperation> postComplete(AppWorkId appWorKId) throws ResourceHandleException;

    List<PrivilegedOperation> tearDown() throws ResourceHandleException;
}
