package api.app_master_message;

import api.MessageID;
import org.greatfree.message.container.Request;

public class RegisterAppMasterRequest extends Request {
    public RegisterAppMasterRequest() {
        super(MessageID.APP_MASTER_REGISTER_REQUEST);
    }
}
