/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
