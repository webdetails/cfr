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

package pt.webdetails.cfr;

import junit.framework.Assert;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CfrApiTest {

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
    List<String> ids = new ArrayList<String>();
    ids.add( "Authenticated" );
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    permissions.add( "write" );
    boolean recursive = true;
    List<String> files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure ) );
    files.addAll( Arrays.asList( dirStructure ) );
    cfrApi.setFileNames( files );

    createCfrServiceMock();
    cfrApi.setCfrService( cfrServiceMock );

    // setPermissions on a dir, with recursive = true
    String result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnDirRecur + bot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = true
    path = fileStructure[ 0 ];
    files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure[ 0 ] ) );
    cfrApi.setFileNames( files );
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    // setPermissions on a dir, with recursive = false
    path = dirStructure[ 0 ];
    recursive = false;
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnDir + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = false
    path = fileStructure[ 0 ];
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
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
    List<String> ids = new ArrayList<String>();
    ids.add( "Authenticated" );
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    permissions.add( "write" );
    boolean recursive = true;
    List<String> files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure ) );
    files.addAll( Arrays.asList( dirStructure ) );
    cfrApi.setFileNames( files );

    createCfrServiceMock();
    cfrApi.setCfrService( cfrServiceMock );

    // setPermissions on a dir, with recursive = true
    String result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnDirRecur + bot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = true
    path = fileStructure[ 0 ];
    files = new ArrayList<String>();
    files.addAll( Arrays.asList( fileStructure[ 0 ] ) );
    cfrApi.setFileNames( files );
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    // setPermissions on a dir, with recursive = false
    path = dirStructure[ 0 ];
    recursive = false;
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnDir + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file, with recursive = false
    path = fileStructure[ 0 ];
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + setOnFile + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a file not owned by non-admin user
    path = fileStructure[ 1 ];
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + permissionsNotSet + singleBot, result.replaceAll( "\n", "" ) );

    //set permissions on a dir not owned by non-admin user
    path = dirStructure[ 1 ];
    result = cfrApi.setPermissions( path, ids, permissions, recursive );
    Assert.assertEquals( top + permissionsNotSet + singleBot, result.replaceAll( "\n", "" ) );

  }

  @Test
  public void testListFiles() throws IOException {
    String correctResult = "<ul class=\"jqueryFileTree\" style=\"display: none;\"><li class=\"file ext_txt\"><a "
        + "href=\"#\" rel=\"/a folder/a file.txt\">a file.txt</a></li></ul>";
    CfrApiForTests cfrApi = new CfrApiForTests();
    createCfrServiceMock();
    cfrApi.setCfrService( cfrServiceMock );
    cfrApi.reloadMetadataReader();
    HttpServletRequest requestMock = Mockito.mock( HttpServletRequest.class );
    Mockito.when( requestMock.getParameter( "fileExtensions" ) ).thenReturn( "" );

    String result = cfrApi.listFiles( "", "/a%20folder/" );
    Assert.assertEquals( result, correctResult );

  }

  private static void createCfrServiceMock() throws IOException {
    //setting the CfrServiceMock
    if ( cfrServiceMock == null ) {
      cfrServiceMock = Mockito.mock( CfrService.class );
      Mockito.when( cfrServiceMock.isCurrentUserAdmin() ).thenReturn( true );
    }
  }


}
