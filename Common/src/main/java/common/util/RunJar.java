package common.util;

import config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class RunJar {
    private static final Logger log = LogManager.getLogger(RunJar.class.getName());
    private String filename;
    private String[] args;
    private Configuration configuration;

    public RunJar(String filename, String[] args, Configuration configuration) {
        this.filename = filename;
        this.args = args;
    }

    public void execute(int type) {
        File file = new File(this.filename);
        if (!file.exists() || !file.isFile()) {
            log.error("run file is not exists");
            return;
        }

    }

    private void runJar(File file) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            log.error("error opening job jar");
            e.printStackTrace();
            return;
        }
        Manifest manifest = null;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            log.error("not load main classes");
            e.printStackTrace();
            return;
        }
        String mainClassName = null;
        if (manifest != null) {
            mainClassName = manifest.getMainAttributes().getValue("Main-Class");
        } else {
            try {
                jarFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            Class<?> mainClass = Class.forName(mainClassName);
            Method main = mainClass.getMethod("main", new Class[] {
                    Array.newInstance(String.class, 0).getClass()
            });
            main.invoke(null, new Object());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
