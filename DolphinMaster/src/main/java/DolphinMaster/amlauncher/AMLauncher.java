package DolphinMaster.amlauncher;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import common.context.AppWorkLaunchContext;
import common.context.ApplicationSubmission;
import common.event.EventProcessor;
import common.struct.AppWorkId;
import common.struct.RemoteAppWork;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AMLauncher implements Runnable {
    private static final Logger log = LogManager.getLogger(AMLauncher.class);

    private final App application;
    private final Configuration configuration;
    private final AMLauncherEventType eventType;
    private final DolphinContext dolphinContext;
    private final RemoteAppWork masterAppWork;
    private EventProcessor processor;

    public AMLauncher(DolphinContext dolphinContext, App schedulerApplication,
                      AMLauncherEventType eventType, Configuration configuration) {
        this.application = schedulerApplication;
        this.configuration = configuration;
        this.eventType = eventType;
        this.dolphinContext = dolphinContext;
        this.masterAppWork = application.getMasterAppWork();
    }

    private void launch() throws IOException {
        AppWorkId appWorkId = masterAppWork.getAppWorkId();
        ApplicationSubmission submission = application.getApplicationSubmission();
        log.info("Setting up AppWork: " + appWorkId + " for AppMaster " + application.getApplicationId());
        AppWorkLaunchContext launchContext = createAMLaunchContext(submission, appWorkId);

    }

    private AppWorkLaunchContext createAMLaunchContext(ApplicationSubmission submission, AppWorkId appWorkId) throws IOException {
        AppWorkLaunchContext appWork = submission.getAppMasterSpec();
        if (appWork == null) {
            throw new IOException("AppMaster launch space lose");
        }
        return appWork;
    }

    @Override
    public void run() {

    }
}
