package api.app_master_message;

import api.MessageID;
import common.resource.Resource;
import org.greatfree.message.ServerMessage;

public class RegisterAppMasterResponse extends ServerMessage {
    private Resource maxResourceCapability;
    public RegisterAppMasterResponse() {
        super(MessageID.APP_MASTER_REGISTER_RESPONSE);
    }

    public Resource getMaxResourceCapability() {
        return maxResourceCapability;
    }

    public void setMaxResourceCapability(Resource maxResourceCapability) {
        this.maxResourceCapability = maxResourceCapability;
    }
}
