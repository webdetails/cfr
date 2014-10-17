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

import junit.framework.Assert;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CfrApiTest {

  static String result = "";
  static ServletOutputStream outputStreamMock;
  static HttpServletResponse responseMock;
  static CfrService cfrServiceMock;
  static final String[] dirStructure = { "dir1", "dir1/dir", "dir2", "a folder" };
  static final String[] fileStructure = { "dir1/file1", "file2", "file3", "dir1/dir/file4", "a folder/a file.txt" };


  @BeforeClass
  public static void setup() {
    byte[] content = new byte[ 100 ];
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    for ( String dir : dirStructure ) {
      fileRep.createFolder( dir );
    }
    for ( String file : fileStructure ) {
      fileRep.storeFile( content, file, "" );
    }
  }

  @AfterClass
  public static void onTestFinish() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    for ( String file : fileStructure ) {
      fileRep.deleteFile( file );
    }
    for ( int i = dirStructure.length - 1; i >= 0; i-- ) {
      fileRep.deleteFile( dirStructure[ i ] );
    }
  }


  @Test
  public void testSetPermissionsAdmin() throws IOException, JSONException {
    CfrApiForTests cfrApi = new CfrApiForTests();
    cfrApi.setIsAdmin( true );
    String top = "{"
      + "  \"status\": \"Operation finished. Check statusArray for details.\","
      + "  \"statusArray\": [";
    String bot = "  ]}";
    String singleBot = "]}";
    String setOnDirRecur;
    StringBuilder sbAllDir = new StringBuilder();
    for ( String dir : dirStructure ) {
      sbAllDir.append( "    {\"status\": \"Added permission for path " + dir + " and user/role Authenticated\"}," );
    }
    setOnDirRecur = sbAllDir.toString();
    setOnDirRecur =
      sbAllDir.replace( setOnDirRecur.lastIndexOf( "," ), setOnDirRecur.lastIndexOf( "," ) + 1, "" ).toString();

    String setOnDir =
      "{\"status\": \"Added permission for path " + dirStructure[ 0 ] + " and user/role Authenticated\"}";
    String setOnFile =
      "{\"status\": \"Added permission for path " + fileStructure[ 0 ] + " and user/role Authenticated\"}";
    String path = "/";
    String[] id = { "Authenticated" };
    String[] permission = { "read", "write" };
    String recursive = "true";
    List<String> files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure ) );
    files.addAll( Arrays.asList( dirStructure ) );
    cfrApi.setFileNames( files );

    createCfrServiceMock();
    cfrApi.setCfrService( cfrServiceMock );

    // setting up Map mock
    Map mapMock = Mockito.mock( Map.class );
    Mockito.when( mapMock.get( "id" ) ).thenReturn( id );
    Mockito.when( mapMock.get( "permission" ) ).thenReturn( permission );

    createOutputStreamMock();

    // setting up request mock
    HttpServletRequest requestMock = Mockito.mock( HttpServletRequest.class );
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameterMap() ).thenReturn( mapMock );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );

    createResponseMock();
    // setPermissions on a dir, with recursive = true
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnDirRecur + bot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = true
    path = fileStructure[ 0 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure[ 0 ] ) );
    cfrApi.setFileNames( files );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    // setPermissions on a dir, with recursive = false
    path = dirStructure[ 0 ];
    recursive = "false";
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnDir + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = false
    path = fileStructure[ 0 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

  }

  @Test
  public void testSetPermissionsNonAdmin() throws IOException, JSONException {
    List<String> nonAdminUserFiles = new ArrayList<String>();
    boolean unsetPermissions = false;
    nonAdminUserFiles.add( fileStructure[ 0 ] );
    nonAdminUserFiles.add( dirStructure[ 0 ] );
    CfrApiForTests cfrApi = new CfrApiForTests();
    cfrApi.setIsAdmin( false );
    cfrApi.setNonAdminUserFiles( nonAdminUserFiles );
    String top = "{"
      + "  \"status\": \"Operation finished. Check statusArray for details.\","
      + "  \"statusArray\": [";
    String bot = "  ]}";
    String singleBot = "]}";
    String setOnDirRecur;
    StringBuilder sbAllDir = new StringBuilder();
    for ( String dir : dirStructure ) {
      if ( nonAdminUserFiles.contains( dir ) ) {
        sbAllDir.append( "    {\"status\": \"Added permission for path " + dir + " and user/role Authenticated\"}," );
      } else {
        unsetPermissions = true;
      }
    }
    setOnDirRecur = sbAllDir.toString();
    if ( !unsetPermissions ) {
      setOnDirRecur =
        sbAllDir.replace( setOnDirRecur.lastIndexOf( "," ), setOnDirRecur.lastIndexOf( "," ) + 1, "" ).toString();
    } else {
      sbAllDir.append( "    {\"status\": \"Some permissions could not be set\"}" );
      setOnDirRecur = sbAllDir.toString();
    }

    String setOnDir =
      "{\"status\": \"Added permission for path " + dirStructure[ 0 ] + " and user/role Authenticated\"}";
    String setOnFile =
      "{\"status\": \"Added permission for path " + fileStructure[ 0 ] + " and user/role Authenticated\"}";
    String permissionsNotSet = "{\"status\": \"Some permissions could not be set\"}";
    String path = "/";
    String[] id = { "Authenticated" };
    String[] permission = { "read", "write" };
    String recursive = "true";
    List<String> files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure ) );
    files.addAll( Arrays.asList( dirStructure ) );
    cfrApi.setFileNames( files );

    createCfrServiceMock();
    cfrApi.setCfrService( cfrServiceMock );

    // setting up Map mock
    Map mapMock = Mockito.mock( Map.class );
    Mockito.when( mapMock.get( "id" ) ).thenReturn( id );
    Mockito.when( mapMock.get( "permission" ) ).thenReturn( permission );

    createOutputStreamMock();

    // setting up request mock
    HttpServletRequest requestMock = Mockito.mock( HttpServletRequest.class );
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameterMap() ).thenReturn( mapMock );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );

    createResponseMock();
    // setPermissions on a dir, with recursive = true
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnDirRecur + bot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = true
    path = fileStructure[ 0 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure[ 0 ] ) );
    cfrApi.setFileNames( files );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    // setPermissions on a dir, with recursive = false
    path = dirStructure[ 0 ];
    recursive = "false";
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnDir + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = false
    path = fileStructure[ 0 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file not owned by non-admin user
    path = fileStructure[ 1 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + permissionsNotSet + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a dir not owned by non-admin user
    path = dirStructure[ 1 ];
    Mockito.when( requestMock.getParameter( "path" ) ).thenReturn( path );
    Mockito.when( requestMock.getParameter( "recursive" ) ).thenReturn( recursive );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( top + permissionsNotSet + singleBot, result.replaceAll( "\n", "" ) );

  }

  @Test
  public void testListFiles() throws IOException {
    String correctResult = "<ul class=\"jqueryFileTree\" style=\"display: none;\"><li class=\"file ext_txt\"><a " +
      "href=\"#\" rel=\"/a folder/a file.txt\">a file.txt</a></li></ul>";
    CfrApiForTests cfrApi = new CfrApiForTests();
    createCfrServiceMock();
    createOutputStreamMock();
    createResponseMock();
    cfrApi.setCfrService( cfrServiceMock );
    cfrApi.reloadMetadataReader();
    HttpServletRequest requestMock = Mockito.mock( HttpServletRequest.class );
    Mockito.when( requestMock.getParameter( "fileExtensions" ) ).thenReturn( "" );

    cfrApi.listFiles( "/a%20folder/", requestMock, responseMock );
    Assert.assertEquals( result, correctResult );

  }

  private static void createOutputStreamMock() throws IOException {
    // setting up outputStream mock
    if ( outputStreamMock == null ) {
      outputStreamMock = Mockito.mock( ServletOutputStream.class );
      Mockito.doAnswer( new Answer() {
        @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
          Object[] args = invocation.getArguments();
          byte[] b = (byte[]) args[ 0 ];
          result = new String( b );
          return null;
        }
      } ).when( outputStreamMock ).write( Mockito.any( byte[].class ) );
    }
  }

  private static void createResponseMock() throws IOException {
    //setting up response mock
    if ( responseMock == null ) {
      responseMock = Mockito.mock( HttpServletResponse.class );
      Mockito.when( responseMock.getOutputStream() ).thenReturn( outputStreamMock );
    }
  }

  private static void createCfrServiceMock() throws IOException {
    //setting the CfrServiceMock
    if ( cfrServiceMock == null ) {
      cfrServiceMock = Mockito.mock( CfrService.class );
      Mockito.when( cfrServiceMock.isCurrentUserAdmin() ).thenReturn( true );
    }
  }


}
