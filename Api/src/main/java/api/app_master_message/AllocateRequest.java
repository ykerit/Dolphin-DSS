package api.app_master_message;

import common.struct.AppWorkId;
import org.greatfree.message.container.Request;

import java.util.List;

public class AllocateRequest extends Request {

    private List<ResourceRequest> ask;
    private List<AppWorkId> release;

    public AllocateRequest(int applicationID) {
        super(applicationID);
    }

    public void setAsk(List<ResourceRequest> ask) {
        this.ask = ask;
    }

    public List<AppWorkId> getRelease() {
        return release;
    }

    public List<ResourceRequest> getAsk() {
        return ask;
    }

    public void setRelease(List<AppWorkId> release) {
        this.release = release;
    }
}
