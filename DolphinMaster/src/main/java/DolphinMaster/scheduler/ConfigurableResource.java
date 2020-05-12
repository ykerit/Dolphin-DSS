package DolphinMaster.scheduler;

import common.resource.Resource;
import common.resource.ResourceInformation;

import java.util.Arrays;

public class ConfigurableResource {
    private Resource resource;
    private double[] percentages;

    ConfigurableResource(double[] percentages) {
        this.percentages = percentages.clone();
        this.resource = null;
    }

    ConfigurableResource() {
        this(getOneHundredPercentArray());
    }

    ConfigurableResource(long value) {

    }

    public ConfigurableResource(Resource resource) {
        this.percentages = null;
        this.resource = resource;
    }

    private static double[] getOneHundredPercentArray() {
        double[] resourcePercentages =
                new double[2];
        Arrays.fill(resourcePercentages, 1.0);

        return resourcePercentages;
    }

    public Resource getResource(Resource clusterResource) {
        if (percentages != null && clusterResource != null) {
            long memory = (long) (clusterResource.getMemorySize() * percentages[0]);
            int vcore = (int) (clusterResource.getVCore() * percentages[1]);
            Resource res = Resource.newInstance(memory, vcore);
            ResourceInformation[] clusterInfo = clusterResource.getResources();

            for (int i = 2; i < clusterInfo.length; i++) {
                res.setResourceValue(i,
                        (long) (clusterInfo[i].getValue() * percentages[i]));
            }

            return res;
        } else {
            return resource;
        }
    }

    public Resource getResource() {
        return resource;
    }

    void setValue(String name, long value) {
        if (resource != null) {
            resource.setResourceValue(name, value);
        }
    }

    void setPercentage(String name, double value) {
        if (percentages != null) {
            Integer index = 0;
            if (name == "memory") {
                index = 0;
            } else {
                index = 1;
            }

            if (index != null) {
                percentages[index] = value;
            }
        }
    }

    public double[] getPercentages() {
        return percentages == null ? null :
                Arrays.copyOf(percentages, percentages.length);
    }
}
