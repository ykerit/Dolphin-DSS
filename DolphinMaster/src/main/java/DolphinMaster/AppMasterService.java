package DolphinMaster;

import DolphinMaster.app.AMLiveLinessMonitor;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.servertask.AppMasterTask;
import api.app_master_message.AllocateRequest;
import api.app_master_message.AllocateResponse;
import api.app_master_message.RegisterAppMasterRequest;
import api.app_master_message.RegisterAppMasterResponse;
import common.service.AbstractService;
import common.struct.ApplicationId;
import config.DefaultServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.server.container.ServerContainer;

import java.util.concurrent.ConcurrentHashMap;

public class AppMasterService extends AbstractService {
    private ServerContainer server;

    private static final Logger log = LogManager.getLogger(AppMasterService.class);

    private final AMLiveLinessMonitor amLiveLinessMonitor;
    private ResourceScheduler scheduler;
    private final DolphinContext context;
    private final ConcurrentHashMap<ApplicationId, AllocateResponseLock> responseMap = new ConcurrentHashMap<>();
    private final AppMasterPipeLine pipeLine;

    public AppMasterService(DolphinContext context, ResourceScheduler resourceScheduler) {
        super(AppMasterService.class.getName());
        this.context = context;
        this.amLiveLinessMonitor = context.getAMLiveLinessMonitor();
        this.scheduler = resourceScheduler;
        this.pipeLine = new AppMasterPipeLine(new DefaultAMHandler());
    }

    @Override
    protected void serviceInit() throws Exception {
        server = new ServerContainer(DefaultServerConfig.APP_MASTER_TRACKER_PORT, new AppMasterTask(this));
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        server.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        server.stop(2000L);
        super.serviceStop();
    }

    public RegisterAppMasterResponse registerAppMaster(RegisterAppMasterRequest request) {
        return null;
    }

    public AllocateResponse allocate(AllocateRequest request) {
        return null;
    }

    public static class AllocateResponseLock {
        private AllocateResponse response;
        public AllocateResponseLock(AllocateResponse response) {
            this.response = response;
        }
        public synchronized AllocateResponse getAllocateResponse() {
            return response;
        }
        public synchronized void setAllocateResponse(AllocateResponse response) {
            this.response = response;
        }
    }
}
