package agent.event;

import common.event.AbstractEvent;
import common.event.ActionType;

public class ActionEvent extends AbstractEvent<ActionType> {
    private Object data;
    public ActionEvent(ActionType actionType, Object data) {
        super(actionType);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
