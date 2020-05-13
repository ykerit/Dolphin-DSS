package agent.appworkmanage;

import common.resource.Resource;
import common.struct.AppWorkId;

import java.io.IOException;

public class ResourceProcessorImp implements ResourceProcessor {

    @Override
    public void init(AppWorkExecutorImp executor) throws IOException {

    }

    @Override
    public void preExecute(AppWorkId appWorkId, Resource appWorkResource) throws IOException {

    }

    @Override
    public void postExecute(AppWorkId appWorkId) {

    }

    @Override
    public String getResourceOption(AppWorkId appWorkId) {
        return null;
    }
}
