package DolphinMaster;

import DolphinMaster.scheduler.PoolInfo;
import DolphinMaster.scheduler.ResourceScheduler;
import DolphinMaster.scheduler.SchedulerUtils;
import api.app_master_message.ResourceRequest;
import common.exception.InvalidResourceRequestException;
import common.resource.Resource;

import java.io.IOException;
import java.util.List;

public class DolphinUtils {
    public static void normalizeAndValidateRequest(List<ResourceRequest> ask,
                                                   Resource maxAllocation,
                                                   String poolName,
                                                   ResourceScheduler scheduler,
                                                   DolphinContext context) throws InvalidResourceRequestException {
        PoolInfo poolInfo = null;
        try {
            poolInfo = scheduler.getPoolInfo(poolName, false, false);
        } catch (IOException e) {

        }
        for (ResourceRequest req : ask) {
            SchedulerUtils.normalizeAndValidateRequest(req, maxAllocation, poolName, context, poolInfo);
        }
    }
}
