package agent.appworkmanage;

import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Notification;
import org.greatfree.message.container.Request;
import org.greatfree.server.container.ServerTask;

public class AppWorkTask implements ServerTask {
    @Override
    public void processNotification(Notification notification) {

    }

    @Override
    public ServerMessage processRequest(Request request) {
        return null;
    }
}
