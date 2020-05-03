package agent;

import agent.appworkmanage.appwork.AppWorkState;
import common.event.EventDispatcher;
import common.resource.Resource;
import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AgentStatusReporter extends AbstractService {

    private static final Logger log = LogManager.getLogger(AgentStatusReporter.class.getName());

    private final Context context;
    private final EventDispatcher dispatcher;

    private long agentId;
    private long nextHeartbeatInterval;
    private Resource totalResource;

    // AppWork
    private final Map<String, Long> recentlyStoppedAppWork;
    private final Map<String, AppWorkState> pendingCompleteAppWork;

    public AgentStatusReporter(Context context, EventDispatcher dispatcher) {
        super(AgentStatusReporter.class.getName());

        this.context = context;
        this.dispatcher = dispatcher;
        this.recentlyStoppedAppWork = new LinkedHashMap<>();
        this.pendingCompleteAppWork = new HashMap<>();
    }

    @Override
    protected void serviceInit() throws Exception {
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
