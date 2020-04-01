package common.util;

import common.service.AbstractService;
import org.greatfree.client.StandaloneClient;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Request;
import org.greatfree.util.IPAddress;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HeartBeatProvider {
    private Timer timer;
    private final long period;
    private volatile boolean cancel = false;
    private final long timeout;
    private Future<ServerMessage> response;

    public HeartBeatProvider(long period, long timeout) {
        this.period = period;
        this.timeout = timeout;
        timer = new Timer();
    }

    public void startHeartBeat(Request request, IPAddress address) {
        timer.scheduleAtFixedRate(new HeartBeatTask(request, address), 0, period);
    }

    public void cancel() {
        cancel = true;
    }

    class HeartBeatTask extends TimerTask {
        private Request request;
        private IPAddress ipAddress;
        public HeartBeatTask(Request request, IPAddress ipAddress) {
            this.request = request;
            this.ipAddress = ipAddress;
        }
        @Override
        public void run() {
            if (cancel) {
                cancel();
            }
            response = StandaloneClient.CS().futureRead(ipAddress.getIP(),
                    ipAddress.getPort(), request, (int)timeout);
        }
    }

    public ServerMessage getHeartBeatResponse() throws ExecutionException, InterruptedException {
        return response.get();
    }
}
