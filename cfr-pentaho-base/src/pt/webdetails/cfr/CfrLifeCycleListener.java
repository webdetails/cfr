/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cfr;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class CfrLifeCycleListener implements IPluginLifecycleListener {
  static Log logger = LogFactory.getLog( CfrLifeCycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CFR" );
    PluginEnvironment.init( new CfrEnvironment() );
    PersistenceEngine engine = PersistenceEngine.getInstance();
    engine.initializeClass( "UploadedFiles" );
    engine.initializeClass( "UploadedFilesPermissions" );
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    String defaultRepositoryPath = PentahoSystem.getApplicationContext().getSolutionPath( "/system/.cfr" );
    File dirPath = new File( defaultRepositoryPath );
    if ( !dirPath.exists() ) {
      dirPath.mkdir();
    }

    // Run the init method for the chosen FileRepository
    IFileRepository repository = getRepository();
    repository.init();

  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    logger.debug( "Unload for CFR" );
    getRepository().shutdown();
  }

  private IFileRepository getRepository() {
    return new CfrEnvironment().getRepository();
  }

}
