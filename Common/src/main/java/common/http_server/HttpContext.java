package common.http_server;

import common.annotation.GetRequest;
import common.annotation.PostRequest;
import common.annotation.RequestURL;
import common.util.AnnotationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class HttpContext {
    // key is url
    private final Map<String, HttpAdapter> router;
    private final Set<Class<?>> classAnnotations;
    public HttpContext() {
        router = new HashMap<>();
        classAnnotations = AnnotationUtil.UTIL().getClassAnnotation(RequestURL.class);
    }

    public void init() {
        for (Class<?> cls : classAnnotations) {
            Object o = null;
            try {
                o = cls.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            RequestURL requestURL = cls.getDeclaredAnnotation(RequestURL.class);
            String top_url = requestURL.url();
            HttpAdapter adapter = new HttpAdapter();

            Method[] methods = cls.getDeclaredMethods();
            for (Method m : methods) {
                adapter.addMethod(m, o);
            }
            router.put(top_url, adapter);
        }
    }

    public Map<String, HttpAdapter> getRouter() {
        return router;
    }
}
