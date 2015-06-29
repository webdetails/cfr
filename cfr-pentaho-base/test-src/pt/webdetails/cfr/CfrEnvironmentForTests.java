/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
