package DolphinMaster.scheduler;

import agent.appworkmanage.appwork.AppWork;
import common.resource.Resource;

import java.util.List;
import java.util.Set;

public class Allocation {
    final List<AppWork> appWorks;
    final Set<String> strictAppWorks;
    final Set<String> fungibleAppWorks;
    final List<AppWork> increasedAppWorks;
    final List<AppWork> decreasedAppWorks;
    final List<AppWork> promotedAppWorks;
    final List<AppWork> demotedAppWorks;
    private final List<AppWork> previousAttemptAppWorks;
    private Resource resourceLimit;

    public Allocation(List<AppWork> appWorks, Resource resourceLimit,
                      Set<String> strictAppWorks, Set<String> fungibleAppWorks) {
        this(appWorks, resourceLimit, strictAppWorks, fungibleAppWorks, null, null);
    }

    public Allocation(List<AppWork> appWorks,
                      Resource resourceLimit,
                      Set<String> strictAppWorks,
                      Set<String> fungibleAppWorks,
                      List<AppWork> increasedAppWorks,
                      List<AppWork> decreasedAppWorks) {
        this(appWorks, resourceLimit, strictAppWorks, fungibleAppWorks,
                increasedAppWorks, decreasedAppWorks,
                null, null, null);
    }

    public Allocation(List<AppWork> appWorks,
                      Resource resourceLimit,
                      Set<String> strictAppWorks,
                      Set<String> fungibleAppWorks,
                      List<AppWork> increasedAppWorks,
                      List<AppWork> decreasedAppWorks,
                      List<AppWork> promotedAppWorks,
                      List<AppWork> demotedAppWorks,
                      List<AppWork> previousAttemptAppWorks) {
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

    public List<AppWork> getAppWorks() {
        return appWorks;
    }

    public Resource getResourceLimit() {
        return resourceLimit;
    }

    public Set<String> getStrictAppWorks() {
        return strictAppWorks;
    }

    public Set<String> getAppWorkPreemption() {
        return fungibleAppWorks;
    }

    public List<AppWork> getIncreasedAppWorks() {
        return increasedAppWorks;
    }

    public List<AppWork> getDecreasedAppWorks() {
        return decreasedAppWorks;
    }

    public List<AppWork> getPromotedAppWorks() {
        return promotedAppWorks;
    }

    public List<AppWork> getDemotedAppWorks() {
        return demotedAppWorks;
    }

    public List<AppWork> getPreviousAttemptAppWorks() {
        return previousAttemptAppWorks;
    }

    public void setResourceLimit(Resource resourceLimit) {
        this.resourceLimit = resourceLimit;
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
