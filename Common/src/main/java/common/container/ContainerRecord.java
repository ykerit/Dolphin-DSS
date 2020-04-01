package common.container;

enum Status {
    RUNNING,
    STOP
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
