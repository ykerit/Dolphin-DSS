package agent;

import common.service.AbstractService;
import config.ServerConfig;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.server.container.ServerContainer;

import java.io.IOException;

public class ServerService extends AbstractService {
    private ServerContainer serverContainer;

    public ServerService() {
        super(ServerService.class.getName());
    }

    @Override
    protected void serviceInit() {
        try {
            this.serverContainer = new ServerContainer(ServerConfig.AGENT_PORT, new AgentTask());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.serviceInit();
    }

    @Override
    protected void serviceStart() {
        try {
            this.serverContainer.start();
        } catch (IOException | ClassNotFoundException | RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() {
        try {
            this.serverContainer.stop(2000L);
        } catch (ClassNotFoundException | IOException | InterruptedException | RemoteReadException e) {
            e.printStackTrace();
        }
        super.serviceStop();
    }


}
