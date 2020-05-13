package agent.message;

import agent.appworkmanage.appwork.AppWork;
import agent.status.AgentAction;
import api.MessageID;
import common.resource.Resource;
import common.struct.ApplicationId;
import org.greatfree.message.ServerMessage;

import java.util.Collection;
import java.util.List;

public class AgentHeartBeatResponse extends ServerMessage {
    private AgentAction action;
    private String masterToken;
    private String tips;

    private List<String> appWorksToCleanup;
    private List<String> appWorksToBeRemoved;
    private List<ApplicationId> applicationsToCleanup;
    private List<AppWork> appWorksToUpdate;
    private List<AppWork> appWorksToDecrease;

    public AgentHeartBeatResponse() {
        super(MessageID.HEART_BEAT_RESPONSE);
    }

    public AgentHeartBeatResponse(AgentAction action, String masterToken) {
        super(MessageID.HEART_BEAT_RESPONSE);
        this.action = action;
        this.masterToken = masterToken;
    }

    public AgentAction getAction() {
        return action;
    }

    public void setAction(AgentAction action) {
        this.action = action;
    }

    public String getMasterToken() {
        return masterToken;
    }

    public void setMasterToken(String masterToken) {
        this.masterToken = masterToken;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public List<String> getAppWorksToCleanup() {
        return appWorksToCleanup;
    }

    public void setAppWorksToCleanup(List<String> appWorksToCleanup) {
        this.appWorksToCleanup = appWorksToCleanup;
    }

    public List<String> getAppWorksToBeRemoved() {
        return appWorksToBeRemoved;
    }

    public void setAppWorksToBeRemoved(List<String> appWorksToBeRemoved) {
        this.appWorksToBeRemoved = appWorksToBeRemoved;
    }

    public List<ApplicationId> getApplicationsToCleanup() {
        return applicationsToCleanup;
    }

    public void setApplicationsToCleanup(List<ApplicationId> applicationsToCleanup) {
        this.applicationsToCleanup = applicationsToCleanup;
    }

    public List<AppWork> getAppWorksToUpdate() {
        return appWorksToUpdate;
    }

    public void setAppWorksToUpdate(Collection<AppWork> appWorksToUpdate) {
        this.appWorksToUpdate.addAll(appWorksToUpdate);
    }

    public List<AppWork> getAppWorksToDecrease() {
        return appWorksToDecrease;
    }

    public void setAppWorksToDecrease(List<AppWork> appWorksToDecrease) {
        this.appWorksToDecrease = appWorksToDecrease;
    }
}
