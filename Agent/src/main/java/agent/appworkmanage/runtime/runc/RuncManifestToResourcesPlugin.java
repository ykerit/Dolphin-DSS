package agent.appworkmanage.runtime.runc;

import common.resource.LocalResource;
import common.service.Service;

import java.io.IOException;
import java.util.List;

public interface RuncManifestToResourcesPlugin extends Service {
  //The layers should be returned in the order in which they
  // appear in the manifest
  List<LocalResource> getLayerResources(ImageManifest manifest)
      throws IOException;

  LocalResource getConfigResource(ImageManifest manifest) throws IOException;
}
