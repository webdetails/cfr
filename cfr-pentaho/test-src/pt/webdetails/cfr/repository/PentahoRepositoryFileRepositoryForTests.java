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

  @Override IReadAccess getReadAccess() {
    return createPluginSystemAccess( "" );
  }

  @Override IRWAccess getRWAccess() {
    return createPluginSystemAccess( "" );
  }

  @Override
  protected IPentahoSession getUserSession() {
    return null;
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
