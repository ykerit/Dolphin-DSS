package common.webserver;

import common.annotation.GetRequest;
import common.annotation.PostRequest;
import common.struct.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Adapter {
    private Map<String, Pair<Object, Method>> getMethods;
    private Map<String, Pair<Object, Method>> postMethods;

    public Adapter() {
        getMethods = new HashMap<>();
        postMethods = new HashMap<>();
    }

    public Pair<Object, Method> GetMethod(String url) {
        return getMethods.get(url);
    }

    public Pair<Object, Method> PostMethod(String url) {
        return postMethods.get(url);
    }

    public void addMethod(Method method, Object object) {
        GetRequest get = method.getDeclaredAnnotation(GetRequest.class);
        PostRequest post = method.getDeclaredAnnotation(PostRequest.class);

        if (get != null && get.type() == MethodType.GET) {
            getMethods.put(get.url(), new Pair<>(object, method));
        } else if (post != null && post.type() == MethodType.POST) {
            postMethods.put(get.url(), new Pair<>(object, method));
        }
    }
}
