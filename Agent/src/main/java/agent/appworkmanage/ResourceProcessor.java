package agent.appworkmanage;

import common.resource.Resource;
import common.struct.AppWorkId;

import java.io.IOException;

public interface ResourceProcessor {
    void init(AppWorkExecutorImp executor) throws IOException;

    void preExecute(AppWorkId appWorkId, Resource appWorkResource) throws IOException;

    void postExecute(AppWorkId appWorkId);

    String getResourceOption(AppWorkId appWorkId);
}
