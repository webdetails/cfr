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

import pt.webdetails.cfr.persistence.PersistenceEngineForTests;
import pt.webdetails.cfr.repository.IFileRepository;

import java.util.Properties;

public class FileStorerForTests extends FileStorer {

  private Properties injectedConfig;
  private String mimeTypeFromContent;
  public FileStorerForTests( IFileRepository repository ) {
    super( repository, PersistenceEngineForTests.getInstance() );
  }

  @Override
  protected MetadataReader getMetadataReader() {
    return new MetadataReaderForTests( null );
  }

  @Override
  protected Properties getConfig() {
    return injectedConfig != null ? injectedConfig : new Properties();
  }

  @Override
  protected String detectMimeTypeFromContent( byte[] contents, String fileName ) {
    return mimeTypeFromContent;
  }

  public void setMimeTypeFromContent( String mimeType ) {
    this.mimeTypeFromContent = mimeType;
  }

  public void setInjectedConfig( Properties config ) {
    this.injectedConfig = config;
  }
}
