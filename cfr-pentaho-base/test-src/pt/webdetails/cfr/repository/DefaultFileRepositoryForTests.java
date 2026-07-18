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
