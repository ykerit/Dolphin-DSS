package api.app_master_message;

import common.struct.AppWorkId;
import org.greatfree.message.ServerMessage;

import java.util.List;

public class StartAppWorksResponse extends ServerMessage {

    private final List<AppWorkId> succeededAppWorks;

    public StartAppWorksResponse(List<AppWorkId> list) {
        super(16);
        this.succeededAppWorks = list;
    }

    public List<AppWorkId> getSucceededAppWorks() {
        return succeededAppWorks;
    }
}
