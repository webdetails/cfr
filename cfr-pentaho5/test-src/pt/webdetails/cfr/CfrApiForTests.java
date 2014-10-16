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

import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.file.MetadataReader;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;
import pt.webdetails.cfr.repository.IFileRepository;

import java.util.List;
import java.util.Set;

public class CfrApiForTests extends CfrApi {

  private CfrService cfrService;
  private List<String> files;

  protected CfrService getCfrService() {
    return cfrService;
  }

  @Override protected boolean storeFile( String file, String id, Set<FilePermissionEnum> validPermissions ) {
    return true;
  }

  @Override protected List<String> getFileNameTree( String path ) {
    return files;
  }

  public void setCfrService( CfrService cfrService ) {
    this.cfrService = cfrService;
  }

  public void setFileNames( List<String> files ) {
    this.files = files;
  }

  protected IFileRepository getRepository() {
    return new DefaultFileRepositoryForTests();
  }

  public void reloadMetadataReader() {
    super.mr = new MetadataReader( cfrService );
  }

  @Override
  public boolean isUserAdmin() {
    return true;
  }

}
