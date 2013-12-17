
package pt.webdetails.cfr;

import pt.webdetails.cfr.repository.PentahoRepositoryFileRepositoryForTests;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CfrEnvironmentForTests extends CfrEnvironment {

  private static final String BASE_PATH = System.getProperty( "user.dir" ) + "test-resources";

  @Override protected String getPluginRepositoryDir() {
    return BASE_PATH;
  }

  @Override
  public IReadAccess getPluginRepositoryReader( String basePath ) {
    return new PentahoRepositoryFileRepositoryForTests().createPluginSystemAccess( BASE_PATH );
  }

  @Override public IReadAccess getPluginSystemReader( String basePath ) {
    return new PentahoRepositoryFileRepositoryForTests().createPluginSystemAccess( BASE_PATH );
  }
}
