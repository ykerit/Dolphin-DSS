package common.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resources {

    private enum RoundingDirection {UP, DOWN}

    private static final Logger LOG =
            LogManager.getLogger(Resources.class);

    /**
     * Helper class to create a resource with a fixed value for all resource
     * types. For example, a NONE resource which returns 0 for any resource type.
     */
    static class FixedValueResource extends Resource {

        private final long resourceValue;
        private String name;

        /**
         * Constructor for a fixed value resource.
         *
         * @param rName the name of the resource
         * @param value the fixed value to be returned for all resource types
         */
        FixedValueResource(String rName, long value) {
            this.resourceValue = value;
            this.name = rName;
        }

        @Override
        @SuppressWarnings("deprecation")
        public int getMemory() {
            return castToInt(resourceValue);
        }

        @Override
        public long getMemorySize() {
            return this.resourceValue;
        }

        @Override
        @SuppressWarnings("deprecation")
        public void setMemory(int memory) {
            throw new RuntimeException(name + " cannot be modified!");
        }

        @Override
        public void setMemorySize(long memory) {
            throw new RuntimeException(name + " cannot be modified!");
        }

        @Override
        public int getVCore() {
            return castToInt(resourceValue);
        }

        @Override
        public void setVCore(int virtualCores) {
            throw new RuntimeException(name + " cannot be modified!");
        }
    }

    public static Resource createResource(int memory) {
        return createResource(memory, (memory > 0) ? 1 : 0);
    }

    public static Resource createResource(int memory, int cores) {
        return Resource.newInstance(memory, cores);
    }

    private static final Resource UNBOUNDED =
            new FixedValueResource("UNBOUNDED", Long.MAX_VALUE);

    private static final Resource NONE = new FixedValueResource("NONE", 0L);

    public static Resource createResource(long memory) {
        return createResource(memory, (memory > 0) ? 1 : 0);
    }

    public static Resource createResource(long memory, int cores) {
        return Resource.newInstance(memory, cores);
    }

    public static Resource none() {
        return NONE;
    }

    /**
     * Check whether a resource object is empty (0 memory and 0 virtual cores).
     *
     * @param other The resource to check
     * @return {@code true} if {@code other} has 0 memory and 0 virtual cores,
     * {@code false} otherwise
     */
    public static boolean isNone(Resource other) {
        return NONE.equals(other);
    }

    public static Resource unbounded() {
        return UNBOUNDED;
    }

    public static Resource clone(Resource res) {
        return Resource.newInstance(res);
    }

    public static Resource addTo(Resource lhs, Resource rhs) {
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = rhs.getResourceInformation(i);
            ResourceInformation lhsValue = lhs.getResourceInformation(i);
            lhs.setResourceValue(i, lhsValue.getValue() + rhsValue.getValue());
        }
        return lhs;
    }

    public static boolean greaterThanOrEqual(
            ResourceCalculator resourceCalculator,
            Resource clusterResource,
            Resource lhs, Resource rhs) {
        return resourceCalculator.compare(clusterResource, lhs, rhs) >= 0;
    }

    public static Resource add(Resource lhs, Resource rhs) {
        return addTo(clone(lhs), rhs);
    }

    public static Resource subtractFrom(Resource lhs, Resource rhs) {
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = rhs.getResourceInformation(i);
            ResourceInformation lhsValue = lhs.getResourceInformation(i);
            lhs.setResourceValue(i, lhsValue.getValue() - rhsValue.getValue());
        }
        return lhs;
    }

    public static boolean lessThan(
            ResourceCalculator resourceCalculator,
            Resource clusterResource,
            Resource lhs, Resource rhs) {
        return (resourceCalculator.compare(clusterResource, lhs, rhs) < 0);
    }

    public static Resource componentwiseMin(Resource lhs, Resource rhs) {
        Resource ret = createResource(0);
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = rhs.getResourceInformation(i);
            ResourceInformation lhsValue = lhs.getResourceInformation(i);
            ResourceInformation outInfo = lhsValue.getValue() < rhsValue.getValue()
                    ? lhsValue
                    : rhsValue;
            ret.setResourceInformation(i, outInfo);
        }
        return ret;
    }

    public static Resource componentwiseMax(Resource lhs, Resource rhs) {
        Resource ret = createResource(0);
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = rhs.getResourceInformation(i);
            ResourceInformation lhsValue = lhs.getResourceInformation(i);
            ResourceInformation outInfo = lhsValue.getValue() > rhsValue.getValue()
                    ? lhsValue
                    : rhsValue;
            ret.setResourceInformation(i, outInfo);
        }
        return ret;
    }

    public static Resource subtract(Resource lhs, Resource rhs) {
        return subtractFrom(clone(lhs), rhs);
    }

    /**
     * Subtract {@code rhs} from {@code lhs} and reset any negative values to
     * zero. This call will modify {@code lhs}.
     *
     * @param lhs {@link Resource} to subtract from
     * @param rhs {@link Resource} to subtract
     * @return the value of lhs after subtraction
     */
    public static Resource subtractFromNonNegative(Resource lhs, Resource rhs) {
        subtractFrom(lhs, rhs);
        if (lhs.getMemorySize() < 0) {
            lhs.setMemorySize(0);
        }
        if (lhs.getVCore() < 0) {
            lhs.setVCore(0);
        }
        return lhs;
    }

    /**
     * Subtract {@code rhs} from {@code lhs} and reset any negative values to
     * zero. This call will operate on a copy of {@code lhs}, leaving {@code lhs}
     * unmodified.
     *
     * @param lhs {@link Resource} to subtract from
     * @param rhs {@link Resource} to subtract
     * @return the value of lhs after subtraction
     */
    public static Resource subtractNonNegative(Resource lhs, Resource rhs) {
        return subtractFromNonNegative(clone(lhs), rhs);
    }

    public static Resource negate(Resource resource) {
        return subtract(NONE, resource);
    }

    public static Resource multiplyTo(Resource lhs, double by) {
        return multiplyAndRound(lhs, by, RoundingDirection.DOWN);
    }

    public static Resource multiply(Resource lhs, double by) {
        return multiplyTo(clone(lhs), by);
    }

    public static boolean equals(Resource lhs, Resource rhs) {
        return lhs.equals(rhs);
    }

    public static boolean fitsIn(Resource smaller, Resource bigger) {
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = bigger.getResourceInformation(i);
            ResourceInformation lhsValue = smaller.getResourceInformation(i);
            if (lhsValue.getValue() > rhsValue.getValue()) {
                return false;
            }
        }
        return true;
    }

    public static boolean greaterThan(
            ResourceCalculator resourceCollector,
            Resource clusterResource,
            Resource lhs, Resource rhs) {
        return resourceCollector.compare(clusterResource, lhs, rhs) > 0;
    }

    /**
     * Multiply {@code rhs} by {@code by}, and add the result to {@code lhs}
     * without creating any new {@link Resource} object
     */
    public static Resource multiplyAndAddTo(
            Resource lhs, Resource rhs, double by) {
        for (int i = 0; i < 2; i++) {
            ResourceInformation rhsValue = rhs.getResourceInformation(i);
            ResourceInformation lhsValue = lhs.getResourceInformation(i);

            long convertedRhs = (long) (rhsValue.getValue() * by);
            lhs.setResourceValue(i, lhsValue.getValue() + convertedRhs);
        }
        return lhs;
    }


    /**
     * Multiply {@code lhs} by {@code by}, and set the result rounded down into a
     * cloned version of {@code lhs} Resource object.
     *
     * @param lhs Resource object
     * @param by  Multiply values by this value
     * @return A cloned version of {@code lhs} with updated values
     */
    public static Resource multiplyAndRoundDown(Resource lhs, double by) {
        return multiplyAndRound(clone(lhs), by, RoundingDirection.DOWN);
    }

    /**
     * Multiply {@code lhs} by {@code by}, and set the result rounded up into a
     * cloned version of {@code lhs} Resource object.
     *
     * @param lhs Resource object
     * @param by  Multiply values by this value
     * @return A cloned version of {@code lhs} with updated values
     */
    public static Resource multiplyAndRoundUp(Resource lhs, double by) {
        return multiplyAndRound(clone(lhs), by, RoundingDirection.UP);
    }

    /**
     * Multiply {@code lhs} by {@code by}, and set the result according to
     * the rounding direction to {@code lhs}
     * without creating any new {@link Resource} object.
     *
     * @param lhs Resource object
     * @param by  Multiply values by this value
     * @return Returns {@code lhs} itself (without cloning) with updated values
     */
    private static Resource multiplyAndRound(Resource lhs, double by,
                                             RoundingDirection roundingDirection) {
        for (int i = 0; i < 2; i++) {
            ResourceInformation lhsValue = lhs.getResourceInformation(i);

            final long value;
            if (roundingDirection == RoundingDirection.DOWN) {
                value = (long) (lhsValue.getValue() * by);
            } else {
                value = (long) Math.ceil(lhsValue.getValue() * by);
            }
            lhs.setResourceValue(i, value);
        }
        return lhs;
    }

    public static Resource normalize(ResourceCalculator calculator, Resource lhs, Resource min,
                                     Resource max, Resource increment) {
        return calculator.normalize(lhs, min, max, increment);
    }
}
