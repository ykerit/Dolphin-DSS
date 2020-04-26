package message.client_master_message;

import message.MessageID;
import org.greatfree.message.ServerMessage;

public class ApplicationIDResponse extends ServerMessage {
    private long applicationId;
    public ApplicationIDResponse(long id) {
        super(MessageID.APPLICATION_ID_RESPONSE);
        this.applicationId = id;
    }

    public long getApplicationId() {
        return applicationId;
    }
}
