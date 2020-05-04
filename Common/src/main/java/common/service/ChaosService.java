package common.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite services
 */
public class ChaosService extends AbstractService  {

    private final static Logger log = LogManager.getLogger(ChaosService.class.getName());
    private final List<Service> serviceList = new ArrayList<>();

    public ChaosService(String name) {
        super(name);
    }

    public List<Service> getServiceList() {
        synchronized (serviceList) {
            return new ArrayList<>(this.serviceList);
        }
    }

    protected void addService(Service service) {
        log.info("Adding service: " + service.getName());
        synchronized (serviceList) {
            serviceList.add(service);
        }
    }

    protected boolean removeService(Service service) {
        log.info("Removing service: " + service.getName());
        synchronized (serviceList) {
            return serviceList.remove(service);
        }
    }

    protected void serviceInit() throws Exception {
        List<Service> services = getServiceList();
        for (Service service : services) {
            service.init();
            log.info(service.getName() + " inited");
        }
        super.serviceInit();
    }

    protected void serviceStart() throws Exception {
        List<Service> services = getServiceList();
        log.info("current service size: " + services.size());
        for (Service service : services) {
            service.start();
            log.info(service.getName() + " started");
        }
        super.serviceStart();
    }

    /**
     * Service stop sequences Warning
     */
    protected void serviceStop() throws Exception {
        List<Service> services = getServiceList();
        for (Service service : services) {
            service.stop();
            log.info(service.getName() + " stopped");
        }
        super.serviceStop();
    }
}
