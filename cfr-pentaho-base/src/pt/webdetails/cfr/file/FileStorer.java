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

package pt.webdetails.cfr.file;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.CfrEnvironment;
import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class FileStorer {

  public static final String FILE_METADATA_STORE_CLASS = "UploadedFiles";

  public static final String FILE_PERMISSIONS_METADATA_STORE_CLASS = "UploadedFilesPermissions";

  protected static final Log logger = LogFactory.getLog( FileStorer.class );

  private static boolean persistenceEngineInitialized = false;

  private static PersistenceEngine defaultPersistenceEngine;

  private IFileRepository repository;

  public FileStorer( IFileRepository repository ) {
    this.repository = repository;
  }

  protected FileStorer( IFileRepository repository, PersistenceEngine pe ) {
    this.repository = repository;
    this.defaultPersistenceEngine = pe;
  }

  protected static PersistenceEngine getPersistenceEngine() {
    return defaultPersistenceEngine != null ? defaultPersistenceEngine : CfrEnvironment.getPersistenceEngine();
  }

  /**
   * @param file         Name of the file to be stored
   * @param relativePath Path relative to repository root for the file to be stored in
   * @param contents     File content
   * @param user
   * @return
   */
  public boolean storeFile( String file, String relativePath, byte[] contents, String user ) {
    // Store file in FileRepository
    String path = relativePath;
    String fileName = file;
    boolean result = this.repository.storeFile( contents, fileName, path );
    if ( !result ) {
      logger.error( "Could not save file in repository. Returning false" );
      return false;
    }

    MetadataReader mr = getMetadataReader();
    List<ODocument> fileEntities = null;
    try {
      fileEntities = mr.getFileEntities( getFullFileName( relativePath, file ) );
    } catch ( JSONException e ) {
      logger.trace( String.format( "unable to retrieve file %s metadata", file ), e );
    }

    JSONObject obj = new JSONObject();
    try {
      obj.put( "user", user );
      obj.put( "file", getFullFileName( path, fileName ) );
      obj.put( "uploadDate", new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() ) );
    } catch ( JSONException jse ) {
      logger.error( "An error ocurred while creating json object representing the upload.", jse );
      return false;
    }

    String id = null;
    if ( fileEntities != null && fileEntities.size() > 0 ) {
      id = fileEntities.get( 0 ).getIdentity().toString();
    }

    result = getPersistenceEngine().store( id, FILE_METADATA_STORE_CLASS, obj ) != null;

    return result;
  }

  public String getFullFileName( String path, String filename ) {
    StringBuilder builder = new StringBuilder( filename );
    if ( path != null && path.length() > 0 && !path.endsWith( "/" ) ) {
      builder.insert( 0, path + "/" );
    }
    return builder.toString();
  }

  public static boolean storeFilePermissions( FilePermissionMetadata permission ) {
    boolean result = false;
    MetadataReader reader = new MetadataReader( new CfrService() );
    final CfrService service = new CfrService();

    if ( permission != null ) {
      try {
        // TODO(rafa): verify that current user is the folder/file owner or is an admin
        if ( service.isCurrentUserAdmin() || reader.isCurrentUserOwner( permission.getFile() ) ) {

          logger.debug( String.format( "current user is an administrator or the owner of the file: %s", permission
            .getFile() ) );

          JSONObject persistedPermissions = null;
          JSONObject permissionToPersist = permission.toJson();

          // verify that the file hasn't already permissions defined
          List<String> ids = new ArrayList<String>();
          ids.add( permission.getId() );
          List<ODocument> currentPermissions = reader.getUniquePermissionEntities( permission.getFile(), ids, null );
          if ( currentPermissions == null || currentPermissions.size() == 0 ) {
            persistedPermissions =
              getPersistenceEngine().store( null, FILE_PERMISSIONS_METADATA_STORE_CLASS, permissionToPersist );
          } else {
            String id = currentPermissions.get( 0 ).getIdentity().toString();
            persistedPermissions =
              getPersistenceEngine().store( id, FILE_PERMISSIONS_METADATA_STORE_CLASS, permissionToPersist );
          }

          result = null != persistedPermissions;
        }
      } catch ( JSONException e ) {
        logger.error( "unable to store " + permission, e );
      }
    }

    if ( result == false ) {
      logger.warn( String.format( "current user doesn't have permissions to set permissions on folder/file: %s",
        permission.getFile() ) );
    }

    return result;
  }

  /**
   * @param path Full path of the folder/file
   * @param id   Group/User Id
   */
  public static boolean removeFile( String path, String id ) {

    CfrService service = new CfrService();
    MetadataReader reader = new MetadataReader( service );
    if ( service.isCurrentUserAdmin() || reader.isCurrentUserOwner( path ) ) {
      Map<String, Object> params = Collections.emptyMap();
      StringBuilder deleteFileBuilder =
        new StringBuilder( String.format( "delete from %s", FILE_METADATA_STORE_CLASS ) );
      StringBuilder whereBuilder = new StringBuilder();

      if ( path != null ) {
        whereBuilder.append( "file = '" ).append( path ).append( "'" );
      }

      if ( id != null ) {
        if ( whereBuilder.length() > 0 ) {
          whereBuilder.append( " and " );
        }

        whereBuilder.append( "id = '" ).append( id ).append( "'" );
      }

      if ( whereBuilder.length() > 0 ) {
        whereBuilder.insert( 0, " where " );
      }

      String cmd = deleteFileBuilder.append( whereBuilder.toString() ).toString();
      getPersistenceEngine().executeCommand( cmd, params );
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param path Full path of the folder/file
   * @param id   Group/User Id
   */
  public static boolean deletePermissions( String path, String id ) {
    CfrService service = new CfrService();
    MetadataReader reader = new MetadataReader( service );
    if ( service.isCurrentUserAdmin() || reader.isCurrentUserOwner( path ) ) {
      Map<String, Object> params = Collections.emptyMap();
      StringBuilder deleteCommandBuilder =
        new StringBuilder( String.format( "delete from %s", FILE_PERMISSIONS_METADATA_STORE_CLASS ) );
      StringBuilder whereBuilder = new StringBuilder();

      if ( path != null ) {
        whereBuilder.append( "file = '" ).append( path ).append( "'" );
      }

      if ( id != null ) {
        if ( whereBuilder.length() > 0 ) {
          whereBuilder.append( " and " );
        }

        whereBuilder.append( "id = '" ).append( id ).append( "'" );
      }

      if ( whereBuilder.length() > 0 ) {
        whereBuilder.insert( 0, " where " );
      }

      String cmd = deleteCommandBuilder.append( whereBuilder.toString() ).toString();
      getPersistenceEngine().executeCommand( cmd, params );
      return true;
    } else {
      return false;
    }
  }

  protected MetadataReader getMetadataReader() {
    return new MetadataReader( new CfrService() );
  }
}
