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


package pt.webdetails.cfr.file;

import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.persistence.PersistenceEngineForTests;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class MetadataReaderForTests extends MetadataReader {

  public MetadataReaderForTests( CfrService service ) {
    super( service );
  }

  @Override protected PersistenceEngine getPersistenceEngine() {
    return PersistenceEngineForTests.getInstance();
  }
}
