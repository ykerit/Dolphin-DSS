package common.container;

import common.util.Tools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerManage {
    private ConcurrentHashMap<String, ContainerRecord> containers;

    private ContainerManage() {}

    private static ContainerManage instance = new ContainerManage();

    public ContainerManage Init() {
        this.containers = new ConcurrentHashMap<>();
        if (instance == null) {
            instance = new ContainerManage();
        }
        return instance;
    }

    // create container && initialize container.
    public String createContainer(String containerName, ResourceLimit resourceLimit) {
        String ID = Tools.GenerateContainerID();
        Container container = new Container(containerName, resourceLimit);
        ContainerRecord containerRecord = new ContainerRecord();
        containerRecord.container = container;
        containerRecord.id = ID;
        containerRecord.name = containerName;
        containerRecord.status = Status.RUNNING;
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
