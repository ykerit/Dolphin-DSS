package agent.appworkmanage;

import common.resource.Resource;

import java.io.IOException;

public class ResourceProcessorImp implements ResourceProcessor {

    @Override
    public void init(AppWorkExecutorImp executor) throws IOException {

    }

    @Override
    public void preExecute(String appWorkId, Resource appWorkResource) throws IOException {

    }

    @Override
    public void postExecute(String appWorkId) {

    }

    @Override
    public String getResourceOption(String appWorkId) {
        return null;
    }
}
