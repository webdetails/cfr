/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2002 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cfr;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class CfrLifeCycleListener extends SimpleLifeCycleListener {
  static Log logger = LogFactory.getLog( CfrLifeCycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CFR" );
    PluginEnvironment.init( new CfrEnvironment() );
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
    super.unLoaded();
    getRepository().shutdown();
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return null;
  }

  private IFileRepository getRepository() {
    return new CfrEnvironment().getRepository();
  }

}
