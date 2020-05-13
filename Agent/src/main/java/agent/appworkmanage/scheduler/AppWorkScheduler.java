package agent.appworkmanage.scheduler;

import agent.Context;
import agent.appworkmanage.appwork.AppWork;
import agent.appworkmanage.appwork.AppWorkExecType;
import common.event.EventDispatcher;
import common.event.EventProcessor;
import common.service.AbstractService;
import common.struct.AppWorkId;

import java.util.LinkedHashMap;

public class AppWorkScheduler extends AbstractService implements EventProcessor<AppWorkSchedulerEvent> {

    private final EventDispatcher dispatcher;
    private final Context context;
    private final int maxOppSize;

    // queue of promise to run appWork waiting for resource.
    private final LinkedHashMap<AppWorkId, AppWork> promiseAppWorks = new LinkedHashMap<>();
    // queue of opportunistic appWork waiting for resource to run.
    private final LinkedHashMap<AppWorkId, AppWork> opportunisticAppWorks = new LinkedHashMap<>();
    // queue of reusable appWork
    private final LinkedHashMap<AppWorkId, AppWork> reusableAppWorks = new LinkedHashMap<>();
    // queue of appWork status is RUNNING or IDLE.
    private final LinkedHashMap<AppWorkId, AppWork> runningAppWorks = new LinkedHashMap<>();

    public AppWorkScheduler(Context context, EventDispatcher dispatcher, int maxOppSize) {
        super(AppWorkScheduler.class.getName());
        this.dispatcher = dispatcher;
        this.context = context;
        this.maxOppSize = maxOppSize;
    }

    @Override
    public void process(AppWorkSchedulerEvent event) {
        switch (event.getType()) {
            case SCHEDULE_APP_WORK:
                scheduleAppWork(event.getAppWork());
                break;
            case UPDATE_APP_WORK:
                break;
            case APP_WORK_COMPLETE:
                break;
            case RECOVERY_COMPLETED:
                break;
            case SHED_QUEUED_APP_WORK:
                break;
            default:
                break;
        }
    }

    protected void scheduleAppWork(AppWork appWork) {

    }

    private boolean enqueueAppWork(AppWork appWork) {
        boolean isPromise = appWork.getExecType() == AppWorkExecType.PROMISE;

        boolean isQueued = false;
        if (isPromise) {
            promiseAppWorks.put(appWork.getAppWorkId(), appWork);
            isQueued = true;
        } else {
            if (opportunisticAppWorks.size() < maxOppSize) {
                opportunisticAppWorks.put(appWork.getAppWorkId(), appWork);
                isQueued = true;
            } else {
            }
        }

        if (isQueued) {

        }
        return isQueued;
    }
}
