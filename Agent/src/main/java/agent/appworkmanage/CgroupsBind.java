package agent.appworkmanage;

import common.resource.Resource;
import common.resource.ResourceCollector;

import java.io.IOException;

public class CgroupsBind implements Bind{

    @Override
    public void init(AppWorkExecutorImp executor) throws IOException {
        init(executor, new ResourceCollector());
    }

    void init(AppWorkExecutorImp executor, ResourceCollector collector) throws IOException {
    }

    @Override
    public void preExecute(String appWorkId, Resource appWorkResource) throws IOException {

    }

    @Override
    public void postExecute(String appWorkId) {

    }
}
