package common.exception;

public class InvalidResourceRequestException extends DolphinException {
    public static final String LESS_THAN_ERROR_RESOURCE_MESSAGE_TMP =
            "Invalid resource request! Can't allocate AppWork as" +
                    "requested resource is less than 0! " +
                    "requested resource type=[%s], " + "requested resource=%s";
    public static final String GREATER_THAN_MAX_RESOURCE_MESSAGE_TMP =
            "Invalid resource request! Can't allocate AppWork as" +
                    "requested resource greater than maximum allowed allocation. " +
                    "requested resource type=[%s], " +
                    "requested resource=%s, maximum allowed allocation=%s.";
    public static final String UNKNOWN_RESON_MESSAGE_TMP =
            "Invalid resource request! Can't allocate AppWork for an unknown reason!" +
                    "requested resource type=[%s], " + "requested resource=%s";

    public enum InvalidResourceType {
        LESS_THAN_ZERO,
        GREATER_THAN_MAX,
        UNKNOWN
    }

    private final InvalidResourceType invalidType;

    public InvalidResourceRequestException(String message, InvalidResourceType type) {
        super(message);
        this.invalidType = type;
    }

    public InvalidResourceRequestException(String message) {
        this(message, InvalidResourceType.UNKNOWN);
    }

    public InvalidResourceRequestException(String message, Throwable e) {
        super(message, e);
        this.invalidType = InvalidResourceType.UNKNOWN;
    }
}
