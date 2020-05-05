package common.exception;

public class DolphinException extends Exception{
    public DolphinException() {
        super();
    }

    public DolphinException(String message) {
        super(message);
    }

    public DolphinException(Throwable cause) {
        super(cause);
    }

    public DolphinException(String message, Throwable cause) {
        super(message, cause);
    }
}
