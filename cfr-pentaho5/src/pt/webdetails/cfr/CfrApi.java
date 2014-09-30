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

import java.io.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import pt.webdetails.cpf.InvalidOperationException;
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
  public void createFolder( @Context HttpServletRequest request,
                            @Context HttpServletResponse response ) throws Exception {
    String path = checkRelativePathSanity( getParameter( "path", request ) );

    if ( path == null || StringUtils.isBlank( path ) ) {
      throw new Exception( "path is null or empty" );
    }

    boolean createResult = getRepository().createFolder( path );
    writeMessage( new JSONObject().put( "result", createResult ).toString(), response.getOutputStream() );

  }

  @POST
  @Path( "/remove" )
  public void remove( @Context HttpServletRequest request, @Context HttpServletResponse response ) throws Exception {
    String fullFileName =
      checkRelativePathSanity( getParameter( "fileName", request ) );

    if ( fullFileName == null || StringUtils.isBlank( fullFileName ) ) {
      throw new Exception( "fileName is null or empty" );
    }

    boolean removeResult = getRepository().deleteFile( fullFileName );
    boolean result = false;
    if ( removeResult ) {
      FileStorer.deletePermissions( fullFileName, null );
      result = FileStorer.removeFile( fullFileName, null );
    }

    writeMessage( new JSONObject().put( "result", result ).toString(), response.getOutputStream() );
  }

  @POST
  @Path( "/store" )
  @Consumes( "multipart/form-data" )
  public void store( @FormDataParam( "file" ) InputStream uploadedInputStream,
                     @FormDataParam( "file" ) FormDataContentDisposition fileDetail,
                     @FormDataParam( "path" ) String path,
                     @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException, InvalidOperationException, Exception {


    String fileName = fileDetail.getFileName(), savePath = path;
    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
    IOUtils.copy( uploadedInputStream, oStream );
    oStream.flush();
    byte[] contents = oStream.toByteArray();
    oStream.close();

    if ( fileName == null ) {
      logger.error( "parameter fileName must not be null" );
      throw new Exception( "parameter fileName must not be null" );
    }
    if ( savePath == null ) {
      logger.error( "parameter path must not be null" );
      throw new Exception( "parameter path must not be null" );
    }
    if ( contents == null ) {
      logger.error( "File content must not be null" );
      throw new Exception( "File content must not be null" );
    }

    FileStorer fileStorer = new FileStorer( getRepository() );

    boolean stored =
      fileStorer
        .storeFile( checkRelativePathSanity( fileName ), checkRelativePathSanity( savePath ), contents,
          service.getCurrentUserName() );

    JSONObject result = new JSONObject().put( "result", stored );
    writeMessage( result.toString(), response.getOutputStream() );
  }

  @POST
  @Path( "/listFiles" )
  public void listFiles( @FormParam( "dir" ) @DefaultValue( "" ) String baseDir,
                         @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException {
    baseDir = URLDecoder.decode( baseDir, CharsetHelper.getEncoding() );
    IFile[] files = getRepository().listFiles( baseDir );
    List<IFile> allowedFiles = new ArrayList<IFile>( files.length );
    String extensions = getParameter( "fileExtensions", request );

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
    response.setContentType( MimeTypes.HTML );
    writeMessage( toJQueryFileTree( baseDir, allowedFiles.toArray( allowedFilesArray ), exts ),
      response.getOutputStream() );

  }

  @GET
  @Path( "/getFile" )
  public void getFile( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException, JSONException, Exception {
    String fullFileName = checkRelativePathSanity( getParameter( "fileName", null, request ) );

    if ( fullFileName == null ) {
      logger.error( "request query parameter fileName must not be null" );
      throw new Exception( "request query parameter fileName must not be null" );
    }

    if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, fullFileName ) ) {
      CfrFile file = getRepository().getFile( fullFileName );


      setResponseHeaders( getMimeType( file.getFileName() ), -1, URLEncoder.encode( file.getFileName(),
        CharsetHelper.getEncoding() ), response );
      ByteArrayInputStream bais = new ByteArrayInputStream( file.getContent() );
      IOUtils.copy( bais, response.getOutputStream() );
      response.getOutputStream().flush();
      IOUtils.closeQuietly( bais );
    } else {
      response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "you don't have permissions to access the file" );
    }
  }

  @GET
  @Path( "/viewFile" )
  public void viewFile( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws Exception {
    String fullFileName =
      checkRelativePathSanity( getParameter( "fileName", null, request ) );

    if ( fullFileName == null ) {
      logger.error( "request query parameter fileName must not be null" );
      throw new Exception( "request query parameter fileName must not be null" );
    }

    if ( mr.isCurrentUserAllowed( FilePermissionEnum.READ, fullFileName ) ) {
      CfrFile file = getRepository().getFile( fullFileName );

      setResponseHeaders( getMimeType( file.getFileName() ), -1, null, response );
      ByteArrayInputStream bais = new ByteArrayInputStream( file.getContent() );
      IOUtils.copy( bais, response.getOutputStream() );
      response.getOutputStream().flush();
      IOUtils.closeQuietly( bais );
    } else {
      response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "you don't have permissions to access the file" );
    }
  }

  @GET
  @Path( "/listFilesJson" )
  public void listFilesJson( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException, JSONException {
    String baseDir = checkRelativePathSanity( getParameter( "dir", request ) );

    JSONArray array = getFileListJson( baseDir );
    writeMessage( array.toString( 2 ), response.getOutputStream() );
  }

  @GET
  @Path( "/listFilesJSON" )
  public void listFilesJSON( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException, JSONException {
    String baseDir = checkRelativePathSanity( getParameter( "dir", request ) );

    JSONArray array = getFileListJson( baseDir );
    writeMessage( array.toString( 2 ), response.getOutputStream() );
  }

  @GET
  @Path( "/listUploads" )
  public void listUploads( @Context HttpServletRequest request,
                           @Context HttpServletResponse response )
    throws IOException, JSONException {
    String path = checkRelativePathSanity( getParameter( "fileName", request ) );
    writeMessage( mr.listFiles( path, getParameter( "user", request ), getParameter( "startDate", request ),
      getParameter( "endDate", request ) ).toString(), response.getOutputStream() );
  }

  @GET
  @Path( "/listUploadsFlat" )
  public void listUploadsFlat( @Context HttpServletRequest request,
                               @Context HttpServletResponse response )
    throws IOException, JSONException {
    String path = checkRelativePathSanity( getParameter( "fileName", request ) );

    writeMessage( mr.listFilesFlat( path, getParameter( "user", request ), getParameter( "startDate", request ),
      getParameter( "endDate", request ) ).toString(), response.getOutputStream() );
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
  public void setPermissions( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws JSONException, IOException {
    String path = checkRelativePathSanity( getParameter( pathParameterPath, null, request ) );
    String[] userOrGroupId = getStringArrayParameter( pathParameterGroupOrUserId, request );
    String[] _permissions = getStringArrayParameter( pathParameterPermission, request );
    boolean recursive = Boolean.parseBoolean( getParameter( pathParameterRecursive, "false", request ) );
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
      JSONArray permissionAddResultArray = new JSONArray();
      for ( String file : files ) {
        for ( String id : userOrGroupId ) {
          JSONObject individualResult = new JSONObject();
          boolean storeResult = storeFile( file, id, validPermissions );
          if ( storeResult ) {
            individualResult
              .put( "status", String.format( "Added permission for path %s and user/role %s", file, id ) );
          } else {
            individualResult
              .put( "status", String.format( "Failed to add permission for path %s and user/role %s", file,
                id ) );
          }
          permissionAddResultArray.put( individualResult );
        }
      }
      result.put( "status", "Operation finished. Check statusArray for details." );
      result.put( "statusArray", permissionAddResultArray );
    } else {
      result.put( "status", "Path or user group parameters not found" );
    }

    writeMessage( result.toString( 2 ), response.getOutputStream() );
  }

  @GET
  @Path( "/deletePermissions" )
  public void deletePermissions( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws JSONException, IOException {
    String path = checkRelativePathSanity( getParameter( pathParameterPath, null, request ) );
    String[] userOrGroupId = getStringArrayParameter( pathParameterGroupOrUserId, request );

    JSONObject result = new JSONObject();

    if ( path != null || ( userOrGroupId != null && userOrGroupId.length > 0 ) ) {
      if ( userOrGroupId == null || userOrGroupId.length == 0 ) {
        if ( FileStorer.deletePermissions( path, null ) ) {
          result.put( "status", "Permissions deleted" );
        } else {
          result.put( "status", "Error deleting permissions" );
        }
      } else {
        JSONArray permissionDeleteResultArray = new JSONArray();
        for ( String id : userOrGroupId ) {
          JSONObject individualResult = new JSONObject();
          boolean deleteResult = FileStorer.deletePermissions( path, id );
          if ( deleteResult ) {
            individualResult.put( "status", String.format( "Permission for %s and path %s deleted.", id, path ) );
          } else {
            individualResult
              .put( "status", String.format( "Failed to delete permission for %s and path %s.", id, path ) );
          }

          permissionDeleteResultArray.put( individualResult );
        }
        result.put( "status", "Multiple permission deletion. Check Status array" );
        result.put( "statusArray", permissionDeleteResultArray );
      }
    } else {
      result.put( "status", "Required arguments user/role and path not found" );
    }

    writeMessage( result.toString( 2 ), response.getOutputStream() );
  }

  @GET
  @Path( "/getPermissions" )
  public void getPermissions( @Context HttpServletRequest request, @Context HttpServletResponse response )
    throws IOException, JSONException {
    String path = checkRelativePathSanity( getParameter( pathParameterPath, null, request ) );
    String id = getParameter( pathParameterGroupOrUserId, null, request );
    if ( path != null || id != null ) {
      JSONArray permissions = mr.getPermissions( path, id, FilePermissionMetadata.DEFAULT_PERMISSIONS );
      writeMessage( permissions.toString( 0 ), response.getOutputStream() );
    }
  }

  @GET
  @Path( "/resetRepository" )
  public String resetRepository() {

    if ( !this.service.isCurrentUserAdmin() ) {
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

  private static final String pathParameterGroupOrUserId = "id";

  private static final String pathParameterPath = "path";

  private static final String pathParameterPermission = "permission";

  private static final String pathParameterRecursive = "recursive";

  @GET
  @Path( "/checkVersion" )
  public void checkVersion( @Context HttpServletResponse response ) throws IOException, JSONException {
    writeMessage( getVersionChecker().checkVersion().toJSON().toString(), response.getOutputStream() );
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
        switch( branch ) {
          case TRUNK:
            return "http://ci.pentaho.com/job/pentaho-cfr/lastSuccessfulBuild/artifact/cfr-pentaho5/dist/marketplace" +
              ".xml";
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

  private String getParameter( String param, HttpServletRequest request ) {
    return getParameter( param, "", request );
  }

  private String getParameter( String param, String dflt, HttpServletRequest request ) {
    return request.getParameter( param ) != null ? request.getParameter( param ) : dflt;
  }

  private String[] getStringArrayParameter( String param, HttpServletRequest request ) {
    String[] result = (String[]) request.getParameterMap().get( param );
    return result;
  }

  private void setResponseHeaders( String mimeType, int cacheDuration, String attachmentName,
                                   HttpServletResponse response ) {

    if ( response == null ) {
      logger.warn( "Parameter 'httpresponse' not found!" );
      return;
    }

    response.setHeader( "Content-Type", mimeType );

    if ( attachmentName != null ) {
      response.setHeader( "content-disposition", "attachment; filename=" + attachmentName );
    } // Cache?

    if ( cacheDuration > 0 ) {
      response.setHeader( "Cache-Control", "max-age=" + cacheDuration );
    } else {
      response.setHeader( "Cache-Control", "max-age=0, no-store" );
    }

  }

  private String getMimeType( String fileName ) {
    String[] fileNameSplit = StringUtils.split( fileName, '.' );
    return MimeTypes.getMimeType( fileNameSplit[ fileNameSplit.length - 1 ].toUpperCase() );
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

}
