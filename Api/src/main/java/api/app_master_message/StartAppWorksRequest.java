package api.app_master_message;

import api.MessageID;
import org.greatfree.message.container.Request;

import java.util.List;

public class StartAppWorksRequest extends Request {
    private List<StartAppWorkRequest> requests;
    public StartAppWorksRequest(List<StartAppWorkRequest> requests) {
        super(MessageID.START_APP_WORK_REQUEST);
        this.requests = requests;
    }

    public List<StartAppWorkRequest> getRequests() {
        return requests;
    }
}
