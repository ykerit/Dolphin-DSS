package DolphinMaster.node;


import DolphinMaster.DolphinContext;
import common.event.EventProcessor;
import common.resource.Resource;
import common.resource.ResourceUtilization;
import common.struct.AgentId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeImp implements Node, EventProcessor<NodeEvent> {

    private static final Logger log = LogManager.getLogger(NodeImp.class.getName());

    private final AgentId agentId;
    private final DolphinContext context;
    private final String hostName;
    private final String nodeAddress;

    private NodeState nodeState;

    private ResourceUtilization appWorksUtilization;
    private ResourceUtilization nodeUtilization;
    private volatile Resource physicalResource;

    public NodeImp(AgentId id, DolphinContext context, String hostName, Resource capability, Resource physicalResource) {
        this.agentId = id;
        this.context = context;
        this.hostName = hostName;
        this.nodeAddress = id.getLocalIP();
    }

    @Override
    public AgentId getNodeId() {
        return agentId;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getNodeAddress() {
        return nodeAddress;
    }

    @Override
    public Resource getTotalCapability() {
        return null;
    }

    @Override
    public Resource getPhysicalResource() {
        return null;
    }

    @Override
    public void rsyncCapability() {

    }

    @Override
    public void process(NodeEvent event) {

    }

    public NodeState getState() {
        return nodeState;
    }
}
