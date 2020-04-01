package DolphinMaster.server_task;

import common.util.SnowFlakeGenerator;
import message.client_master_message.ApplicationIDResponse;
import message.MessageID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Notification;
import org.greatfree.message.container.Request;
import org.greatfree.server.container.ServerTask;

public class ClientTask implements ServerTask {
    private static final Logger log = LogManager.getLogger(ClientTask.class.getName());
    @Override
    public void processNotification(Notification notification) {
        switch (notification.getApplicationID()) {

        }
    }

    @Override
    public ServerMessage processRequest(Request request) {
        log.info("request: " + request.getApplicationID());
        switch (request.getApplicationID()) {
            case MessageID.APPLICATION_ID_REQUEST:
                log.info("RECEIVE MESSAGE: GET_APPLICATION_ID");
                return new ApplicationIDResponse(SnowFlakeGenerator.GEN().nextId());
            case MessageID.SUBMIT_APPLICATION_REQUEST:
        }
        return null;
    }
}
