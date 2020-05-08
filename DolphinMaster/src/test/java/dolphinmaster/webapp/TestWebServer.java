package dolphinmaster.webapp;

import DolphinMaster.webapp.WebServer;
import org.junit.Test;

import java.net.URL;

public class TestWebServer {
    @Test
    public void testFreeMarker() throws Exception {
        WebServer webServer = new WebServer();
        webServer.init();
        webServer.start();
        webServer.render();
    }

    @Test
    public void testResource() {
        URL url = this.getClass().getResource("/templates");
        URL url1 = Thread.currentThread().getContextClassLoader().getResource("templates");
        System.out.println(url);
        System.out.println(url1);
    }
}
