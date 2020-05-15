package api.app_master_message;

import api.MessageID;
import org.greatfree.message.ServerMessage;

public class AllocateResponse extends ServerMessage {

    public AllocateResponse() {
        super(MessageID.ALLOCATE_RESPONSE);
    }
}
