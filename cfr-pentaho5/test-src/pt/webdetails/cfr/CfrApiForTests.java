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

import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.file.MetadataReader;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;
import pt.webdetails.cfr.repository.IFileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CfrApiForTests extends CfrApi {

  private CfrService cfrService;
  private List<String> files;
  private List<String> nonAdminUserFiles;
  private boolean isAdmin;

  public CfrApiForTests() {
    this.nonAdminUserFiles = new ArrayList<String>();
  }

  protected CfrService getCfrService() {
    return cfrService;
  }

  @Override protected boolean storeFile( String file, String id, Set<FilePermissionEnum> validPermissions ) {
    return isAdmin || nonAdminUserFiles.contains( file );
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

  public void setNonAdminUserFiles( List<String> nonAdminUserFiles ) {
    this.nonAdminUserFiles = nonAdminUserFiles;
  }

  public void setIsAdmin( boolean isAdmin ) {
    this.isAdmin = isAdmin;
  }

  protected IFileRepository getRepository() {
    return new DefaultFileRepositoryForTests();
  }

  public void reloadMetadataReader() {
    super.mr = new MetadataReader( cfrService );
  }

  @Override
  public boolean isUserAdmin() {
    return isAdmin;
  }

}
