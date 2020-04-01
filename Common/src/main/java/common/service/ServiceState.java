package common.service;

public enum ServiceState {
    READY(0, "READY"),
    INITED(1, "INITED"),
    STARTED(2, "STARTED"),
    STOPPED(3, "STOPPED");

    private final int value;
    private final String state;

    private ServiceState(int value, String state) {
        this.value = value;
        this.state = state;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return state;
    }
}
