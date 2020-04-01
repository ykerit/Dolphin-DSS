package common.http_server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpHandle extends SimpleChannelInboundHandler<FullHttpRequest> {
    private HttpDispatcher dispatcher;

    public HttpHandle(HttpDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        System.out.println(msg.uri());
        this.dispatcher.dispatcher(msg);
    }
}
