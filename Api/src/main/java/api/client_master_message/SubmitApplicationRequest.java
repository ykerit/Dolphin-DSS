package api.client_master_message;

import common.context.ApplicationSubmission;
import api.MessageID;
import common.struct.ApplicationId;
import org.greatfree.message.container.Request;

public class SubmitApplicationRequest extends Request {
    private ApplicationSubmission submission;
    private ApplicationId applicationId;

    public SubmitApplicationRequest(ApplicationSubmission submission, ApplicationId applicationId) {
        super(MessageID.SUBMIT_APPLICATION_REQUEST);
        this.submission = submission;
        this.applicationId = applicationId;
    }

    public ApplicationSubmission getSubmission() {
        return submission;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }
}
