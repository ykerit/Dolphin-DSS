package common.util;

import common.exception.ExitCodeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Shell {
    private static final Logger log = LogManager.getLogger(Shell.class.getName());

    public static final String USER_NAME_COMMAND = "whoami";

    // in the future, object maybe save something about Shell
    private static final Map<Shell, Object> shells = Collections.synchronizedMap(new WeakHashMap<Shell, Object>());

    public static String[] getSetPermissionCommand(String perm, boolean recursive) {
        if (recursive) {
            return new String[]{"chmod", "-R", perm};
        } else {
            return new String[]{"chmod", perm};
        }
    }

    public static String[] getSetOwnerCommand(String owner) {
        return new String[]{"chown", owner};
    }

    public static String[] getSymLinkCommand(String target, String link) {
        return new String[]{"ln", "-s", target, link};
    }

    public static String[] getReadLinkCommand(String link) {
        return new String[]{"readlink", link};
    }

    public static String[] getSignalKillCommand(int code, int pid) {
        return new String[]{"bash", "-c", "kill -" + code + " " + pid};
    }

    // refresh interval
    private long interval;
    private long lastTime;
    // Environment variables required for running
    private Map<String, String> env;
    // application running workspace
    private File dir;
    private Process process;
    // application exit code
    private int exitCode;
    private Thread waitingThread;
    private final boolean redirectErrorStream;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    protected boolean inheritParentEnv = true;
    protected long timeoutInterval = 0L;
    private final AtomicBoolean timeout = new AtomicBoolean(false);

    protected Shell() {
        this(0L, false);
    }

    protected Shell(long interval, boolean redirectErrorStream) {
        this.interval = interval;
        this.lastTime = (interval < 0) ? 0 : -interval;
        this.redirectErrorStream = redirectErrorStream;
    }

    protected void setEnv(Map<String, String> env) {
        this.env = env;
    }

    protected void setWorkSpace(File dir) {
        this.dir = dir;
    }

    protected void run() throws IOException {
        if (lastTime + interval > System.currentTimeMillis()) {
            return;
        }
        exitCode = 0;
        runCommand();
    }

    private void runCommand() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getExecStr());
        Timer timeoutTimer = null;
        ShellTimeoutTimerTask timeoutTimerTask = null;
        timeout.set(false);
        shutdown.set(false);
        if (!inheritParentEnv) {
            builder.environment().clear();
        }
        if (env != null) {
            builder.environment().putAll(env);
        }
        if (dir != null) {
            builder.directory(dir);
        }

        builder.redirectErrorStream(redirectErrorStream);
        process = builder.start();

        waitingThread = Thread.currentThread();
        shells.put(this, null);

        // if timeout interval is set, Timer checks if the command execution timeout.
        if (timeoutInterval > 0) {
            timeoutTimer = new Timer("Shell command timeout");
            timeoutTimerTask = new ShellTimeoutTimerTask(this);
            timeoutTimer.schedule(timeoutTimerTask, timeoutInterval);
        }

        final BufferedReader errReader =
                new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()));
        BufferedReader inReader =
                new BufferedReader(
                        new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
        final StringBuffer errMsg = new StringBuffer();

        Thread errThread = new Thread() {
            @Override
            public void run() {
                try {
                    String line = errReader.readLine();
                    while (line != null && !isInterrupted()) {
                        errMsg.append(line).append(System.getProperty("line.separator"));
                        line = errReader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (!isTimeOut()) {
                        log.warn("Error read errStream", e);
                    } else {
                        log.warn("Error read errStream and timeout", e);
                    }
                }
            }
        };

        errThread.start();

        parseExecResult(inReader);

        // clear input stream and buffer
        String line = inReader.readLine();
        while (line != null) {
            line = inReader.readLine();
        }

        try {
            exitCode = process.waitFor();
            joinThread(errThread);
            shutdown.set(true);
            if (exitCode != 0) {
                throw new ExitCodeException(exitCode, errMsg.toString());
            }
        } catch (InterruptedException | ExitCodeException e) {
            e.printStackTrace();
        } finally {
            if (timeoutTimer != null) {
                timeoutTimer.cancel();
            }
            inReader.close();
            if (!shutdown.get()) {
                errThread.interrupt();
                joinThread(errThread);
            }
            errReader.close();
            process.destroy();
            waitingThread = null;
            shells.remove(this);
            lastTime = System.currentTimeMillis();
        }

    }

    // In the future, maybe dolphin can running on other platform
    public interface CommandExecutor {
        void execute() throws IOException;

        int getExitCode();

        String getOutput();

        void close();
    }

    // Shell command executor is used on Linux
    public static class ShellCommandExecutor extends Shell implements CommandExecutor {

        private String[] command;
        private StringBuffer output;

        public ShellCommandExecutor(String[] execStr) {
            this(execStr, null);
        }

        public ShellCommandExecutor(String[] execStr, File dir) {
            this(execStr, dir, null);
        }

        public ShellCommandExecutor(String[] execStr, File dir, Map<String, String> env) {
            this(execStr, dir, env, 0L);
        }

        public ShellCommandExecutor(String[] execStr, File dir, Map<String, String> env, long timeout) {
            this(execStr, dir, env, timeout, true);
        }

        public ShellCommandExecutor(String[] execStr, File dir, Map<String, String> env, long timeout, boolean inheritParentEnv) {
            command = execStr.clone();
            if (dir != null) {
                setWorkSpace(dir);
            }
            if (env != null) {
                setEnv(env);
            }
            this.timeoutInterval = timeout;
            this.inheritParentEnv = inheritParentEnv;
        }

        @Override
        public void execute() throws IOException {
            for (String str : command) {
                if (str == null) {
                    throw new IOException("no command need to execute");
                }
            }
            this.run();
        }

        @Override
        public String getOutput() {
            return output == null ? "" : output.toString();
        }

        @Override
        public void close() {
        }

        @Override
        protected String[] getExecStr() {
            return command;
        }

        @Override
        protected void parseExecResult(BufferedReader lines) throws IOException {
            output = new StringBuffer();
            char[] buf = new char[512];
            int nRead = 0;
            while ((nRead = lines.read(buf, 0, buf.length)) > 0) {
                output.append(buf);
            }
        }
    }

    private static class ShellTimeoutTimerTask extends TimerTask {
        private Shell shell;

        public ShellTimeoutTimerTask(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void run() {
            Process process = shell.getProcess();
            try {
                process.exitValue();
            } catch (Exception e) {
                if (process != null && !shell.shutdown.get()) {
                    shell.setTimeout();
                    process.destroy();
                }
            }
        }
    }

    protected abstract String[] getExecStr();

    protected abstract void parseExecResult(BufferedReader lines) throws IOException;

    public Process getProcess() {
        return process;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Thread getWaitingThread() {
        return waitingThread;
    }

    public boolean isTimeOut() {
        return timeout.get();
    }

    protected void setTimeout() {
        timeout.set(true);
    }

    private static void joinThread(Thread t) {
        while (t.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                t.interrupt();
            }
        }
    }

    public static String execCommand(String ... cmd) throws IOException {
        return execCommand(null, cmd, 0L);
    }

    public static String execCommand(Map<String, String> env, String ... cmd) throws IOException {
        return execCommand(env, cmd, 0L);
    }

    public static String execCommand(Map<String, String> env, String[] cmd, long timeout) throws IOException {
        ShellCommandExecutor executor = new ShellCommandExecutor(cmd, null, env, timeout);
        executor.execute();
        return executor.getOutput();
    }

    public static void destroyAllShellProcess() {
        synchronized (shells) {
            for (Shell shell : shells.keySet()) {
                if (shell.getProcess() != null) {
                    shell.getProcess().destroy();
                }
            }
            shells.clear();
        }
    }

    public static Set<Shell> getAllShells() {
        synchronized (shells) {
            return new HashSet<>(shells.keySet());
        }
    }
}
