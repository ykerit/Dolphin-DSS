package agent.appworkmanage.Privileged;

import common.exception.DolphinRuntimeException;

public class PrivilegedOperationException extends DolphinRuntimeException {
    private static final long serialVersionUID = 1L;
    private int exitCode = -1;
    private String output;
    private String errorOutput;

    public PrivilegedOperationException() {
        super();
    }

    public PrivilegedOperationException(String message) {
        super(message);
    }

    public PrivilegedOperationException(String message, int exitCode,
                                        String output, String errorOutput) {
        super(message);
        this.exitCode = exitCode;
        this.output = output;
        this.errorOutput = errorOutput;
    }

    public PrivilegedOperationException(Throwable cause) {
        super(cause);
    }

    public PrivilegedOperationException(Throwable cause, int exitCode,
                                        String output, String errorOutput) {
        super(cause);
        this.exitCode = exitCode;
        this.output = output;
        this.errorOutput = errorOutput;
    }
    public PrivilegedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorOutput() { return errorOutput; }
}