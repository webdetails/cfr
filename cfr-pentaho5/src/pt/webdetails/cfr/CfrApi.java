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

package pt.webdetails.cfr;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.FileStorer;
import pt.webdetails.cfr.file.IFile;
import pt.webdetails.cfr.file.MetadataReader;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.utils.MimeTypes;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.core.header.FormDataContentDisposition;

@Path( "cfr/api" )
public class CfrApi {

  private static final Log logger = LogFactory.getLog( CfrApi.class );

  private CfrService service = getCfrService();

  protected MetadataReader mr = new MetadataReader( service );

  private static final String UI_PATH = "cfr/presentation/";
  private static final String DEFAULT_STORE_ERROR_MESSAGE = "Something went wrong when trying to upload the file";
  private static final String DEFAULT_REMOVE_ERROR_MESSAGE = "Something went wrong when trying to remove the file";
  private static final String DEFAULT_CREATE_ERROR_MESSAGE = "Something went wrong when trying to create the folder";
  private static final String ROOT = ".";

  static String checkRelativePathSanity( String path ) {
    String result = path;

    if ( path != null ) {
      if ( result.startsWith( "./" ) ) {
        result = result.replaceFirst( "./", "" );
      }
      if ( result.startsWith( "." ) ) {
        result = result.replaceFirst( ".", "" );
      }
      if ( result.startsWith( "/" ) ) {
        result = result.replaceFirst( "/", "" );
      }

      if ( result.endsWith( "/" ) ) {
        result = result.substring( 0, result.length() - 1 );
      }
    }

    return result;
  }

  static String relativeFilePath( String baseDir, String file ) {
    String _baseDir = checkRelativePathSanity( baseDir );
    String _file = checkRelativePathSanity( file );
    String result = null;

    if ( _baseDir == null || _baseDir.length() == 0 ) {
      return _file;
    } else {
      if ( _baseDir.endsWith( "/" ) ) {
        result = new StringBuilder( _baseDir ).append( _file ).toString();
      } else {
        result = new StringBuilder( _baseDir ).append( '/' ).append( _file ).toString();
      }
    }

    return result;
  }

  @GET
  @Path( "/home" )
  public void home( @Context HttpServletRequest request,
                    @Context HttpServletResponse response ) throws Exception {


    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "solution", "system" );
    params.put( "path", "cfr/presentation/" );
    params.put( "file", "cfr.wcdf" );
    params.put( "absolute", "false" );
    params.put( "inferScheme", "false" );

    if ( Boolean.parseBoolean( request.getParameter( "debug" ) ) ) {
      params.put( "debug", "true" );
    }

    renderInCde( response, params );
  }


  @GET
  @Path( "/createFolder" )
  @Produces( MimeTypes.JSON )
  public String createFolder( @QueryParam( MethodParams.PATH ) String path ) throws Exception {
    path = checkRelativePathSanity( path );

    if ( path == null || StringUtils.isBlank( path ) ) {
      throw new Exception( "path is null or empty" );
    }
    String parentFolder = extractParentFolder( path );
    if ( !mr.isCurrentUserAllowed( FilePermissionEnum.WRITE, parentFolder ) ) {
      logger.error( "user has no write permission on folder '" + parentFolder + "'" );
      return buildResponseJson( false, DEFAULT_CREATE_ERROR_MESSAGE );
    }
    boolean createResult = getRepository().createFolder( path );
    return new JSONObject().put( "result", createResult ).toString();
  }
  private String extractParentFolder( String path ) {
    if ( path.contains( "/" ) ) {
      return path.substring( 0, path.lastIndexOf( "/" ) );
    } else {
      return ROOT;
    }
  }

  @POST
  @Path( "/remove" )
  @Produces( MimeTypes.JSON )
  public String remove( @QueryParam( MethodParams.FILENAME ) String filename ) throws Exception {
    String fullFileName = checkRelativePathSanity( filename );

    if ( fullFileName == null || StringUtils.isBlank( fullFileName ) ) {
      throw new Exception( "fileName is null or empty" );
    }
    if ( !mr.isCurrentUserAllowed( FilePermissionEnum.DELETE, fullFileName ) ) {
      logger.error( "user has no delete permission on file '" + fullFileName + "'" );
      return buildResponseJson( false, DEFAULT_REMOVE_ERROR_MESSAGE );
    }
    boolean removeResult = getRepository().deleteFile( fullFileName );
    boolean result = false;
    if ( removeResult ) {
      deletePermissions( fullFileName, null );
      result = FileStorer.removeFile( fullFileName, null );
    }

    return new JSONObject().put( "result", result ).toString();
  }

  @POST
  @Path( "/store" )
  @Consumes( "multipart/form-data" )
  @Produces( MimeTypes.JSON )
  public String store( @FormDataParam( "file" ) InputStream uploadedInputStream,
                       @FormDataParam( "file" ) FormDataContentDisposition fileDetail,
                       @FormDataParam( "path" ) String path ) throws JSONException {

    String fileName = checkRelativePathSanity( fileDetail.getFileName() ),
        savePath = checkRelativePathSanity( path );

    if ( fileName == null ) {
      logger.error( "parameter fileName must not be null" );
      return buildResponseJson( false, DEFAULT_STORE_ERROR_MESSAGE );
    }
    if ( savePath == null ) {
      logger.error( "parameter path must not be null" );
      return buildResponseJson( false, DEFAULT_STORE_ERROR_MESSAGE );
    }
    if ( !mr.isCurrentUserAllowed( FilePermissionEnum.WRITE, StringUtils.isEmpty( savePath ) ? ROOT : savePath ) ) {
      logger.error( "user has no write permission on path '" + savePath + "'" );
      return buildResponseJson( false, DEFAULT_STORE_ERROR_MESSAGE );
    }

    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
    byte[] contents;
    try {
      IOUtils.copy( uploadedInputStream, oStream );
      oStream.flush();
      contents = oStream.toByteArray();
      oStream.close();
    } catch ( IOException e ) {
      logger.error( e );
      return buildResponseJson( false, DEFAULT_STORE_ERROR_MESSAGE );
    }

    if ( contents == null ) {
      logger.error( "File content must not be null" );
      return buildResponseJson( false, DEFAULT_STORE_ERROR_MESSAGE );
    }

    FileStorer fileStorer = new FileStorer( getRepository() );
    String fullPath = FilenameUtils.normalize( savePath + "/" + fileName );
    if ( getRepository().fileExists( checkRelativePathSanity( fullPath ) ) ) {
      return buildResponseJson( false, "File " + fileName + " already exists!" );
    }
    return fileStorer.storeFile( fileName, savePath, contents, service.getCurrentUserName() ).toString();
  }
  @POST
  @Path( "/storeIE" )
  @Consumes( "multipart/form-data" )
  @Produces( MimeTypes.HTML )
  public String storeIE( @FormDataParam( "file" ) InputStream uploadedInputStream,
                       @FormDataParam( "file" ) FormDataContentDisposition fileDetail,
                       @FormDataParam( "path" ) String path ) throws JSONException {
    // IE versions < 10 can't handle JSON as a response to a form submit
    // the plugin used to upload files allows for a textarea tag encapsulating the JSON response to be returned instead
    return "<textarea>" + store( uploadedInputStream, fileDetail, path ) + "</textarea>";
  }


  @POST
  @Path( "/listFiles" )
  @Produces( MimeTypes.HTML )
  public String listFiles( @QueryParam( MethodParams.FILEEXTENSIONS ) @DefaultValue( "" ) String extensions,
                           @FormParam( "dir" ) @DefaultValue( "" ) String baseDir )
    throws IOException {
    baseDir = URLDecoder.decode( baseDir, CharsetHelper.getEncoding() );
    IFile[] files = getRepository().listFiles( baseDir );
    List<IFile> allowedFiles = new ArrayList<IFile>( files.length );

    // checks permissions
    /*
     * remarks: ideally the repository must list only the files that the current user is allowed to access?
     */
    for ( IFile file : files ) {
      String relativePath = relativeFilePath( baseDir, file.getName() );
      if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, relativePath ) ) {
        allowedFiles.add( file );
      }
    }

    String[] exts = null;
    if ( !StringUtils.isBlank( extensions ) ) {
      exts = extensions.split( " " );
    }

    IFile[] allowedFilesArray = new IFile[ allowedFiles.size() ];
    return toJQueryFileTree( baseDir, allowedFiles.toArray( allowedFilesArray ), exts );

  }

  @GET
  @Path( "/getFile" )
  @Produces( MediaType.APPLICATION_OCTET_STREAM )
  public Response getFile( @QueryParam( MethodParams.FILENAME ) String fileName ) throws Exception {
    String fullFileName = checkRelativePathSanity( fileName );

    if ( fullFileName == null ) {
      logger.error( "request query parameter fileName must not be null" );
      throw new Exception( "request query parameter fileName must not be null" );
    }

    if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, fullFileName ) ) {
      CfrFile file = getRepository().getFile( fullFileName );
      ResponseBuilder rsp = Response.ok( file.getContent() );
      rsp.type( getMimeType( file.getFileName() ) );
      return buildResponseOk( file, -1, true );
    } else {
      return buildResponseError( Status.UNAUTHORIZED,
        "User \"" + getCfrService().getCurrentUserName() + "\" doesn't have permissions to access the file \""
          + fileName + "\"" );
    }
  }

  @GET
  @Path( "/viewFile" )
  @Produces( "*/*" )
  public Response viewFile( @QueryParam( MethodParams.FILENAME ) String fileName )
    throws Exception {
    String fullFileName = checkRelativePathSanity( fileName );

    if ( fullFileName == null ) {
      logger.error( "request query parameter fileName must not be null" );
      throw new Exception( "request query parameter fileName must not be null" );
    }

    if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, fullFileName ) ) {
      CfrFile file = getRepository().getFile( fullFileName );
      return buildResponseOk( file, -1, false );
    } else {
      return buildResponseError( Status.UNAUTHORIZED,
        "User \"" + getCfrService().getCurrentUserName() + "\" doesn't have permissions to access the file \""
          + fileName + "\"" );
    }
  }

  @GET
  @Path( "/listFilesJson" )
  @Produces( MimeTypes.JSON )
  public String listFilesJson( @QueryParam( MethodParams.DIR ) @DefaultValue( "" ) String dir ) throws JSONException {
    String baseDir = checkRelativePathSanity( dir );
    JSONArray array = getFileListJson( baseDir );
    return array.toString( 2 );
  }

  @GET
  @Path( "/listFilesJSON" )
  @Produces( MimeTypes.JSON )
  public String listFilesJSON( @QueryParam( MethodParams.DIR ) @DefaultValue( "" ) String dir ) throws JSONException {
    return listFilesJson( dir );
  }

  @GET
  @Path( "/listUploads" )
  @Produces( MimeTypes.JSON )
  public String listUploads( @QueryParam( MethodParams.FILENAME ) @DefaultValue( "" ) String filename,
                             @QueryParam( MethodParams.USER ) @DefaultValue( "" ) String user,
                             @QueryParam( MethodParams.STARTDATE ) @DefaultValue( "" ) String startDate,
                             @QueryParam( MethodParams.ENDDATE ) @DefaultValue( "" ) String endDate )
    throws JSONException {
    String path = checkRelativePathSanity( filename );
    return mr.listFiles( path, user, startDate, endDate ).toString();
  }

  @GET
  @Path( "/listUploadsFlat" )
  @Produces( MimeTypes.JSON )
  public String listUploadsFlat( @QueryParam( MethodParams.FILENAME ) @DefaultValue( "" ) String filename,
                                 @QueryParam( MethodParams.USER ) @DefaultValue( "" ) String user,
                                 @QueryParam( MethodParams.STARTDATE ) @DefaultValue( "" ) String startDate,
                                 @QueryParam( MethodParams.ENDDATE ) @DefaultValue( "" ) String endDate )
    throws JSONException {
    String path = checkRelativePathSanity( filename );
    return mr.listFilesFlat( path, user, startDate, endDate ).toString();
  }

  private static String toJQueryFileTree( String baseDir, IFile[] files, String[] extensions ) {
    StringBuilder out = new StringBuilder();
    out.append( "<ul class=\"jqueryFileTree\" style=\"display: none;\">" );

    for ( IFile file : files ) {
      if ( file.isDirectory() ) {
        out.append( "<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "/\">"
            + file.getName() + "</a></li>" );
      }
    }

    for ( IFile file : files ) {
      if ( !file.isDirectory() ) {
        int dotIndex = file.getName().lastIndexOf( '.' );
        String ext = dotIndex > 0 ? file.getName().substring( dotIndex + 1 ) : "";
        boolean accepted = ext.equals( "" );
        if ( !ext.equals( "" ) ) {
          if ( extensions == null || extensions.length == 0 ) {
            accepted = true;
          } else {
            for ( String acceptedExtension : extensions ) {
              if ( ext.equals( acceptedExtension ) ) {
                accepted = true;
                break;
              }
            }
          }
        }
        if ( accepted ) {
          out.append( "<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "\">"
              + file.getName() + "</a></li>" );
        }
      }
    }
    out.append( "</ul>" );
    return out.toString();
  }

  @GET
  @Path( "/setPermissions" )
  @Produces( MimeTypes.JSON )
  public String setPermissions( @QueryParam( MethodParams.PATH ) String path,
                                @QueryParam( MethodParams.ID ) @DefaultValue( "" ) List<String> ids,
                                @QueryParam( MethodParams.PERMISSION ) @DefaultValue( "" ) List<String> permissions,
                                @QueryParam( MethodParams.RECURSIVE ) @DefaultValue( "false" ) Boolean recursive )
    throws JSONException {
    boolean isRoot = false;
    if ( ROOT.equals( path ) ) {
      isRoot = true;
    }
    path = checkRelativePathSanity( path );
    String[] userOrGroupId = ids.toArray( new String[ ids.size() ] );
    String[] _permissions = permissions.toArray( new String[ permissions.size() ] );
    boolean errorSetting = false;
    JSONObject result = new JSONObject();
    if ( path != null && userOrGroupId.length > 0 && _permissions.length > 0 ) {
      List<String> files = new ArrayList<String>();
      if ( recursive ) {
        files = getFileNameTree( path );
      } else {
        files.add( path );
      }
      // build valid permissions set
      Set<FilePermissionEnum> validPermissions = new TreeSet<FilePermissionEnum>();
      for ( String permission : _permissions ) {
        FilePermissionEnum perm = FilePermissionEnum.resolve( permission );
        if ( perm != null ) {
          validPermissions.add( perm );
        }
      }
      if ( isRoot ) {
        files.add( ROOT );
      }
      JSONArray permissionAddResultArray = new JSONArray();
      for ( String file : files ) {
        CfrFile f = getRepository().getFile( file );
        if ( getRepository().getFile( path ).isDirectory() && f.isFile() ) {
          continue;
        }
        for ( String id : userOrGroupId ) {
          boolean storeResult = storeFile( file, id, validPermissions );
          if ( storeResult ) {
            permissionAddResultArray.put( new JSONObject()
                .put( "status", String.format( "Added permission for path %s and user/role %s", file, id ) ) );
          } else {
            if ( isUserAdmin() ) {
              permissionAddResultArray.put( new JSONObject()
                  .put( "status", String.format( "Failed to add permission for path %s and user/role %s", file,
                    id ) ) );
            } else {
              errorSetting = true;
            }
          }
        }
      }
      result.put( "status", "Operation finished. Check statusArray for details." );
      if ( errorSetting ) {
        permissionAddResultArray.put( new JSONObject().put( "status", "Some permissions could not be set" ) );
      }
      result.put( "statusArray", permissionAddResultArray );
    } else {
      result.put( "status", "Path or user group parameters not found" );
    }

    return result.toString( 2 );
  }

  @GET
  @Path( "/deletePermissions" )
  @Produces( MimeTypes.JSON )
  public String deletePermissions( @QueryParam( MethodParams.PATH ) @DefaultValue( "" ) String path,
                                   @QueryParam( MethodParams.ID ) @DefaultValue( "" ) List<String> ids,
                                   @QueryParam( MethodParams.RECURSIVE ) @DefaultValue( "false" ) Boolean recursive )
    throws JSONException, IOException {
    boolean isRoot = false;
    if ( ROOT.equals( path ) ) {
      isRoot = true;
    }
    path = checkRelativePathSanity( path );
    String[] userOrGroupId = ids.toArray( new String[ ids.size() ] );
    JSONObject result = new JSONObject();
    boolean admin = isUserAdmin();
    boolean errorDeleting = false;

    if ( path != null || ( userOrGroupId != null && userOrGroupId.length > 0 ) ) {
      List<String> files = new ArrayList<String>();
      if ( recursive ) {
        files = getFileNameTree( path );
      } else {
        files.add( path );
      }
      if ( isRoot ) {
        files.add( ROOT );
      }
      JSONArray permissionDeleteResultArray = new JSONArray();
      if ( userOrGroupId == null || userOrGroupId.length == 0 ) {
        for ( String f : files ) {
          if ( deletePermissions( f, null ) ) {
            permissionDeleteResultArray.put( new JSONObject().put( "status", "Permissions for " + f + " deleted" ) );
          } else {
            if ( admin ) {
              permissionDeleteResultArray
                .put( new JSONObject().put( "status", "Error deleting permissions for " + f ) );
            } else {
              errorDeleting = true;
            }
          }
        }
        result.put( "status", "Multiple permission deletion. Check Status array" );
        if ( errorDeleting ) {
          permissionDeleteResultArray.put( new JSONObject().put( "status", "Some permissions could not be removed" ) );
        }
        result.put( "statusArray", permissionDeleteResultArray );
      } else {
        for ( String id : userOrGroupId ) {
          for ( String f : files ) {
            JSONObject individualResult = new JSONObject();
            boolean deleteResult = deletePermissions( f, id );
            if ( deleteResult ) {
              individualResult.put( "status", String.format( "Permission for %s and path %s deleted.", id, f ) );
            } else {
              individualResult
                .put( "status", String.format( "Failed to delete permission for %s and path %s.", id, f ) );
            }

            permissionDeleteResultArray.put( individualResult );
          }
        }
        result.put( "status", "Multiple permission deletion. Check Status array" );
        result.put( "statusArray", permissionDeleteResultArray );
      }
    } else {
      result.put( "status", "Required arguments user/role and path not found" );
    }

    return result.toString( 2 );
  }

  @GET
  @Path( "/getPermissions" )
  @Produces( MimeTypes.JSON )
  public String getPermissions( @QueryParam( MethodParams.PATH ) String path, @QueryParam( MethodParams.ID ) String id )
    throws JSONException {
    path = checkRelativePathSanity( path );
    if ( path != null || id != null ) {
      JSONArray permissions = mr.getPermissions( path, id, FilePermissionMetadata.DEFAULT_PERMISSIONS );
      return permissions.toString( 0 );
    }
    return "{\n  \"status\": \"error\",\n  \"result\": \"false\",\n  \"message\": \"Must supply a path and/or an "
      + "id\"\n}";
  }

  @GET
  @Path( "/resetRepository" )
  public String resetRepository() {

    if ( !isUserAdmin() ) {
      logger.warn( "Reset repository called by a non admin user. Aborting" );
      return "User has no access to this endpoint";
    }


    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().initializeClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().initializeClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );

    IFileRepository repo = new CfrService().getRepository();
    for ( IFile file : repo.listFiles( "" ) ) {
      repo.deleteFile( file.getFullPath() );
    }

    return "Repository Reset complete";


  }

  @GET
  @Path( "/checkVersion" )
  @Produces( MimeTypes.JSON )
  public String checkVersion() throws JSONException {
    return getVersionChecker().checkVersion().toJSON().toString();
  }

  @GET
  @Path( "/getVersion" )
  public void getVersion( @Context HttpServletResponse response ) throws IOException, JSONException {
    writeMessage( getVersionChecker().getVersion(), response.getOutputStream() );
  }

  @GET
  @Path( "/about" )
  public void about( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws Exception {
    renderInCde( response, getRenderRequestParameters( "cfrAbout.wcdf", request ) );
  }

  @GET
  @Path( "/browser" )
  public void browser( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws Exception {
    renderInCde( response, getRenderRequestParameters( "cfrBrowser.wcdf", request ) );
  }

  public VersionChecker getVersionChecker() {

    return new VersionChecker( new CfrPluginSettings() ) {

      @Override
      protected String getVersionCheckUrl( VersionChecker.Branch branch ) {
        switch ( branch ) {
          case TRUNK:
            return "http://ci.pentaho.com/job/pentaho-cfr/lastSuccessfulBuild/artifact/cfr-pentaho5/dist/marketplace"
              + ".xml";
          //          case STABLE:
          //            return "http://ci.analytical-labs.com/job/Webdetails-CFR-Release/"
          //              + "lastSuccessfulBuild/artifact/dist/marketplace.xml";
          default:
            return null;
        }

      }

    };
  }

  private void renderInCde( HttpServletResponse response, Map<String, Object> params ) throws Exception {
    response.setContentType( MimeTypes.HTML );
    InterPluginBroker.run( params, response.getOutputStream() );
  }

  private Map<String, Object> getRenderRequestParameters( String dashboardName, HttpServletRequest request ) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "solution", "system" );
    params.put( "path", UI_PATH );
    params.put( "file", dashboardName );
    params.put( "bypassCache", "true" );
    params.put( "absolute", "false" );
    params.put( "inferScheme", "false" );

    // add request parameters
    Enumeration<String> originalParams = request.getParameterNames();
    // Iterate and put the values there
    while ( originalParams.hasMoreElements() ) {
      String originalParam = originalParams.nextElement();
      params.put( originalParam, request.getParameter( originalParam ) );
    }

    return params;
  }

  private Response buildResponseOk( CfrFile file, int cacheDuration, boolean download ) {
    ResponseBuilder responseBuilder = Response.ok( file.getContent() );

    responseBuilder.header( "Content-Type", getMimeType( file.getName() ) );

    if ( download ) {
      responseBuilder.header( "Content-Disposition", "attachment; filename=" + file.getName() );
    } // Cache?

    if ( cacheDuration > 0 ) {
      responseBuilder.header( "Cache-Control", "max-age=" + cacheDuration );
    } else {
      responseBuilder.header( "Cache-Control", "max-age=0, no-store" );
    }
    return responseBuilder.build();

  }

  private Response buildResponseError( Status status, String message ) {
    ResponseBuilder responseBuilder = Response.status( status );
    responseBuilder.entity( message );
    responseBuilder.type( "text/plain" );
    return responseBuilder.build();
  }

  private String getMimeType( String fileName ) {
    return MimeTypes.getMimeType( fileName );
  }

  private void writeMessage( String message, OutputStream out ) throws IOException {
    IOUtils.write( message, out );
    out.flush();
  }

  protected List<String> getFileNameTree( String path ) {
    List<String> files = new ArrayList<String>();
    if ( !StringUtils.isEmpty( path ) ) {
      files.add( path );
    }

    files.addAll( buildFileNameTree( path, getFileNames( getRepository().listFiles( path ) ) ) );
    List<String> treatedFileNames = new ArrayList<String>();
    for ( String file : files ) {
      if ( file.startsWith( "/" ) ) {
        treatedFileNames.add( file.replaceFirst( "/", "" ) );
      } else {
        treatedFileNames.add( file );
      }
    }
    return treatedFileNames;
  }

  private List<String> buildFileNameTree( String basePath, List<String> children ) {
    List<String> result = new ArrayList<String>();
    for ( String child : children ) {
      String newEntry = basePath + "/" + child;
      result.add( newEntry );
      result.addAll( buildFileNameTree( newEntry, getFileNames( getRepository().listFiles( newEntry ) ) ) );
    }
    return result;
  }

  private List<String> getFileNames( IFile[] files ) {
    List<String> names = new ArrayList<String>();
    for ( IFile file : files ) {
      names.add( file.getName() );
    }
    return names;
  }

  private JSONArray getFileListJson( String baseDir ) throws JSONException {
    IFile[] files = getRepository().listFiles( baseDir );
    JSONArray arr = new JSONArray();
    if ( files != null ) {
      for ( IFile file : files ) {
        if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, relativeFilePath( baseDir, file.getName() ) ) ) {
          JSONObject obj = new JSONObject();
          obj.put( "fileName", file.getName() );
          obj.put( "isDirectory", file.isDirectory() );
          obj.put( "path", baseDir );
          arr.put( obj );
        }
      }
    }
    return arr;
  }

  protected CfrService getCfrService() {
    return new CfrService();
  }

  protected boolean storeFile( String file, String id, Set<FilePermissionEnum> validPermissions ) {
    return FileStorer.storeFilePermissions( new FilePermissionMetadata( file, id, validPermissions ) );
  }

  protected IFileRepository getRepository() {
    return service.getRepository();
  }

  private boolean deletePermissions( String path, String id ) {
    return FileStorer.deletePermissions( path, id );
  }

  protected boolean isUserAdmin() {
    return this.service.isCurrentUserAdmin();
  }

  protected String buildResponseJson( boolean status, String message ) throws JSONException {
    JSONObject result = new JSONObject();
    result.put( "result", status );
    result.put( "message", message );
    return result.toString();
  }

  private class MethodParams {
    public static final String PATH = "path";
    public static final String FILENAME = "fileName";
    public static final String FILEEXTENSIONS = "fileExtensions";
    public static final String DIR = "dir";
    public static final String USER = "user";
    public static final String STARTDATE = "startDate";
    public static final String ENDDATE = "endDate";
    public static final String ID = "id";
    public static final String PERMISSION = "permission";
    public static final String RECURSIVE = "recursive";
  }

}
