package api.app_master_message;

import org.greatfree.message.container.Request;

public class StopAppWorkRequest extends Request {
    public StopAppWorkRequest(int applicationID) {
        super(applicationID);
    }
}
