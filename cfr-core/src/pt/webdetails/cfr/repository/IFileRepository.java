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

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;

public interface IFileRepository {

  public void init();

  public boolean storeFile( byte[] content, String fileName, String relativePath );

  public IFile[] listFiles( String startPath );

  public CfrFile getFile( String fullName );

  public boolean createFolder( String fullPathName );

  public boolean deleteFile( String fullName );

  public void shutdown();

  public boolean fileExists( String fullName );

}
