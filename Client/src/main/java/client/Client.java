package client;

import common.context.ApplicationSubmissionContext;
import common.service.AbstractService;
import config.Configuration;
import message.client_master_message.ApplicationIDRequest;
import message.client_master_message.ApplicationIDResponse;
import message.client_master_message.SubmitApplicationRequest;
import message.client_master_message.SubmitApplicationResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;

import java.io.IOException;

public class Client extends AbstractService {
    private static final Logger log = LogManager.getLogger(Client.class.getName());
    private Configuration configuration;

    public Client() {
        super(Client.class.getName());
        this.configuration = new Configuration();
    }

    @Override
    protected void serviceInit() {
        // ----------Client Init---------
        try {
            StandaloneClient.CS().init();
        } catch (ClassNotFoundException | RemoteReadException | IOException e) {
            e.printStackTrace();
        }

        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        try {
            StandaloneClient.CS().dispose();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }

    public ApplicationIDResponse getApplicationID()
            throws IOException, RemoteReadException, ClassNotFoundException {
        System.out.println(configuration.getDolphinMasterClientHost().getIP() + ":" + configuration.getDolphinMasterClientHost().getPort());
        return (ApplicationIDResponse) StandaloneClient.CS().read(configuration.getDolphinMasterClientHost().getIP(),
                configuration.getDolphinMasterClientHost().getPort(), new ApplicationIDRequest());
    }

    public SubmitApplicationResponse submitApplication(long applicationID, String applicationName, String user, int priority, ApplicationSubmissionContext applicationSubmissionContext)
            throws IOException, RemoteReadException, ClassNotFoundException {
        SubmitApplicationRequest request = new SubmitApplicationRequest(applicationID, applicationName, user, priority, applicationSubmissionContext);
        return (SubmitApplicationResponse) StandaloneClient.CS().read(configuration.getDolphinMasterClientHost().getIP(),
                configuration.getDolphinMasterClientHost().getPort(), request);
    }
}
