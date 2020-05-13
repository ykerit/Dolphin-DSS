package DolphinMaster.scheduler;

import common.resource.Resource;
import common.struct.RemoteAppWork;

import java.util.List;
import java.util.Set;

public class Allocation {
    final List<RemoteAppWork> appWorks;
    final Set<String> strictAppWorks;
    final Set<String> fungibleAppWorks;
    final List<RemoteAppWork> increasedAppWorks;
    final List<RemoteAppWork> decreasedAppWorks;
    final List<RemoteAppWork> promotedAppWorks;
    final List<RemoteAppWork> demotedAppWorks;
    private final List<RemoteAppWork> previousAttemptAppWorks;
    private Resource resourceLimit;

    public Allocation(List<RemoteAppWork> appWorks, Resource resourceLimit,
                      Set<String> strictAppWorks, Set<String> fungibleAppWorks) {
        this(appWorks, resourceLimit, strictAppWorks, fungibleAppWorks, null, null);
    }

    public Allocation(List<RemoteAppWork> appWorks,
                      Resource resourceLimit,
                      Set<String> strictAppWorks,
                      Set<String> fungibleAppWorks,
                      List<RemoteAppWork> increasedAppWorks,
                      List<RemoteAppWork> decreasedAppWorks) {
        this(appWorks, resourceLimit, strictAppWorks, fungibleAppWorks,
                increasedAppWorks, decreasedAppWorks,
                null, null, null);
    }

    public Allocation(List<RemoteAppWork> appWorks,
                      Resource resourceLimit,
                      Set<String> strictAppWorks,
                      Set<String> fungibleAppWorks,
                      List<RemoteAppWork> increasedAppWorks,
                      List<RemoteAppWork> decreasedAppWorks,
                      List<RemoteAppWork> promotedAppWorks,
                      List<RemoteAppWork> demotedAppWorks,
                      List<RemoteAppWork> previousAttemptAppWorks) {
        this.appWorks = appWorks;
        this.strictAppWorks = strictAppWorks;
        this.fungibleAppWorks = fungibleAppWorks;
        this.increasedAppWorks = increasedAppWorks;
        this.decreasedAppWorks = decreasedAppWorks;
        this.promotedAppWorks = promotedAppWorks;
        this.demotedAppWorks = demotedAppWorks;
        this.previousAttemptAppWorks = previousAttemptAppWorks;
        this.resourceLimit = resourceLimit;
    }

    public List<RemoteAppWork> getAppWorks() {
        return appWorks;
    }

    public Resource getResourceLimit() {
        return resourceLimit;
    }

    public void setResourceLimit(Resource resourceLimit) {
        this.resourceLimit = resourceLimit;
    }

    public Set<String> getStrictAppWorks() {
        return strictAppWorks;
    }

    public Set<String> getAppWorkPreemption() {
        return fungibleAppWorks;
    }

    public List<RemoteAppWork> getIncreasedAppWorks() {
        return increasedAppWorks;
    }

    public List<RemoteAppWork> getDecreasedAppWorks() {
        return decreasedAppWorks;
    }

    public List<RemoteAppWork> getPromotedAppWorks() {
        return promotedAppWorks;
    }

    public List<RemoteAppWork> getDemotedAppWorks() {
        return demotedAppWorks;
    }

    public List<RemoteAppWork> getPreviousAttemptAppWorks() {
        return previousAttemptAppWorks;
    }

    @Override
    public String toString() {
        return "Allocation{" +
                "appWorks=" + appWorks +
                ", strictAppWorks=" + strictAppWorks +
                ", fungibleAppWorks=" + fungibleAppWorks +
                ", increasedAppWorks=" + increasedAppWorks +
                ", decreasedAppWorks=" + decreasedAppWorks +
                ", promotedAppWorks=" + promotedAppWorks +
                ", demotedAppWorks=" + demotedAppWorks +
                ", previousAttemptAppWorks=" + previousAttemptAppWorks +
                ", resourceLimit=" + resourceLimit +
                '}';
    }
}
