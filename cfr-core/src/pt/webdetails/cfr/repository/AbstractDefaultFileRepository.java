/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;

/**
 * The default file repository takes system/.cfr as its base path. All files and folders are created here.
 *
 * @author pedrovale
 */
public abstract class AbstractDefaultFileRepository implements IFileRepository {

  static Log logger = LogFactory.getLog( AbstractDefaultFileRepository.class );

  protected String basePath;

  abstract String getBasePath();

  @Override
  public void init() {

  }

  @Override
  public void shutdown() {

  }

  @Override
  public boolean storeFile( byte[] content, String fileName, String relativePath ) {

    if ( !checkPath( relativePath ) ) {
      return false;
    }

    String fullPath = getBasePath() + File.separator + relativePath;
    File f = new File( fullPath, fileName );

    if ( !f.getParentFile().exists() ) {
      f.getParentFile().mkdirs();
    }
    FileOutputStream fos;

    try {
      fos = new FileOutputStream( f, false );
      fos.write( content );
      fos.flush();
      fos.close();
    } catch ( FileNotFoundException fnfe ) {
      logger.error( "Unable to create file. Check permissions on folder " + fullPath, fnfe );
      return false;
    } catch ( IOException ioe ) {
      logger.error( "Error caught while writing file", ioe );
      return false;
    }

    return true;
  }

  @Override
  public boolean createFolder( String fullPathName ) {

    if ( !checkPath( fullPathName ) ) {
      return false;
    }

    File f = new File( getBasePath() + File.separator + fullPathName );
    if ( !f.exists() ) {
      return f.mkdirs();
    }

    return true;
  }

  @Override
  public boolean deleteFile( String fullName ) {

    if ( !checkPath( fullName ) ) {
      return false;
    }

    File f = new File( getBasePath() + File.separator + fullName );
    return f.delete();
  }

  @Override
  public IFile[] listFiles( String startPath ) {

    if ( !checkPath( startPath ) ) {
      return new IFile[ 0 ];
    }

    File f = new File( getBasePath() + File.separator + startPath );
    File[] files = f.listFiles();
    if ( files == null ) {
      return new IFile[ 0 ];
    }

    IFile[] result = new IFile[ files.length ];
    for ( int i = 0; i < files.length; i++ ) {
      final File listedFile = files[ i ];
      result[ i ] = new IFile() {

        @Override
        public String getFullPath() {
          return listedFile.getPath();
        }

        @Override
        public String getName() {
          return listedFile.getName();
        }

        @Override
        public boolean isDirectory() {
          return listedFile.isDirectory();
        }

        @Override
        public boolean isFile() {
          return listedFile.isFile();
        }

      };
    }

    return result;
  }

  @Override
  public CfrFile getFile( String fullName ) {
    CfrFile result = null;

    if ( !checkPath( fullName ) ) {
      return null;
    }

    File f = null;
    if ( fullName.startsWith( File.separator ) ) {
      f = new File( getBasePath() + fullName );
    } else {
      f = new File( getBasePath() + File.separator + fullName );
    }
    if ( !f.exists() ) {
      logger.error( "File not found for " + fullName + ". Returning null." );
      return null;
    }

    result = new CfrFile( f.getName(), f.getPath().replace( f.getName(), "" ), f );
    return result;
  }

  @Override
  public boolean fileExists( String fullName ) {
    if ( !checkPath( fullName ) ) {
      return false;
    }
    File f;
    if ( fullName.startsWith( File.separator ) ) {
      f = new File( getBasePath() + fullName );
    } else {
      f = new File( getBasePath() + File.separator + fullName );
    }
    return f.exists();
  }

  /**
   * Checks if path contains ../ - we won't allow any back tracking in paths, even if they might be valid
   *
   * @param path
   * @return <i>true</i> if path does not contain bactracking info
   */
  private boolean checkPath( String path ) {
    boolean result = !path.contains( ".." );
    if ( !result ) {
      logger.warn( "Path parameter contains unsupported back tracking path element: " + path );
    }
    return result;
  }

}
