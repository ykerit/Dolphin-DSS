package agent.appworkmanage;

import common.container.Container;
import common.container.ResourceLimit;
import common.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* In the future, will support task interrupt and resume
** the status represents future;
 */

public class AppWork {
    enum Status {
        INIT,
        RUNNING,
        STOP,
        IDLE;
    }

    private static final Logger log = LogManager.getLogger(AppWork.class.getName());

    private int pid;
    private String agentId;
    private String id;
    private int priority;
    private Status status;
    private Container container;
    private String createTime;

    public AppWork(ResourceLimit resourceLimit) {
        id = Tools.GenerateContainerID();
        createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        status = Status.INIT;
        container = new Container(id, resourceLimit);
        try {
            container.init();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("AppWork: ${}, container init failed: ${}", id, e.getMessage());
        }
    }

    public void work(int pid) {
        if (status == Status.IDLE || status == Status.INIT) {
            try {
                container.apply(pid);
            } catch (IOException e) {
                log.error("AppWork: ${}, container work failed: ${}", id, e.getMessage());
                e.printStackTrace();
            }
            status = Status.RUNNING;
            this.pid = pid;
        }
    }

    public void destroy() {
        if (status == Status.IDLE || status == Status.INIT) {
            container.remove();
        }
    }

    public String status() {
        return "AppWork"+ id + ": " + status;
    }
}
