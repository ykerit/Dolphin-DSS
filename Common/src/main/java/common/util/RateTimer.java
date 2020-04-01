package common.util;

import java.util.Timer;
import java.util.TimerTask;

public class RateTimer {
    private Timer timer;
    private long delay;
    private long interval;

    public RateTimer(long delay, long interval) {
        this.timer = new Timer();
    }

    public void run(TimerTask task) {
        this.timer.scheduleAtFixedRate(task, this.delay, this.interval);
    }
}
