package DolphinMaster;

import common.service.AbstractService;
import org.greatfree.server.container.ServerContainer;

public class AppMasterService extends AbstractService {
    private ServerContainer server;

    public AppMasterService() {
        super(AppMasterService.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
    }
}
