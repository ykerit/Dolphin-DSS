package common.util;

import org.greatfree.client.StandaloneClient;
import org.greatfree.exceptions.RemoteReadException;
import org.greatfree.message.ServerMessage;
import org.greatfree.message.container.Request;
import org.greatfree.util.IPAddress;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class HeartBeatProvider {
    private Timer timer;
    private final long period;
    private volatile boolean cancel = false;

    public HeartBeatProvider(long period) {
        this.period = period;
        timer = new Timer();
    }

    public void startHeartBeat(final CallBack callBack) {
        timer.scheduleAtFixedRate(new HeartBeatTask(callBack), 0, period);
    }

    public void cancel() {
        cancel = true;
    }

    class HeartBeatTask extends TimerTask {
        private CallBack callBack;
        public HeartBeatTask(final CallBack callBack) {
            this.callBack = callBack;
        }
        @Override
        public void run() {
            if (cancel) {
                cancel();
            }
            callBack.handleHeartBeat();
        }
    }
}
