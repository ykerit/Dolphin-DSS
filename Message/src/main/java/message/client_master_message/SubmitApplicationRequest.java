package message.client_master_message;

import common.context.ApplicationSubmissionContext;
import message.MessageID;
import org.greatfree.message.container.Request;

public class SubmitApplicationRequest extends Request {
    private long uniApplicationID;
    private String applicationName;
    private int priority;
    private String user;
    private ApplicationSubmissionContext applicationSubmissionContext;

    public SubmitApplicationRequest(long applicationID, String applicationName, String user, int priority, ApplicationSubmissionContext applicationSubmissionContext) {
        super(MessageID.SUBMIT_APPLICATION_REQUEST);
        this.applicationSubmissionContext = applicationSubmissionContext;
        this.uniApplicationID = applicationID;
        this.applicationName = applicationName;
        this.priority = priority;
    }

    public long getUniApplicationID() {
        return uniApplicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getPriority() {
        return priority;
    }

    public String getUser() {
        return user;
    }

    public ApplicationSubmissionContext getApplicationSubmissionContext() {
        return applicationSubmissionContext;
    }
}
