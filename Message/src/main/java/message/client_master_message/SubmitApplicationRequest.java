package message.client_master_message;

import common.context.ApplicationSubmission;
import message.MessageID;
import org.greatfree.message.container.Request;

public class SubmitApplicationRequest extends Request {
    private ApplicationSubmission submission;

    public SubmitApplicationRequest(ApplicationSubmission submission) {
        super(MessageID.SUBMIT_APPLICATION_REQUEST);
        this.submission = submission;
    }

    public ApplicationSubmission getSubmission() {
        return submission;
    }
}
