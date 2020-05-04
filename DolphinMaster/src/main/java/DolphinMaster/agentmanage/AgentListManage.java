package DolphinMaster.agentmanage;

import common.service.AbstractService;
import common.struct.AgentId;
import common.struct.Pair;

import java.util.HashMap;
import java.util.Map;

public class AgentListManage extends AbstractService {
    // white list
    private Map<Long, AgentId> includeAgents;
    // black list
    private Map<Long, AgentId> excludeAgents;
    public AgentListManage() {
        super(AgentListManage.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        includeAgents = new HashMap<>();
        excludeAgents = new HashMap<>();
        super.serviceInit();
    }

    public synchronized void addInclude(long id, AgentId agentID) {
        if (includeAgents.get(id) == null) {
            excludeAgents.put(id, agentID);
        }
    }

    public synchronized void addExclude(long id, AgentId agentID) {
        if (excludeAgents.get(id) == null) {
            excludeAgents.put(id, agentID);
        }
    }

    public synchronized void moveToInclude(long id) {
        AgentId agentID = excludeAgents.get(id);
        if (agentID != null) {
            includeAgents.put(id, agentID);
            excludeAgents.remove(id);
        }
    }

    public synchronized void moveToExclude(long id) {
        AgentId agentID = includeAgents.get(id);
        if (agentID != null) {
            excludeAgents.put(id, agentID);
            includeAgents.remove(id);
        }
    }

    public Pair<Integer, Integer> size() {
        return new Pair<>(includeAgents.size(), excludeAgents.size());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("white list:\n");
        for (AgentId agentID : includeAgents.values()) {
            buffer.append(agentID.toString());
        }
        buffer.append("black list:\n");
        for (AgentId agentID : excludeAgents.values()) {
            buffer.append(agentID.toString());
        }
        return buffer.toString();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
    }

}
