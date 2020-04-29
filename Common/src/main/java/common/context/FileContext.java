package common.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

// File context wrap up file operation
public class FileContext {
    static final Map<FileContext, Set<Path>> defer = new IdentityHashMap<>();

    private static final Logger log = LogManager.getLogger(FileContext.class.getName());

    private Path workspace;

    private FileContext() {
        workspace = getHomeDirectory();
    }

    public static FileContext getFileContext() {
        return new FileContext();
    }

    Path fixRelativePath(Path path) {
        if (path == null) {
            log.error("path can't null");
            return null;
        }
        if (path.isAbsolute()) {
            return path;
        }
        return path.resolve(workspace);
    }

    static void processDeleteOnExit() {
        synchronized (defer) {
            Set<Map.Entry<FileContext, Set<Path>>> set = defer.entrySet();
            for (Map.Entry<FileContext, Set<Path>> entry : set) {
                FileContext fc = entry.getKey();
                Set<Path> paths = entry.getValue();
                for (Path path : paths) {
                    try {
                        fc.delete(path);
                    } catch (IOException e) {
                        log.warn("delete on process exit failed, path is: ", path);
                    }
                }
            }
            set.clear();
        }
    }

    public boolean delete(final Path p) throws IOException {
        Path absoluteP = fixRelativePath(p);
        return Files.deleteIfExists(absoluteP);
    }

    public void setWorkspace(final Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new NotDirectoryException("cannot set workspace to a file");
        }
        workspace = fixRelativePath(dir);
    }

    protected static Path getHomeDirectory() {
        String username = System.getProperty("user.name");
        Path path = null;
        try {
            path = Paths.get(new URI("/user/" + username));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (!Files.exists(path)) {
            Path retPath = null;
            try {
                retPath = Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Directory create failed: {}", e.getMessage());
            }
            return retPath;
        }
        return path;
    }
}
