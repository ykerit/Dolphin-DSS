package agent.agentstatusreport;

import agent.AgentContext;
import common.event.EventProcessor;

public class ActionProcessor implements EventProcessor<ActionEvent> {
    private final AgentContext context;

    public ActionProcessor(AgentContext context) {
        this.context = context;
    }

    @Override
    public void process(ActionEvent event) {
        switch (event.getType()) {
            case EXPIRE_TOKEN:
                this.context.setToken((String) event.getData());
                break;
            case NONE:
                break;
        }
    }
}
