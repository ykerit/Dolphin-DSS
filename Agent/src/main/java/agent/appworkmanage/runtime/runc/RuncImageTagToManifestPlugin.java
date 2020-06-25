package agent.appworkmanage.runtime.runc;

import common.service.Service;

import java.io.IOException;

public interface RuncImageTagToManifestPlugin extends Service {
  ImageManifest getManifestFromImageTag(String imageTag) throws IOException;

  String getHashFromImageTag(String imageTag);
}
