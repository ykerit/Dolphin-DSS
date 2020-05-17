package agent.message;

import agent.appworkmanage.appwork.AppWork;
import agent.status.AgentAction;
import api.MessageID;
import common.resource.Resource;
import common.struct.AppWorkId;
import common.struct.ApplicationId;
import common.struct.RemoteAppWork;
import org.greatfree.message.ServerMessage;

import java.util.Collection;
import java.util.List;

public class AgentHeartBeatResponse extends ServerMessage {
    private AgentAction action;
    private String masterToken;
    private String tips;

    private List<AppWorkId> appWorksToCleanup;
    private List<AppWorkId> appWorksToBeRemoved;
    private List<ApplicationId> applicationsToCleanup;
    private List<RemoteAppWork> appWorksToUpdate;
    private List<RemoteAppWork> appWorksToDecrease;

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

    public List<AppWorkId> getAppWorksToCleanup() {
        return appWorksToCleanup;
    }

    public void setAppWorksToCleanup(List<AppWorkId> appWorksToCleanup) {
        this.appWorksToCleanup = appWorksToCleanup;
    }

    public List<AppWorkId> getAppWorksToBeRemoved() {
        return appWorksToBeRemoved;
    }

    public void setAppWorksToBeRemoved(List<AppWorkId> appWorksToBeRemoved) {
        this.appWorksToBeRemoved = appWorksToBeRemoved;
    }

    public List<ApplicationId> getApplicationsToCleanup() {
        return applicationsToCleanup;
    }

    public void setApplicationsToCleanup(List<ApplicationId> applicationsToCleanup) {
        this.applicationsToCleanup = applicationsToCleanup;
    }

    public List<RemoteAppWork> getAppWorksToUpdate() {
        return appWorksToUpdate;
    }

    public void setAppWorksToUpdate(Collection<RemoteAppWork> appWorksToUpdate) {
        this.appWorksToUpdate.addAll(appWorksToUpdate);
    }

    public List<RemoteAppWork> getAppWorksToDecrease() {
        return appWorksToDecrease;
    }

    public void setAppWorksToDecrease(List<RemoteAppWork> appWorksToDecrease) {
        this.appWorksToDecrease = appWorksToDecrease;
    }
}
