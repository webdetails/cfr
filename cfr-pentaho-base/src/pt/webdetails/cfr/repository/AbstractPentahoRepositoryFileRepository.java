/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import pt.webdetails.cfr.CfrEnvironment;
import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;
import pt.webdetails.cfr.utils.GenericBasicFileFilter;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;


public abstract class AbstractPentahoRepositoryFileRepository implements IFileRepository {

  static Log logger = LogFactory.getLog( AbstractPentahoRepositoryFileRepository.class );
  private final String SOLUTION = "/";
  private String basePath;

  public IReadAccess getReadAccess() {
    return CfrEnvironment.getInstance().getUserContentAccess( SOLUTION );
  }

  public IRWAccess getRWAccess() {
    return CfrEnvironment.getInstance().getUserContentAccess( SOLUTION );
  }

  protected IPentahoSession getUserSession() {
    return PentahoSessionHolder.getSession();
  }

  @Override
  public void init() {

  }

  @Override
  public boolean storeFile( byte[] content, String fileName, String relativePath ) {
    relativePath = getBasePath() + relativePath;
    return getRWAccess().saveFile( relativePath + "/" + fileName, new ByteArrayInputStream( content ) );
  }

  @Override
  public IFile[] listFiles( String startPath ) {
    if ( !StringUtils.isEmpty( getBasePath() ) ) {
      startPath = getBasePath() + "/" + startPath;
    }
    List<IBasicFile> repositoryFiles =
      getReadAccess().listFiles( startPath, new GenericBasicFileFilter( "", "", true ), 1, true, false );

    List<IFile> result = new ArrayList<IFile>();
    for ( final IBasicFile file : repositoryFiles ) {
      if ( !file.getName().equals( startPath ) ) {
        result.add( new IFile() {

          @Override
          public String getFullPath() {
            return file.getFullPath();
          }

          @Override
          public String getName() {
            return file.getName();
          }

          @Override
          public boolean isDirectory() {
            return file.isDirectory();
          }

          @Override
          public boolean isFile() {
            return !file.isDirectory();
          }

        } );
      }
    }
    Collections.sort( result, new Comparator<IFile>() {
      @Override public int compare( IFile file1, IFile file2 ) {
        return file1.getName().compareTo( file2.getName() );
      }
    } );

    return result.toArray( new IFile[ result.size() ] );
  }

  @Override
  public CfrFile getFile( String fullName ) {
    if ( !StringUtils.isEmpty( getBasePath() ) ) {
      fullName = getBasePath() + "/" + fullName;
    }
    InputStream is = null;
    try {
      is = getReadAccess().fetchFile( fullName ).getContents();
    } catch ( FileNotFoundException fnfe ) {
      logger.error( "file not found: " + fullName, fnfe );
      return null;
    } catch ( IOException ioe ) {
      logger.error( "file not found: " + fullName, ioe );
    }

    byte[] contents = null;
    try {
      contents = new byte[ is.available() ];
      is.read( contents );
      is.close();
    } catch ( IOException ioe ) {
      logger.error( "IOException while reading file content", ioe );
      return null;
    }
    String[] pathComponents = fullName.split( "/" );
    String fileName = pathComponents[ pathComponents.length - 1 ];
    CfrFile resultFile = new CfrFile( fileName, fullName.replace( fileName, "" ), contents );
    return resultFile;
  }

  @Override
  public boolean createFolder( String fullPathName ) {
    if ( getRWAccess().fileExists( fullPathName ) && getRWAccess().fetchFile( fullPathName ).isDirectory() ) {
      return true;
    }
    return getRWAccess().createFolder( fullPathName );
  }

  @Override
  public boolean deleteFile( String fullName ) {
    return getRWAccess().deleteFile( getBasePath() + "/" + fullName );
  }

  @Override
  public void shutdown() {
  }

  //can be set in cfr.spring.xml
  public void setBasePath( String basePath ) {
    this.basePath = basePath;
  }

  private String getBasePath() {
    return basePath != null ? basePath : getDefaultBasePath();
  }

  protected abstract String getDefaultBasePath();

}
