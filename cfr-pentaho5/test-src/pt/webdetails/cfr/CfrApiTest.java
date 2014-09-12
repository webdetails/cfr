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


  @BeforeClass
  public static void setup() {
    byte[] content = new byte[ 100 ];
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    fileRep.createFolder( "a folder" );
    fileRep.storeFile( content, "a file.txt", "a folder" );

  }

  @AfterClass
  public static void onTestFinish() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    fileRep.deleteFile( "/a folder/a file.txt" );
    fileRep.deleteFile( "/a folder" );
  }

  @Test
  public void testRecursivelySetPermissions() throws IOException, JSONException {
    CfrApiForTests cfrApi = new CfrApiForTests();
    String top = "{\n"
      + "  \"status\": \"Operation finished. Check statusArray for details.\",\n"
      + "  \"statusArray\": [\n";
    String bot = "  ]\n}";
    String allFiles = "    {\"status\": \"Added permission for path file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path file2 and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/dirInsideDir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/dirInsideDir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/otherDir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/otherDir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/file2 and user/role Authenticated\"}\n";
    String dirFiles = "    {\"status\": \"Added permission for path dir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/dirInsideDir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/dirInsideDir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/otherDir and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/otherDir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/file and user/role Authenticated\"},\n"
      + "    {\"status\": \"Added permission for path dir/file2 and user/role Authenticated\"}\n";
    String dirOtherDirFiles =
      "    {\"status\": \"Added permission for path dir/otherDir and user/role Authenticated\"},\n"
        + "    {\"status\": \"Added permission for path dir/otherDir/file and user/role Authenticated\"}\n";
    String path = "/";
    String[] id = { "Authenticated" };
    String[] permission = { "read", "write" };
    String recursive = "true";
    String[] filesArr =
      { "file", "file2" };
    String[] filesDir = { "dir", "dir/dirInsideDir", "dir/dirInsideDir/file", "dir/otherDir", "dir/otherDir/file",
      "dir/file", "dir/file2" };
    String[] filesDirOtherDir = { "dir/otherDir", "dir/otherDir/file" };
    List<String> files = new ArrayList<String>();
    files.addAll( Arrays.asList( filesArr ) );
    files.addAll( Arrays.asList( filesDir ) );
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

    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( result, top + allFiles + bot );

    files = new ArrayList<String>();
    files.addAll( Arrays.asList( filesDir ) );
    cfrApi.setFileNames( files );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( result, top + dirFiles + bot );

    files = new ArrayList<String>();
    files.addAll( Arrays.asList( filesDirOtherDir ) );
    cfrApi.setFileNames( files );
    cfrApi.setPermissions( requestMock, responseMock );
    Assert.assertEquals( result, top + dirOtherDirFiles + bot );

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
