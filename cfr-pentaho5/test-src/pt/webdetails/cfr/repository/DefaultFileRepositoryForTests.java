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

package pt.webdetails.cfr.repository;

public class DefaultFileRepositoryForTests extends DefaultFileRepository {

  private boolean result;
  private boolean normal;

  public DefaultFileRepositoryForTests( boolean expectedResult ) {
    this.result = expectedResult;
  }

  public DefaultFileRepositoryForTests() {
    this.normal = true;
  }

  @Override
  protected String getBasePath() {
    return "./test-resources";
  }

  @Override
  public boolean storeFile( byte[] content, String fileName, String relativePath ) {
    if ( this.normal ) {
      return super.storeFile( content, fileName, relativePath );
    }
    return this.result;
  }

}
