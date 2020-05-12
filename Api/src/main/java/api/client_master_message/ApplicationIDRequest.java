package api.client_master_message;

import api.MessageID;
import org.greatfree.message.container.Request;

public class ApplicationIDRequest extends Request {

    public ApplicationIDRequest() {
        super(MessageID.APPLICATION_ID_REQUEST);
    }
}
