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
