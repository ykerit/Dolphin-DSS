package common.http_server;

import io.netty.handler.codec.http.FullHttpRequest;

public class HttpDispatcher {
    private HttpContext context;
    public HttpDispatcher() {
        this.context = new HttpContext();
        this.context.init();
    }

    public void dispatcher(FullHttpRequest request) {
//        request.uri()
    }
}
