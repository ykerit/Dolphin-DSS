package agent.appworkmanage.runtime;

import common.exception.DolphinRuntimeException;

public class AppWorkExecutionException extends DolphinRuntimeException {

    private static final int EXIT_CODE_UNSET = -1;
    private static final String OUTPUT_UNSET = "<unknown>";

    private int exitCode;
    private String output;
    private String errOutput;

    public AppWorkExecutionException(Throwable throwable) {
        super(throwable);
        exitCode = EXIT_CODE_UNSET;
        output = OUTPUT_UNSET;
        errOutput = OUTPUT_UNSET;
    }

    public AppWorkExecutionException(String message) {
        super(message);
        exitCode = EXIT_CODE_UNSET;
        output = OUTPUT_UNSET;
        errOutput = OUTPUT_UNSET;
    }

    public AppWorkExecutionException(String message, int code) {
        super(message);
        exitCode = code;
        output = OUTPUT_UNSET;
        errOutput = OUTPUT_UNSET;
    }

    public AppWorkExecutionException(String message, int code, String output, String errOutput) {
        super(message);
        exitCode = code;
        this.output = output;
        this.errOutput = errOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public String getErrOutput() {
        return errOutput;
    }
}
