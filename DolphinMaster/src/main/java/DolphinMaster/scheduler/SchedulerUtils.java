package DolphinMaster.scheduler;

import DolphinMaster.DolphinContext;
import common.struct.AppWorkStatus;
import api.app_master_message.ResourceRequest;
import common.exception.InvalidResourceRequestException;
import common.resource.Resource;
import common.resource.ResourceCalculator;
import common.resource.ResourceInformation;
import common.resource.Resources;
import common.struct.AppWorkId;
import common.struct.RemoteAppWorkState;
import common.util.SchedulerUnitExitStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static common.exception.InvalidResourceRequestException.*;

public class SchedulerUtils {
    private static final Logger log = LogManager.getLogger(SchedulerUtils.class);

    public static final String RELEASED_SCHEDULER_UNIT =
            "SchedulerUnit released by application";

    public static final String LOST_SCHEDULER_UNIT =
            "Container released on a *lost* node";

    public static void normalizeAndValidateRequest(ResourceRequest req, Resource maxAllocation,
                                                   String poolName, DolphinContext context, PoolInfo poolInfo) throws InvalidResourceRequestException {
        if (poolInfo == null) {
            try {
                poolInfo = context.getScheduler().getPoolInfo(poolName, false, false);
            } catch (IOException e) {

            }
        }
        validateResourceRequest(req, maxAllocation, poolInfo, context);
    }

    private static void validateResourceRequest(ResourceRequest req, Resource maxAllocation, PoolInfo poolInfo, DolphinContext context) throws InvalidResourceRequestException {
        final Resource requestedResource = req.getCapability();
        checkResourceRequestAgainstAvailableResource(requestedResource, maxAllocation);
    }

    private static void checkResourceRequestAgainstAvailableResource(Resource req, Resource availableResource) throws InvalidResourceRequestException {
        for (int i = 0; i < 2; ++i) {
            final ResourceInformation reqRI = req.getResourceInformation(i);
            final String reqName = reqRI.getName();

            if (reqRI.getValue() < 0) {
                throwInvalidResourceException(req, availableResource, reqName, InvalidResourceType.LESS_THAN_ZERO);
            }
            boolean valid = checkResource(reqRI, availableResource);
            if (!valid) {
                throwInvalidResourceException(req, availableResource, reqName, InvalidResourceType.GREATER_THAN_MAX);
            }
        }
    }

    private static boolean checkResource(
            ResourceInformation requestedRI, Resource availableResource) {
        final ResourceInformation availableRI =
                availableResource.getResourceInformation(requestedRI.getName());

        long requestedResourceValue = requestedRI.getValue();
        long availableResourceValue = availableRI.getValue();

        if (log.isDebugEnabled()) {
            log.debug("Requested resource information: " + requestedRI);
            log.debug("Available resource information: " + availableRI);
        }
        if (log.isDebugEnabled()) {
            log.debug("Requested resource value after conversion: "
                    + requestedResourceValue);
            log.info("Available resource value after conversion: "
                    + availableResourceValue);
        }
        return requestedResourceValue <= availableResourceValue;
    }

    private static void throwInvalidResourceException(Resource reqResource,
                                                      Resource maxAllowedAllocation, String reqResourceName,
                                                      InvalidResourceRequestException.InvalidResourceType invalidResourceType)
            throws InvalidResourceRequestException {
        final String message;

        if (invalidResourceType == InvalidResourceRequestException.InvalidResourceType.LESS_THAN_ZERO) {
            message = String.format(LESS_THAN_ERROR_RESOURCE_MESSAGE_TMP,
                    reqResourceName, reqResource);
        } else if (invalidResourceType ==
                InvalidResourceRequestException.InvalidResourceType.GREATER_THAN_MAX) {
            message = String.format(GREATER_THAN_MAX_RESOURCE_MESSAGE_TMP,
                    reqResourceName, reqResource, maxAllowedAllocation);
        } else if (invalidResourceType == InvalidResourceRequestException.InvalidResourceType.UNKNOWN) {
            message = String.format(UNKNOWN_RESON_MESSAGE_TMP, reqResourceName,
                    reqResource);
        } else {
            throw new IllegalArgumentException(String.format(
                    "InvalidResourceType argument should be either " + "%s, %s or %s",
                    InvalidResourceType.LESS_THAN_ZERO,
                    InvalidResourceType.GREATER_THAN_MAX,
                    InvalidResourceType.UNKNOWN));
        }
        throw new InvalidResourceRequestException(message, invalidResourceType);
    }

    public static Resource getNormalizeResource(Resource ask, ResourceCalculator resourceCalculator,
                                                Resource minResource, Resource maxResource,
                                                Resource incrementResource) {
        Resource normalized = Resources.normalize(resourceCalculator, ask, minResource, maxResource, incrementResource);
        return normalized;
    }

    public static AppWorkStatus createAbnormalSchedulerUnitStatus(
            AppWorkId appWorkId, String diagnostics) {
        return createAbnormalSchedulerUnitStatus(appWorkId,
                SchedulerUnitExitStatus.ABORTED, diagnostics);
    }

    public static AppWorkStatus createKilledSchedulerUnitStatus(
            AppWorkId appWorkId, String diagnostics) {
        return createAbnormalSchedulerUnitStatus(appWorkId,
                SchedulerUnitExitStatus.KILLED_BY_RESOURCEMANAGER, diagnostics);
    }

    private static AppWorkStatus createAbnormalSchedulerUnitStatus(
            AppWorkId appWorkId, int exitStatus, String diagnostics) {
        AppWorkStatus appWorkStatus = new AppWorkStatus(appWorkId, RemoteAppWorkState.COMPLETE, exitStatus, diagnostics);
        return appWorkStatus;
    }
}
