package common;

import common.webserver.HttpServer;
import org.junit.Test;

public class HttpServerTest {
    @Test
    public void test() {
        HttpServer server = new HttpServer(9000);
        server.start();
    }
}
