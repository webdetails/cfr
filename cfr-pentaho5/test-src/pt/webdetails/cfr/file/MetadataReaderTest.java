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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cfr.CfrEnvironmentForTests;
import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.persistence.PersistenceEngineForTests;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.messaging.JsonSerializable;

import com.google.common.collect.ImmutableList;

public class MetadataReaderTest {

  private static void createFile( String owner, String file, String date ) throws JSONException {
    JSONObject obj = new JSONObject();

    obj.put( "user", owner );
    obj.put( "file", file );
    obj.put( "uploadDate", date );
    PersistenceEngineForTests.getInstance().store( null, FileStorer.FILE_METADATA_STORE_CLASS, obj );

  }

  private static final String USER_1 = "user1";

  private static final List<String> USER_1_ROLES = ImmutableList.of( "admin", "user" );

  private static final String USER_2 = "user2";

  private static final List<String> USER_2_ROLES = ImmutableList.of( "manager", "user" );

  private static final String FILE_1 = "test" + File.separator + "t1.txt";

  private static final String FILE_1_DATE = "2012-11-13 14:56:00";

  private static final String FILE_2 = "test" + File.separator + "t2.txt";

  private static final String FILE_2_DATE = "2012-11-13 16:56:00";

  private static final String FILE_3 = "test" + File.separator + "t3.txt";

  private static final String FILE_3_DATE = "2012-11-13 18:00:00";

  private static final String FILE_NOT_STORED = "fake_test.txt";

  private static final String SQL_INJECTION_OR_TRUE = "' OR '1' = '1";

  @BeforeClass
  public static void setUp() throws Exception {
    PluginEnvironment.init( new CfrEnvironmentForTests() );
    ISecurityHelper mockedSecurityHelper = mock( ISecurityHelper.class );
    when( mockedSecurityHelper.runAsSystem( any( Callable.class ) ) ).thenReturn( true );
    SecurityHelper.setMockInstance( mockedSecurityHelper );
    PersistenceEngineForTests.getInstance().startOrient();
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );

    PersistenceEngineForTests.getInstance().initializeClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().initializeClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );

    createFile( USER_1, FILE_1, FILE_1_DATE );
    createFile( USER_1, FILE_2, FILE_2_DATE );
    createFile( USER_2, FILE_3, FILE_3_DATE );
  }

  @AfterClass
  public static void setDown() throws Exception {
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
  }

  @Mock
  private CfrService cs;

  private MetadataReader mr;

  @Before
  public void initializeMocks() {

    cs = mock( CfrService.class );

    when( cs.getCurrentUserName() ).thenReturn( USER_1 );
    when( cs.getRepository() ).thenReturn( new DefaultFileRepositoryForTests() );
    when( cs.getUserRoles() ).thenReturn( USER_1_ROLES );
    when( cs.isCurrentUserAdmin() ).thenReturn( true );

    mr = new MetadataReaderForTests( cs );

  }

  @Test
  public void testMetadataReadFull() throws JSONException {
    // admin users are supposed to have access to the whole file repo
    when( cs.isCurrentUserAdmin() ).thenReturn( true );

    JsonSerializable result = mr.listFiles( null, null, null, null );
    JSONObject obj = result.toJSON();
    JSONArray arr = obj.getJSONArray( "result" );

    Assert.assertEquals( "files count", 3, arr.length() );
  }

  @Test
  public void testMetadataReadByUser() throws JSONException {
    // admin users are supposed to have access to the whole file repo
    when( cs.isCurrentUserAdmin() ).thenReturn( true );

    JsonSerializable result = mr.listFiles( null, USER_2, null, null );
    JSONObject obj = result.toJSON();
    JSONArray arr = obj.getJSONArray( "result" );

    Assert.assertEquals( 1, arr.length() );

    Assert.assertEquals( USER_2, arr.getJSONObject( 0 ).getString( "user" ) );
    Assert.assertEquals( FILE_3, arr.getJSONObject( 0 ).getString( "file" ) );

    //making sure we cannot use SQL Injection
    JsonSerializable resultInject = mr.listFiles( null, USER_2 + SQL_INJECTION_OR_TRUE, null, null );
    JSONObject objInject = resultInject.toJSON();
    JSONArray arrInject = objInject.getJSONArray( "result" );

    Assert.assertEquals( 0, arrInject.length() );
  }

  @Test
  public void testMetadataReadByFile() throws JSONException {

    JsonSerializable result = mr.listFiles( FILE_1, null, null, null );

    JSONObject obj = result.toJSON();
    JSONArray arr = obj.getJSONArray( "result" );

    assertEquals( 1, arr.length() );

    result = mr.listFiles( FILE_NOT_STORED, null, null, null );
    arr = result.toJSON().getJSONArray( "result" );

    assertEquals( 0, arr.length() );

    //making sure we cannot use SQL Injection
    JsonSerializable resultInject = mr.listFiles( FILE_1 + SQL_INJECTION_OR_TRUE, null, null, null );
    JSONObject objInject = resultInject.toJSON();
    JSONArray arrInject = objInject.getJSONArray( "result" );

    Assert.assertEquals( 0, arrInject.length() );
  }

  @Test
  public void testMetadataReadByFileAndUser() throws JSONException {
    when( cs.getCurrentUserName() ).thenReturn( USER_2 );
    JsonSerializable result = mr.listFiles( FILE_3, USER_2, null, null );

    JSONObject obj = result.toJSON();
    JSONArray arr = obj.getJSONArray( "result" );

    Assert.assertEquals( 1, arr.length() );

  }

  @Test
  public void testMetadataReadByStartDate() throws JSONException {
    String startDate = "2012-11-13 16:50:00";
    JsonSerializable result = mr.listFiles( null, null, startDate, null );

    JSONObject obj = result.toJSON();
    JSONArray arr = obj.getJSONArray( "result" );

    Assert.assertEquals( 2, arr.length() );

    //making sure we cannot use SQL Injection
    JsonSerializable resultInject = mr.listFiles( startDate + SQL_INJECTION_OR_TRUE, null, null, null );
    JSONObject objInject = resultInject.toJSON();
    JSONArray arrInject = objInject.getJSONArray( "result" );

    Assert.assertEquals( 0, arrInject.length() );
  }

  @Test
  public void testMetadataReadByEndDate() throws JSONException {
    String endDate = "2012-11-13 16:50:00";
    JsonSerializable result = mr.listFiles( null, null, null, endDate );

    JSONObject obj = result.toJSON();

    JSONArray arr = obj.getJSONArray( "result" );

    Assert.assertEquals( 1, arr.length() );

    //making sure we cannot use SQL Injection
    JsonSerializable resultInject = mr.listFiles( endDate + SQL_INJECTION_OR_TRUE, null, null, null );
    JSONObject objInject = resultInject.toJSON();
    JSONArray arrInject = objInject.getJSONArray( "result" );

    Assert.assertEquals( 0, arrInject.length() );

  }

  @Test
  public void testGetPermissions() {
    // TODO: implementation
  }

  @Test
  public void testGetPermissionEntities() {
    // TODO: implementation
  }

  @Test
  public void testSetPermissions() {
    // TODO: implementation
  }

  @Test
  public void testIsCurrentUserAllowed() {
    when( cs.isCurrentUserAdmin() ).thenReturn( false );
    when( cs.getCurrentUserName() ).thenReturn( USER_2 );
    FilePermissionEnum permission = FilePermissionEnum.READ;
    assertFalse(
        String.format( "current user %s isn't allowed to %s %s", cs.getCurrentUserName(), permission, FILE_1 ), mr
        .isCurrentUserAllowed( permission, FILE_1 ) );

    when( cs.getCurrentUserName() ).thenReturn( USER_1 );
    assertTrue(
        String.format( "current user %s is allowed to %s file %s", cs.getCurrentUserName(), permission, FILE_1 ), mr
        .isCurrentUserAllowed( permission, FILE_1 ) );
  }

  @Test
  public void testIsCurrentUserOwner() {
    when( cs.getCurrentUserName() ).thenReturn( USER_1 );
    assertTrue( String.format( "current user %s is supposed to be the owner of the file %s", cs.getCurrentUserName(),
        FILE_1 ), mr.isCurrentUserOwner( FILE_1 ) );

    when( cs.getCurrentUserName() ).thenReturn( USER_2 );
    assertFalse( String.format( "current user %s isn't supposed to be the owner of the file %s", cs
        .getCurrentUserName(), FILE_1 ), mr.isCurrentUserOwner( FILE_1 ) );
  }

  @Test
  public void testGetOwner() {
    assertEquals( String.format( "file %s owner is ", FILE_1, USER_1 ), USER_1, mr.getOwner( FILE_1 ) );
    assertEquals( "expected empty string since the file doesn't exists", "", mr.getOwner( "unexistent_file.log" ) );
  }

}
