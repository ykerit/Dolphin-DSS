package DolphinMaster;

import DolphinMaster.app.App;
import DolphinMaster.app.AppMasterRegisterEvent;
import DolphinMaster.app.AppState;
import DolphinMaster.scheduler.Allocation;
import api.app_master_message.*;
import common.context.ApplicationSubmission;
import common.exception.DolphinException;
import common.exception.InvalidResourceRequestException;
import common.resource.Resource;
import common.resource.Resources;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DefaultAMHandler implements AppMasterServiceHandler {

    private static final Logger log = LogManager.getLogger(DefaultAMHandler.class);

    private final static List<RemoteAppWork> EMPTY_APP_WORK_LIST = new ArrayList<>();
    protected static final Allocation EMPTY_ALLOCATION = new Allocation(EMPTY_APP_WORK_LIST,
            Resources.createResource(0), null, null);
    private DolphinContext context;

    @Override
    public void init(AppMasterServiceContext context, AppMasterServiceHandler nextHandler) {
        this.context = (DolphinContext)context;
    }

    @Override
    public void registerAppMaster(ApplicationId applicationId, RegisterAppMasterRequest request, RegisterAppMasterResponse response) throws DolphinException {
        App app = context.getApps().get(applicationId);
        log.info("AM Register " + applicationId);
        context.getDolphinDispatcher().getEventProcessor().process(new AppMasterRegisterEvent(applicationId));
        response.setMaxResourceCapability(context.getScheduler().getMaximumResourceCapability(app.getPool()));
    }

    @Override
    public void allocate(ApplicationId applicationId, AllocateRequest request, AllocateResponse response) throws DolphinException {
        List<ResourceRequest> ask = request.getAsk();
        List<AppWorkId> release = request.getRelease();
        App app = context.getApps().get(applicationId);
        ApplicationSubmission submission = app.getApplicationSubmission();
        Resource maxCapacity = context.getScheduler().getMaximumResourceCapability(app.getPool());

        try {
            DolphinUtils.normalizeAndValidateRequest(ask, maxCapacity, app.getPool(), context.getScheduler(), context);
        } catch (InvalidResourceRequestException e) {
            log.warn("Invalid request by application " + applicationId, e);
            throw e;
        }

        Allocation allocation;
        AppState state = app.getState();
        if (state.equals(AppState.FINISHING)) {
            log.warn(applicationId + " is in " + state + " state, ignore AppWork allocate request");
            allocation = EMPTY_ALLOCATION;
        } else {
            allocation = context.getScheduler().allocate(applicationId, ask, release);
        }

        response.setAllocatedAppWorks(allocation.getAppWorks());
        response.setAvailResources(allocation.getResourceLimit());
        response.setNumClusterNodes(context.getScheduler().getNumClusterNodes());
    }

    @Override
    public void finishAppMaster(ApplicationId applicationId, FinishAppMasterRequest request, FinishAppMasterResponse response) {

    }

    private void handleNodeUpdate(App app, AllocateResponse allocateResponse) {

    }
}
