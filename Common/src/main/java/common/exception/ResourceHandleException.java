package common.exception;

public class ResourceHandleException extends DolphinException {
    public ResourceHandleException() {
        super();
    }

    public ResourceHandleException(String message) {
        super(message);
    }

    public ResourceHandleException(Throwable cause) {
        super(cause);
    }

    public ResourceHandleException(String message, Throwable cause) {
        super(message, cause);
    }
}
