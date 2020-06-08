package agent.appworkmanage.appwork;

import agent.appworkmanage.Localize.LocalResourceRequest;
import common.struct.AppWorkId;

import java.nio.file.Path;

public class AppWorkResourceLocalizedEvent extends AppWorkResourceEvent {
    private final Path local;

    public AppWorkResourceLocalizedEvent(AppWorkId appWorkId, LocalResourceRequest resourceRequest, Path loc) {
        super(appWorkId, AppWorkEventType.RESOURCE_LOCALIZED, resourceRequest);
        this.local = loc;
    }

    public Path getLocation() {
        return local;
    }
}
