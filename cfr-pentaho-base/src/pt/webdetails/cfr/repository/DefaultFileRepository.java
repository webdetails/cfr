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

import pt.webdetails.cfr.CfrPluginSettings;

public class DefaultFileRepository extends AbstractDefaultFileRepository {

  private String UPLOAD_DIR = "system/.cfr";

  @Override
  protected String getBasePath() {
    if ( basePath == null ) {
      CfrPluginSettings settings = new CfrPluginSettings();
      basePath = settings.getBasePath() + UPLOAD_DIR;
    }
    return basePath;
  }

  public void setBasePath( String basePath ) {
    this.basePath = basePath;
  }
}
