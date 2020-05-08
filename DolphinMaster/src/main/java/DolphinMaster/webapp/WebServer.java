package DolphinMaster.webapp;

import common.service.AbstractService;
import freemarker.template.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class WebServer extends AbstractService {
    private Configuration configuration;
    public WebServer() {
        super(WebServer.class.getName());
    }

    @Override
    protected void serviceInit() throws Exception {
        configuration = new Configuration(Configuration.VERSION_2_3_30);
//        String dirs = this.getClass().getResource("/templates").toString();
//        System.out.println(dirs);
        configuration.setDirectoryForTemplateLoading(new File("/Users/yuankai/workspace/Dolphin-DSS/DolphinMaster/src/main/resources/templates"));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
        super.serviceInit();
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
    }

    public void render() {
        try {
            Template template = configuration.getTemplate("test.html");
            Writer out = new OutputStreamWriter(System.out);
            Map<String, String> root = new HashMap<>();
            root.put("val", "yker");
            template.process(root, out);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}
