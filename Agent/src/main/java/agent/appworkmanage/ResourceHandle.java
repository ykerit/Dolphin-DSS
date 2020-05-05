package agent.appworkmanage;

import common.resource.Resource;

import java.io.IOException;

public interface ResourceHandle {
    void init(AppWorkExecutorImp executor) throws IOException;

    void preExecute(String appWorkId, Resource appWorkResource) throws IOException;

    void postExecute(String appWorkId);
}
