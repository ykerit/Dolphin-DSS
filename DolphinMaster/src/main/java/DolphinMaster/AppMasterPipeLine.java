package DolphinMaster;

import api.app_master_message.*;
import common.exception.DolphinException;
import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppMasterPipeLine implements AppMasterServiceHandler{

    private static final Logger log = LogManager.getLogger(AppMasterPipeLine.class);
    private AppMasterServiceHandler head;
    private DolphinContext context;

    public AppMasterPipeLine(AppMasterServiceHandler handler) {
        this.head = handler;
    }

    @Override
    public void init(AppMasterServiceContext context, AppMasterServiceHandler nextHandler) {
        this.context = (DolphinContext) context;
        this.head.init(context, null);
    }

    public synchronized void addHandler(AppMasterServiceHandler handler) {
        handler.init(this.context, this.head);
        this.head = handler;
    }

    @Override
    public void registerAppMaster(ApplicationId applicationId, RegisterAppMasterRequest request, RegisterAppMasterResponse response) throws DolphinException {
        this.head.registerAppMaster(applicationId, request, response);
    }

    @Override
    public void allocate(ApplicationId applicationId, AllocateRequest request, AllocateResponse response) throws DolphinException {
        this.head.allocate(applicationId, request, response);
    }

    @Override
    public void finishAppMaster(ApplicationId applicationId, FinishAppMasterRequest request, FinishAppMasterResponse response) {

    }
}
