package common.exception;

import java.io.IOException;

public class ExitCodeException extends IOException {
    private final int exitCode;

    public ExitCodeException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return "ExitCodeException{" +
                "exitCode=" + exitCode +
                '}';
    }
}
