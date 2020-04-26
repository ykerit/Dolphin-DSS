package client;

import com.ceph.rados.exceptions.RadosException;
import common.context.AppMasterSpec;
import common.context.ApplicationSubmission;
import common.service.ChaosService;
import common.util.CephService;
import common.util.FileOperation;
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

public class Client extends ChaosService {
    private static final Logger log = LogManager.getLogger(Client.class.getName());
    private final Configuration configuration;
    private CephService cephService;

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

        this.cephService = new CephService(configuration);
        addService(this.cephService);
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

    protected ApplicationIDResponse getApplicationID()
            throws IOException, RemoteReadException, ClassNotFoundException {
        System.out.println(configuration.getDolphinMasterClientHost().getIP() + ":" + configuration.getDolphinMasterClientHost().getPort());
        return (ApplicationIDResponse) StandaloneClient.CS().read(configuration.getDolphinMasterClientHost().getIP(),
                configuration.getDolphinMasterClientHost().getPort(), new ApplicationIDRequest());
    }

    public SubmitApplicationResponse submitApplication(String applicationPath, String user, String group, int priority, AppMasterSpec spec)
            throws IOException, RemoteReadException, ClassNotFoundException {
        ApplicationIDResponse response = getApplicationID();
        String applicationName = null;
        try {
            FileOperation operation = new FileOperation(this.cephService.getIoContext("rbd"));
            applicationName = operation.write(applicationPath, response.getApplicationId());
            log.debug("application: ${}", applicationName);
        } catch (RadosException e) {
            e.printStackTrace();
        }
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                new ApplicationSubmission(response.getApplicationId(), applicationName, priority, group, user, spec));
        return (SubmitApplicationResponse) StandaloneClient.CS().read(configuration.getDolphinMasterClientHost().getIP(),
                configuration.getDolphinMasterClientHost().getPort(), request);
    }

}
