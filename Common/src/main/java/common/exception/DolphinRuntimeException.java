package common.exception;

public class DolphinRuntimeException extends Exception {

    public DolphinRuntimeException() { }

    public DolphinRuntimeException(Throwable e) {
        super(e);
    }

    public DolphinRuntimeException(String message) {
        super(message);
    }

    public DolphinRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
