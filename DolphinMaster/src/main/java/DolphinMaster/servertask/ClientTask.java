package DolphinMaster.servertask;

import DolphinMaster.ClientService;
import api.MessageID;
import api.client_master_message.ApplicationIDRequest;
import api.client_master_message.ApplicationIDResponse;
import api.client_master_message.SubmitApplicationRequest;
import common.struct.ApplicationId;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Notification;
import org.greatfree.message.container.Request;
import org.greatfree.server.container.ServerTask;

public class ClientTask implements ServerTask {
    private final ClientService clientService;

    public ClientTask(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void processNotification(Notification notification) {
    }

    @Override
    public ServerMessage processRequest(Request request) {
        switch (request.getApplicationID()) {
            case MessageID.APPLICATION_ID_REQUEST:
                ApplicationId applicationId = clientService.getNewApplicationId((ApplicationIDRequest) request);
                return new ApplicationIDResponse(applicationId);
            case MessageID.SUBMIT_APPLICATION_REQUEST:
                return clientService.submitApplication((SubmitApplicationRequest) request);
        }
        return null;
    }
}
