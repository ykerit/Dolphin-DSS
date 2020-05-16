package DolphinMaster.amlauncher;

import common.event.Event;
import common.event.EventProcessor;
import common.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppMasterLauncher extends AbstractService implements EventProcessor {
    private static final Logger log = LogManager.getLogger(AppMasterLauncher.class);

    public AppMasterLauncher() {
        super(AppMasterLauncher.class.getName());
    }

    @Override
    public void process(Event event) {

    }
}
