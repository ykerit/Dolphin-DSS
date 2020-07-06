package dolphinmaster.webapp;

import DolphinMaster.DolphinContext;
import DolphinMaster.node.NodeImp;
import DolphinMaster.webapp.WebServer;
import common.struct.AgentId;
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

    @Test
    public void testDolphinContext() {
        DolphinContext context = new DolphinContext();
        AgentId agentId = new AgentId();
        agentId.setHostname("host");
        agentId.setCommandPort(9000);
        context.getNodes().putIfAbsent(agentId, new NodeImp(agentId, context, "host", 9000, null, null));
        System.out.println(context.getNodes().get(agentId));
    }
}
