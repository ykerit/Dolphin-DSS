package DolphinMaster.scheduler.fica;

import DolphinMaster.app.App;
import DolphinMaster.node.Node;
import DolphinMaster.scheduler.SchedulerApplication;
import DolphinMaster.scheduler.SchedulerNode;
import DolphinMaster.schedulerunit.SchedulerUnit;
import common.struct.ApplicationId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FicaSchedulerNode extends SchedulerNode {
    private static final Logger log = LogManager.getLogger(FicaSchedulerNode.class);

    public FicaSchedulerNode(Node node) {
        super(node);
    }

    @Override
    public void reserveResource(SchedulerApplication application, SchedulerUnit unit) {
        SchedulerUnit reservedUnit = getReservedSchedulerUnit();
        if (reservedUnit != null) {
            if (!unit.getAppWork().getAgentId().equals(getNodeId())) {
                throw new IllegalStateException("Trying to reserve" +
                        " SchedulerUnit " + unit + " on node " +
                        unit.getReservedNode() + " when currently " + " reserved resource " +
                        reservedUnit + " on node" + reservedUnit.getReservedNode());
            }
            if (log.isDebugEnabled()) {
                log.debug("Update reserved AppWork " + unit.getAppWork().getAppWorkId()
                        + " on node" + this + " for application " + application.getApplicationId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reserved AppWork " + unit.getAppWork().getAppWorkId() + " on node" + this +
                        " for application " + application.getApplicationId());
            }
        }
        setReservedSchedulerUnit(unit);
    }

    @Override
    public void unreserveResource(SchedulerApplication application) {
        if (getReservedSchedulerUnit() != null &&
        getReservedSchedulerUnit().getAppWork() != null &&
        getReservedSchedulerUnit().getAppWork().getAppWorkId() != null &&
        getReservedSchedulerUnit().getAppWork().getAppWorkId().getApplicationId() != null) {
            ApplicationId reservedApplication = getReservedSchedulerUnit().getAppWork().getAppWorkId().getApplicationId();
            if (!reservedApplication.equals(application.getApplicationId())) {
                throw  new IllegalStateException("Trying to unreserve " + " for application " +
                        application.getApplicationId() + " when currently reserved " + " for application " +
                        reservedApplication + " on node " + this);
            }
        }
        setReservedSchedulerUnit(null);
    }
}
