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

import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cfr.repository.PentahoRepositoryFileRepositoryForTests;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CfrEnvironmentForTests extends CfrEnvironment {

  private static final String BASE_PATH = System.getProperty( "user.dir" ) + "test-resources";

  public CfrEnvironmentForTests() {
  }

  @Override
  public void init( IBeanFactory factory ) {

  }

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
