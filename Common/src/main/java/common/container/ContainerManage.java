package common.container;

import common.util.Tools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManage {
    private ConcurrentHashMap<String, ContainerRecord> containers;


    public ContainerManage() {
        this.containers = new ConcurrentHashMap<>();
    }

    // create container && initialize container.
    public String createContainer(String containerName, ResourceLimit resourceLimit) {
        String ID = Tools.GenerateContainerID();
        Container container = new Container(containerName, resourceLimit);
        ContainerRecord containerRecord = new ContainerRecord();
        containerRecord.container = container;
        containerRecord.id = ID;
        containerRecord.name = containerName;
        containerRecord.status = Status.IDLE;
        containerRecord.createTime =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.containers.put(ID, containerRecord);
        try {
            container.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ID;
    }

    public void execute(String containerId, int pid) throws IOException {
        ContainerRecord record = null;
        if ((record = containers.get(containerId)) != null) {
            record.container.apply(pid);
            record.status = Status.RUNNING;
        }
    }

    // Destroy container according to ID.
    public boolean destroyContainer(String ID) {
        ContainerRecord containerRecord = containers.get(ID);
        if (containerRecord.status == Status.RUNNING || containerRecord.status == Status.STOP) {
            return false;
        }
        containerRecord.container.remove();
        containers.remove(ID);
        return true;
    }
}
