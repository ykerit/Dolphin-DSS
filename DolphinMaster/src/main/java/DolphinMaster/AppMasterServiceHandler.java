package DolphinMaster;

import api.app_master_message.*;
import common.exception.DolphinException;
import common.struct.ApplicationId;

public interface AppMasterServiceHandler {
    void init(AppMasterServiceContext context, AppMasterServiceHandler nextHandler);

    void registerAppMaster(ApplicationId applicationId, RegisterAppMasterRequest request, RegisterAppMasterResponse response) throws DolphinException;

    void allocate(ApplicationId applicationId, AllocateRequest request, AllocateResponse response) throws DolphinException;

    void finishAppMaster(ApplicationId applicationId, FinishAppMasterRequest request, FinishAppMasterResponse response);
}
