package agent.appworkmanage;

import common.service.AbstractService;

import java.util.HashMap;
import java.util.Map;

public class AppWorkMonitor extends AbstractService {
    private Map<String, AppWork> appWorks;
    public AppWorkMonitor() {
        super(AppWorkMonitor.class.getName());
        appWorks = new HashMap<>();
    }

    @Override
    protected void serviceInit() {
        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        super.serviceStop();
    }
}
