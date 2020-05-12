package api.client_master_message;

import api.MessageID;
import common.struct.ApplicationId;
import org.greatfree.message.ServerMessage;

public class ApplicationIDResponse extends ServerMessage {
    private ApplicationId applicationId;
    public ApplicationIDResponse(ApplicationId id) {
        super(MessageID.APPLICATION_ID_RESPONSE);
        this.applicationId = id;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }
}
