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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.CfrEnvironment;
import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

import com.orientechnologies.orient.core.record.impl.ODocument;
import pt.webdetails.cpf.utils.MimeTypes;


public class FileStorer {

  public static final String FILE_METADATA_STORE_CLASS = "UploadedFiles";

  public static final String FILE_PERMISSIONS_METADATA_STORE_CLASS = "UploadedFilesPermissions";

  public static final String UPLOAD_SECURITY_MATCHERS = "upload.security.matchers";
  public static final String UPLOAD_SECURITY_WHITELIST = "upload.security.whitelist";
  public static final String UPLOAD_SECURITY_BLACKLIST = "upload.security.blacklist";
  public static final String EXTENSION_MATCHER = "ext";
  public static final String WHITELIST_MATCHER = "wht";
  public static final String BLACKLIST_MATCHER = "blk";
  public static final String ERR_WHITELIST = "MimeType \"%s\" is not whitelisted!";
  public static final String ERR_BLACKLIST = "MimeType \"%s\" is blacklisted!";
  public static final String ERR_EXTENSION = "File content did not match its extension";


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
  public JSONObject storeFile( String file, String relativePath, byte[] contents, String user ) throws JSONException {
    // Store file in FileRepository
    String path = relativePath;
    String fileName = file;
    logger.debug( String.format( "Store Operation initiated: User - \"%s\", File - \"%s\", relativePath - \"%s\"",
        user, fileName, path ) );
    JSONObject resultObj = checkContents( contents, fileName );
    if ( !resultObj.getBoolean( "result" ) ) {
      return resultObj;
    }

    boolean result = this.repository.storeFile( contents, fileName, path );
    if ( !result ) {
      logger.error( String.format( "Store Operation failed: User - \"%s\", File - \"%s\", relativePath - \"%s\"",
          user, fileName, path ) );
      return new JSONObject().put( "result", false );
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
      logger.error( String.format( "Store Operation failed: User - \"%s\", File - \"%s\", relativePath - \"%s\"",
          user, fileName, path ) );
      return new JSONObject().put( "result", false );
    }

    String id = null;
    if ( fileEntities != null && fileEntities.size() > 0 ) {
      id = fileEntities.get( 0 ).getIdentity().toString();
    }

    result = getPersistenceEngine().store( id, FILE_METADATA_STORE_CLASS, obj ) != null;
    resultObj.put( "result", result );
    logger.debug( String
        .format( "Store Operation over: User - \"%s\", File - \"%s\", relativePath - \"%s\", Status - \"%s\"", user,
        fileName, path, result ? "OK" : "ERROR" ) );
    return resultObj;
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
      Map<String, Object> params = new HashMap<String, Object>();
      StringBuilder deleteFileBuilder =
          new StringBuilder( String.format( "delete from %s", FILE_METADATA_STORE_CLASS ) );
      StringBuilder whereBuilder = new StringBuilder();

      if ( path != null ) {
        whereBuilder.append( "file = :fileName" );
        params.put( "fileName", path );
      }

      if ( id != null ) {
        if ( whereBuilder.length() > 0 ) {
          whereBuilder.append( " and " );
        }

        whereBuilder.append( "id = :id" );
        params.put( "id", id );
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
      Map<String, Object> params = new HashMap<String, Object>();
      StringBuilder deleteCommandBuilder =
          new StringBuilder( String.format( "delete from %s", FILE_PERMISSIONS_METADATA_STORE_CLASS ) );
      StringBuilder whereBuilder = new StringBuilder();

      if ( path != null ) {
        whereBuilder.append( "file = :fileName" );
        params.put( "fileName", path );
      }

      if ( id != null ) {
        if ( whereBuilder.length() > 0 ) {
          whereBuilder.append( " and " );
        }

        whereBuilder.append( "id = :id" );
        params.put( "id", id );
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

  protected JSONObject checkContents( byte[] contents, String fileName ) throws JSONException {
    JSONObject result = new JSONObject();
    Properties cfrConfig = getConfig();
    String matchers = cfrConfig.getProperty( UPLOAD_SECURITY_MATCHERS, "" );
    if ( matchers.length() == 0 ) {
      result.put( "result", true );
      return result;
    }
    List<String> securityMatchers = Arrays.asList( matchers.split( "," ) );

    boolean success = securityMatchers.size() == 1 && securityMatchers.contains( BLACKLIST_MATCHER );
    String contentMimeType = detectMimeTypeFromContent( contents, fileName );
    String inferedMimeType = MimeTypes.getMimeType( fileName );
    if ( inferedMimeType.equals( "text/javascript" ) ) {
      inferedMimeType = "application/javascript";
    }
    String message = "";


    //we start by checking against extension
    if ( securityMatchers.contains( EXTENSION_MATCHER ) ) {
      if ( contentMimeType != null && contentMimeType.equals( inferedMimeType ) ) {
        success = true;
      } else {
        message = ERR_EXTENSION;
        logger.error( message );
      }
    }
    //even if extension fails, the mimeType may be whitelisted
    if ( !success && securityMatchers.contains( WHITELIST_MATCHER ) ) {
      if ( Arrays.asList( cfrConfig.getProperty( UPLOAD_SECURITY_WHITELIST, "" ).split( "," ) ).contains(
          contentMimeType ) ) {
        success = true;
        logger.debug( "MimeType " + contentMimeType + " whitelisted" );
      } else {
        logger.error( String.format( ERR_WHITELIST, contentMimeType ) );
        if ( message.length() > 0 ) {
          message += " and ";
        }
        message += String.format( ERR_WHITELIST, contentMimeType );
      }
    }
    //the mimeType may be blacklisted
    if ( success && securityMatchers.contains( BLACKLIST_MATCHER ) ) {
      if ( Arrays.asList( cfrConfig.getProperty( UPLOAD_SECURITY_BLACKLIST, "" ).split( "," ) ).contains(
          contentMimeType ) ) {
        success = false;
        message = String.format( ERR_BLACKLIST, contentMimeType );
        logger.error( message );
      }
    }

    result.put( "result", success );
    result.put( "message", message );

    return result;
  }

  protected Properties getConfig() {
    return CfrEnvironment.getConfig();
  }

  protected String detectMimeTypeFromContent( byte[] contents, String fileName ) {
    return new Tika().detect( contents, fileName );
  }

}


