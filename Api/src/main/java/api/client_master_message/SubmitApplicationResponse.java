package api.client_master_message;

import api.MessageID;
import org.greatfree.message.ServerMessage;

public class SubmitApplicationResponse extends ServerMessage {
    public SubmitApplicationResponse() {
        super(MessageID.SUBMIT_APPLICATION_RESPONSE);
    }

}
