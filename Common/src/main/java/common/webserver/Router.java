package common.webserver;

import common.annotation.RequestURL;
import common.util.AnnotationUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import freemarker.template.*;

public class Router extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger(Router.class.getName());
    private final Map<String, Adapter> routerTable = new HashMap<>();
    private Configuration configuration;
    private Router() {
        initializeAnnotation();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    public static Router newInstance() {
        return new Router();
    }

    private void initializeAnnotation() {
        Set<Class<?>> classAnnotations = AnnotationUtil.UTIL().getClassAnnotation(RequestURL.class);
        for (Class<?> cls : classAnnotations) {
            Object o = null;
            try {
                o = cls.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.warn("Failed to create dao instance");
            }
            RequestURL requestURL = cls.getDeclaredAnnotation(RequestURL.class);
            String top_url = requestURL.url();
            Adapter adapter = new Adapter();

            Method[] methods = cls.getDeclaredMethods();
            for (Method m : methods) {
                adapter.addMethod(m, o);
            }
            routerTable.put(top_url, adapter);
        }
    }

    private void initializeFreeMaker() throws IOException {
        configuration = new Configuration(Configuration.VERSION_2_3_30);
        configuration.setDirectoryForTemplateLoading(new File("/Users/yuankai/workspace/Dolphin-DSS/DolphinMaster/src/main/resources/templates"));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
    }

    private void render() throws IOException {
        configuration.getTemplate("home.ftl");
    }

    private void dispatch() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String massage = "";
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(massage, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        ctx.writeAndFlush(response);
    }

}
