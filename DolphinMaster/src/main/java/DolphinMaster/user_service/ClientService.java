package DolphinMaster.user_service;

import config.ServerConfig;
import DolphinMaster.server_task.ClientTask;
import common.service.AbstractService;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.server.container.ServerContainer;
import org.greatfree.util.TerminateSignal;

import java.io.IOException;

public class ClientService extends AbstractService {
    private ServerContainer server;

    public ClientService() {
        super(ClientService.class.getName());
    }

    @Override
    protected void serviceInit() {
        try {
            this.server = new ServerContainer(ServerConfig.CLIENT_SERVER_PORT, new ClientTask());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        try {
            this.server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        TerminateSignal.SIGNAL().setTerminated();
        try {
            this.server.stop(2000L);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteReadException e) {
            e.printStackTrace();
        }

        super.serviceStop();
    }
}
