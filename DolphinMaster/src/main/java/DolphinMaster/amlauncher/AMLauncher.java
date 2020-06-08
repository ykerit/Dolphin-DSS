package DolphinMaster.amlauncher;

import DolphinMaster.DolphinContext;
import DolphinMaster.app.App;
import DolphinMaster.app.AppEvent;
import DolphinMaster.app.AppEventType;
import api.app_master_message.StartAppWorkRequest;
import api.app_master_message.StartAppWorksRequest;
import api.app_master_message.StartAppWorksResponse;
import common.context.AppWorkLaunchContext;
import common.context.ApplicationSubmission;
import common.event.EventProcessor;
import common.struct.AgentId;
import common.struct.AppWorkId;
import common.struct.RemoteAppWork;
import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AMLauncher implements Runnable {
    private static final Logger log = LogManager.getLogger(AMLauncher.class);

    private final App application;
    private final Configuration configuration;
    private final AMLauncherEventType eventType;
    private final DolphinContext dolphinContext;
    private final RemoteAppWork masterAppWork;
    private EventProcessor processor;

    public AMLauncher(DolphinContext dolphinContext, App app,
                      AMLauncherEventType eventType, Configuration configuration) {
        this.application = app;
        this.configuration = configuration;
        this.eventType = eventType;
        this.dolphinContext = dolphinContext;
        this.masterAppWork = application.getMasterAppWork();
    }

    private void launch() throws IOException, RemoteReadException, ClassNotFoundException {
        AppWorkId appWorkId = masterAppWork.getAppWorkId();
        ApplicationSubmission submission = application.getApplicationSubmission();
        log.info("Setting up AppWork: " + appWorkId + " for AppMaster " + application.getApplicationId());
        AppWorkLaunchContext launchContext = createAMLaunchContext(submission, appWorkId);
        StartAppWorkRequest sReq = new StartAppWorkRequest(appWorkId, launchContext, "root");
        List<StartAppWorkRequest> list = new ArrayList<>();
        list.add(sReq);
        StartAppWorksRequest allRequest = new StartAppWorksRequest(list);
        AgentId agent = masterAppWork.getAgentId();
        StartAppWorksResponse response = (StartAppWorksResponse) StandaloneClient.CS()
                .read(agent.getLocalIP(), agent.getCommandPort(), allRequest);
        if (response != null) {
            log.info("Done launch AppMaster");
        }
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
        switch (eventType) {
            case LAUNCH:
                log.info("Launching AppMaster: " + application.getApplicationId());
                try {
                    launch();
                    processor.process(new AppEvent(application.getApplicationId(),
                            AppEventType.LAUNCHED, System.currentTimeMillis()));
                } catch (IOException | RemoteReadException | ClassNotFoundException e) {
                    onAMLaunchFailed(masterAppWork.getAppWorkId(), e);
                }
                break;
            case CLEANUP:
        }
    }

    protected void onAMLaunchFailed(AppWorkId appWorkId, Exception e) {
        String msg = "Error Launch AppMaster: " + appWorkId.getApplicationId()
                + " get exception: " + e.getMessage();
        log.info(msg);
        processor.process(new AppEvent(appWorkId.getApplicationId(), AppEventType.LAUNCHED_FAILED, msg));
    }
}
