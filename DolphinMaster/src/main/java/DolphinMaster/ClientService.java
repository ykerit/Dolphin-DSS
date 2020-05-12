package DolphinMaster;

import DolphinMaster.AppManager.ApplicationManager;
import DolphinMaster.app.AppState;
import DolphinMaster.app.ApplicationReport;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.servertask.ClientTask;
import api.client_master_message.ApplicationIDRequest;
import api.client_master_message.SubmitApplicationRequest;
import api.client_master_message.SubmitApplicationResponse;
import common.context.ApplicationSubmission;
import common.exception.DolphinException;
import common.service.AbstractService;
import common.struct.ApplicationId;
import common.util.SnowFlakeGenerator;
import common.util.SystemClock;
import config.DefaultServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.server.container.ServerContainer;
import org.greatfree.util.TerminateSignal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientService extends AbstractService {
    private static final List<ApplicationReport> EMPTY_APPS_REPORT = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(ClientService.class);
    private final AtomicInteger applicationCounter = new AtomicInteger(0);
    private final ResourceScheduler scheduler;
    private final DolphinContext context;
    private final ApplicationManager appManager;

    private ServerContainer server;
    private SystemClock clock;

    public ClientService(DolphinContext context, ResourceScheduler scheduler,
                         ApplicationManager appManager) {
        super(ClientService.class.getName());
        this.scheduler = scheduler;
        this.context = context;
        this.appManager = appManager;
        this.clock = SystemClock.getInstance();
    }

    @Override
    protected void serviceInit() throws Exception {
        try {
            this.server = new ServerContainer(DefaultServerConfig.CLIENT_SERVER_PORT, new ClientTask(this));
        } catch (IOException e) {
            throw new DolphinException("Client server initialize failed");
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        try {
            this.server.start();
        } catch (IOException | ClassNotFoundException | RemoteReadException e) {
            throw new DolphinException("Client server start failed");
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        TerminateSignal.SIGNAL().setTerminated();
        try {
            this.server.stop(2000L);
        } catch (ClassNotFoundException | IOException | InterruptedException | RemoteReadException e) {
            throw new DolphinException("Client server stop failed");
        }
        super.serviceStop();
    }

    public ApplicationId getNewApplicationId(ApplicationIDRequest request) {
        ApplicationId applicationId = new ApplicationId(clock.getTime(), SnowFlakeGenerator.GEN().nextId());
        applicationCounter.incrementAndGet();
        log.info("Allocate new applicationId: " + applicationId);
        return applicationId;
    }

    public SubmitApplicationResponse submitApplication(SubmitApplicationRequest request) {
        ApplicationSubmission submission = request.getSubmission();
        ApplicationId applicationId = request.getApplicationId();

        String user = null;

        if (context.getApps().get(applicationId) != null) {
            log.info("This application is submitted: " + applicationId);
            return new SubmitApplicationResponse();
        }
        if (submission.getPool() == null) {
            submission.setPool("root");
        }
        if (submission.getApplicationName() == null) {
            submission.setApplicationName("Default_name" + applicationId);
        }
        if (submission.getApplicationType() == null) {
            submission.setApplicationName("jvm");
        }
        try {
            appManager.submitApplication(submission, clock.getTime(), user);
        } catch (DolphinException e) {
            log.error("Submit application failed");
        }
        return new SubmitApplicationResponse();
    }

}
