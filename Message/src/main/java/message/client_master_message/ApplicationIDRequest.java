package message.client_master_message;

import message.MessageID;
import org.greatfree.message.container.Request;

public class ApplicationIDRequest extends Request {

    public ApplicationIDRequest() {
        super(MessageID.APPLICATION_ID_REQUEST);
    }
}
