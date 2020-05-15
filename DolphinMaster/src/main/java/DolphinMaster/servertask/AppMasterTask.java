package DolphinMaster.servertask;

import DolphinMaster.AppMasterService;
import api.MessageID;
import api.app_master_message.AllocateRequest;
import api.app_master_message.RegisterAppMasterRequest;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Notification;
import org.greatfree.message.container.Request;
import org.greatfree.server.container.ServerTask;

public class AppMasterTask implements ServerTask {
    private final AppMasterService appMasterService;

    public AppMasterTask(AppMasterService appMasterService) {
        this.appMasterService = appMasterService;
    }

    @Override
    public void processNotification(Notification notification) {
    }

    @Override
    public ServerMessage processRequest(Request request) {
        switch (request.getApplicationID()) {
            case MessageID.APP_MASTER_REGISTER_REQUEST:
                return appMasterService.registerAppMaster((RegisterAppMasterRequest) request);
            case MessageID.ALLOCATE_REQUEST:
                return appMasterService.allocate((AllocateRequest) request);
        }
        return null;
    }
}
