package common.resource;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class DefaultResourceCalculator extends ResourceCalculator {
    private static final Logger LOG =
            LogManager.getLogger(DefaultResourceCalculator.class);

    private static final Set<String> INSUFFICIENT_RESOURCE_NAME =
            ImmutableSet.of(ResourceInformation.MEMORY_KEY);

    @Override
    public int compare(Resource unused, Resource lhs, Resource rhs,
                       boolean singleType) {
        // Only consider memory
        return Long.compare(lhs.getMemorySize(), rhs.getMemorySize());
    }

    @Override
    public long computeAvailableContainers(Resource available, Resource required) {
        // Only consider memory
        return available.getMemorySize() / required.getMemorySize();
    }

    @Override
    public float divide(Resource unused,
                        Resource numerator, Resource denominator) {
        return ratio(numerator, denominator);
    }

    public boolean isInvalidDivisor(Resource r) {
        if (r.getMemorySize() == 0.0f) {
            return true;
        }
        return false;
    }

    @Override
    public float ratio(Resource a, Resource b) {
        return divideSafelyAsFloat(a.getMemorySize(), b.getMemorySize());
    }

    @Override
    public Resource divideAndCeil(Resource numerator, int denominator) {
        return Resources.createResource(
                divideAndCeil(numerator.getMemorySize(), denominator));
    }

    @Override
    public Resource divideAndCeil(Resource numerator, float denominator) {
        return Resources.createResource(
                divideAndCeil(numerator.getMemorySize(), denominator));
    }

    @Override
    public Resource normalize(Resource r, Resource minimumResource,
                              Resource maximumResource, Resource stepFactor) {
        if (stepFactor.getMemorySize() == 0) {
            LOG.error("Memory cannot be allocated in increments of zero. Assuming " +
                    minimumResource.getMemorySize() + "MB increment size. "
                    + "Please ensure the scheduler configuration is correct.");
            stepFactor = minimumResource;
        }

        long normalizedMemory = Math.min(
                roundUp(
                        Math.max(r.getMemorySize(), minimumResource.getMemorySize()),
                        stepFactor.getMemorySize()),
                maximumResource.getMemorySize());
        return Resources.createResource(normalizedMemory);
    }

    @Override
    public Resource roundUp(Resource r, Resource stepFactor) {
        return Resources.createResource(
                roundUp(r.getMemorySize(), stepFactor.getMemorySize())
        );
    }

    @Override
    public Resource roundDown(Resource r, Resource stepFactor) {
        return Resources.createResource(
                roundDown(r.getMemorySize(), stepFactor.getMemorySize()));
    }

    @Override
    public Resource multiplyAndNormalizeUp(Resource r, double by,
                                           Resource stepFactor) {
        return Resources.createResource(
                roundUp((long) (r.getMemorySize() * by + 0.5),
                        stepFactor.getMemorySize()));
    }

    @Override
    public Resource multiplyAndNormalizeUp(Resource r, double[] by,
                                           Resource stepFactor) {
        return Resources.createResource(
                roundUp((long) (r.getMemorySize() * by[0] + 0.5),
                        stepFactor.getMemorySize()));
    }

    @Override
    public Resource multiplyAndNormalizeDown(Resource r, double by,
                                             Resource stepFactor) {
        return Resources.createResource(
                roundDown(
                        (long)(r.getMemorySize() * by),
                        stepFactor.getMemorySize()
                )
        );
    }

    @Override
    public boolean fitsIn(Resource smaller, Resource bigger) {
        return smaller.getMemorySize() <= bigger.getMemorySize();
    }

    @Override
    public Resource normalizeDown(Resource r, Resource stepFactor) {
        return Resources.createResource(
                roundDown((r.getMemorySize()), stepFactor.getMemorySize()));
    }

    @Override
    public boolean isAnyMajorResourceZeroOrNegative(Resource resource) {
        return resource.getMemorySize() <= 0;
    }

    @Override
    public boolean isAnyMajorResourceAboveZero(Resource resource) {
        return resource.getMemorySize() > 0;
    }

    public Set<String> getInsufficientResourceNames(Resource required,
                                                    Resource available) {
        if (required.getMemorySize() > available.getMemorySize()) {
            return INSUFFICIENT_RESOURCE_NAME;
        } else {
            return ImmutableSet.of();
        }
    }
}
