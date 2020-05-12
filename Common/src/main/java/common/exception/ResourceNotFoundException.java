package common.exception;

import common.resource.Resource;

public class ResourceNotFoundException extends DolphinRuntimeException {
    private static final String MESSAGE = "The resource manager encountered a "
            + "problem that should not occur under normal circumstances. "
            + "Please report this error to the Hadoop community by opening a "
            + "JIRA ticket at http://issues.apache.org/jira and including the "
            + "following information:%n* Resource type requested: %s%n* Resource "
            + "object: %s%n* The stack trace for this exception: %s%n"
            + "After encountering this error, the resource manager is "
            + "in an inconsistent state. It is safe for the resource manager "
            + "to be restarted as the error encountered should be transitive. "
            + "If high availability is enabled, failing over to "
            + "a standby resource manager is also safe.";

    public ResourceNotFoundException(Resource resource, String type) {
        this(String.format(MESSAGE, type, resource));
    }

    public ResourceNotFoundException(Resource resource, String type,
                                     Throwable cause) {
        super(String.format(MESSAGE, type, resource, cause));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}