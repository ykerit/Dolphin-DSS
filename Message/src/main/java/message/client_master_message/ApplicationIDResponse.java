package message.client_master_message;

import message.MessageID;
import org.greatfree.message.ServerMessage;

public class ApplicationIDResponse extends ServerMessage {
    private long ID;
    public ApplicationIDResponse(long id) {
        super(MessageID.APPLICATION_ID_RESPONSE);
        this.ID = id;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
