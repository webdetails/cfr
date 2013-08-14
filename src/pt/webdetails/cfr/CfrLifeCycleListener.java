/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cfr;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class CfrLifeCycleListener implements IPluginLifecycleListener {
  static Log logger = LogFactory.getLog(CfrLifeCycleListener.class);
    
  @Override
  public void init() throws PluginLifecycleException {
      logger.debug("Init for CFR");
      PersistenceEngine engine = PersistenceEngine.getInstance();
      engine.initializeClass("UploadedFiles");
      engine.initializeClass("UploadedFilesPermissions");
  }

  @Override
  public void loaded() throws PluginLifecycleException {    
    String defaultRepositoryPath = PentahoSystem.getApplicationContext().getSolutionPath("/system/.cfr");
    File dirPath = new File(defaultRepositoryPath);
    if (!dirPath.exists()) {
      dirPath.mkdir();
    }    
    
    //Run the init method for the chosen FileRepository
    IFileRepository repository = getRepository();
    repository.init();
            
    
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
      logger.debug("Unload for CFR");
      getRepository().shutdown();
  }
  
  
  private IFileRepository getRepository() {
    String repositoryClass = new CfrPluginSettings().getRepositoryClass();
    try {
      return (IFileRepository)Class.forName(repositoryClass).newInstance();    
    } catch (ClassNotFoundException cnfe) {
      logger.fatal("Class for repository " + repositoryClass + " not found. CFR will not be available", cnfe);
    } catch (InstantiationException ie) {
      logger.fatal("Instantiaton of class failed", ie);
    } catch (IllegalAccessException iae) {
      logger.fatal("Illegal access to repository class", iae);
    }
    return null;
  }
  
}
