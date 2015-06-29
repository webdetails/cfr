/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cfr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cfr.file.FileStorer;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;

import java.io.IOException;
import java.util.Properties;

public class CfrEnvironment extends PentahoPluginEnvironment implements ICfrEnvironment {

  private static final String PLUGIN_NAME = "cfr";
  private static final String PROPERTIES_FILE = "cfr.properties";
  private static boolean persistenceEngineInitialized = false;

  private IBeanFactory factory;
  private IFileRepository repository;
  private static Properties config;

  static Log logger = LogFactory.getLog( CfrEnvironment.class );

  public void init( IBeanFactory factory ) {
    this.factory = factory;

    if ( factory.containsBean( IFileRepository.class.getSimpleName() ) ) {
      repository = (IFileRepository) factory.getBean( IFileRepository.class.getSimpleName() );
    }

    super.init( this );
  }

  public static Properties getConfig() {
    if ( config == null ) {
      IReadAccess sysReader = getInstance().getPluginSystemReader( "" );
      if ( sysReader.fileExists( PROPERTIES_FILE ) ) {
        config = new Properties();
        try {
          config.load( sysReader.getFileInputStream( PROPERTIES_FILE ) );
          logger.debug( PROPERTIES_FILE + " read ok." );
        } catch ( IOException e ) {
          logger.error( "Error reading " + PROPERTIES_FILE, e );
        }
      } else {
        logger.error( "Unable to load " + PROPERTIES_FILE );
      }
    }
    return config;
  }

  public CfrEnvironment() {
    init( new AbstractBeanFactory(){
      @Override
      public String getSpringXMLFilename(){ return "cfr.spring.xml"; }
    });
  }

  @Override public IPluginUtils getPluginUtils() {
    return null;
  }

  @Override public String getPluginName() {
    return PLUGIN_NAME;
  }

  @Override public ISessionUtils getSessionUtils() {
    return null;
  }

  @Override public void reload() {

  }

  public IFileRepository getRepository() {
    return repository;
  }

  public static PersistenceEngine getPersistenceEngine() {

    PersistenceEngine engine = PersistenceEngine.getInstance();
    if ( !persistenceEngineInitialized ) {
      synchronized ( CfrEnvironment.class ) {
        if ( !persistenceEngineInitialized ) {
          if ( !engine.classExists( FileStorer.FILE_METADATA_STORE_CLASS ) ) {
            engine.initializeClass( FileStorer.FILE_METADATA_STORE_CLASS );
          }
          if ( !engine.classExists( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS ) ) {
            engine.initializeClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
          }
          persistenceEngineInitialized = true;
        }

      }
    }

    return engine;
  }


}
