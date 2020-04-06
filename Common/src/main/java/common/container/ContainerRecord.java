package common.container;

enum Status {
    INIT,
    RUNNING,
    STOP,
    IDLE
}

// record container info
public class ContainerRecord {
    int pid;
    String id;
    String name;
    Status status;
    Container container;
    String createTime;
}
