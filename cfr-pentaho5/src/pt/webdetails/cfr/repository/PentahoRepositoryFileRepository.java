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


public class PentahoRepositoryFileRepository extends AbstractPentahoRepositoryFileRepository {

  @Override protected String getDefaultBasePath() {
    return "public/cfr";
  }
}
