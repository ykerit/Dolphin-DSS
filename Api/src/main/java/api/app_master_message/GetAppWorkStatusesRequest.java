package api.app_master_message;

import org.greatfree.message.container.Request;

public class GetAppWorkStatusesRequest extends Request {
    public GetAppWorkStatusesRequest(int applicationID) {
        super(applicationID);
    }
}
