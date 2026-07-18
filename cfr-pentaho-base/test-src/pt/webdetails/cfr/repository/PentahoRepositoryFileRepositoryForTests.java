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


package pt.webdetails.cfr.repository;

import org.pentaho.platform.api.engine.IPentahoSession;

import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;

import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.pentaho.SystemPluginResourceAccess;

import java.io.File;

public class PentahoRepositoryFileRepositoryForTests extends AbstractPentahoRepositoryFileRepository {

  private boolean hasAccess;


  public PentahoRepositoryFileRepositoryForTests() {
    this( true );
  }

  public PentahoRepositoryFileRepositoryForTests( boolean hasAccess ) {
    this.hasAccess = hasAccess;
  }

  @Override
  public IReadAccess getReadAccess() {
    return createPluginSystemAccess( "/" );
  }

  @Override
  public IRWAccess getRWAccess() {
    return createPluginSystemAccess( "/" );
  }

  @Override
  protected IPentahoSession getUserSession() {
    return null;
  }

  @Override protected String getDefaultBasePath() {
    return "";
  }

  public SystemPluginResourceAccess createPluginSystemAccess( String basePath ) {
    return new SystemPluginResourceAccess( getMockClassLoader(), basePath );
  }

  private PluginClassLoader getPluginClassLoader() {
    return getMockClassLoader();
  }

  private PluginClassLoader getMockClassLoader() {
    String systemPath = getTestResourcesPath();
    return new PluginClassLoader( new File( systemPath ), this.getClass().getClassLoader() );
  }

  private String getTestResourcesPath() {
    return System.getProperty( "user.dir" ) + "/test-resources";
  }

}
