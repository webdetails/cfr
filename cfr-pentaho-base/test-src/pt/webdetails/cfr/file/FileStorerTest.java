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

import java.io.File;
import java.util.Properties;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cfr.CfrEnvironmentForTests;
import pt.webdetails.cfr.persistence.PersistenceEngineForTests;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.utils.MimeTypes;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileStorerTest {

  private static final String RES_STATUS = "result";
  private static final String RES_MESSAGE = "message";

  private static final String TEST_DIR = "my_test";
  private static final String FILE1 = "t.txt";
  private static final String FILE2 = "t1.txt";
  private static final String FILE1_PDF = "t.pdf";
  private static final String FILE2_PDF = "t1.pdf";
  private static final String FILE_SINGLE_QUOTE = "foo'bar.txt";

  private static final String USER = "User";
  private static final String USER2 = "User2";

  @BeforeClass
  public static void setUp() throws Exception {
    PluginEnvironment.init( new CfrEnvironmentForTests() );
    ISecurityHelper mockedSecurityHelper = mock( ISecurityHelper.class );
    when( mockedSecurityHelper.runAsSystem( any( Callable.class ) ) ).thenReturn( true );
    SecurityHelper.setMockInstance( mockedSecurityHelper );
    PersistenceEngineForTests.getInstance().startOrient();
  }

  @AfterClass
  public static void setDown() throws Exception {
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
  }

  @Before
  public void resetRepo() {
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().initializeClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngineForTests.getInstance().initializeClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
  }

  @Test
  public void testFileStorer() throws JSONException {
    FileStorer fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    JSONObject res;
    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );
    res = fs.storeFile( FILE_SINGLE_QUOTE, TEST_DIR, new byte[ 50 ], USER2 );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

    PersistenceEngine pe = PersistenceEngineForTests.getInstance();
    JSONObject result = pe.query( "select from " + FileStorer.FILE_METADATA_STORE_CLASS, null );

    JSONArray resultArray = result.getJSONArray( "object" );
    Assert.assertEquals( 2, resultArray.length() );

    JSONObject resultElt = resultArray.getJSONObject( 0 );

    Assert.assertEquals( USER, resultElt.getString( "user" ) );
    Assert.assertEquals( TEST_DIR + File.separator + FILE1, resultElt.getString( "file" ) );

    JSONObject resultElt2 = resultArray.getJSONObject( 1 );

    Assert.assertEquals( USER2, resultElt2.getString( "user" ) );
    Assert.assertEquals( TEST_DIR + File.separator + FILE_SINGLE_QUOTE, resultElt2.getString( "file" ) );

  }

  @Test
  public void testFileStorerFailLoading() throws JSONException {
    FileStorer fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    JSONObject res;

    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

    res = fs.storeFile( FILE_SINGLE_QUOTE, TEST_DIR, new byte[ 50 ], USER2 );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

    fs = new FileStorerForTests( new DefaultFileRepositoryForTests( false ) );

    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );

    res = fs.storeFile( FILE_SINGLE_QUOTE, TEST_DIR, new byte[ 50 ], USER2 );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );

    PersistenceEngine pe = PersistenceEngineForTests.getInstance();
    JSONObject result = pe.query( "select from " + FileStorer.FILE_METADATA_STORE_CLASS, null );

    JSONArray resultArray = result.getJSONArray( "object" );
    Assert.assertEquals( 2, resultArray.length() ); // There should be only the result of the first test
  }

  @Test
  public void testFileStorerWithSecurityExt() throws JSONException {
    Properties injectedConfig = new Properties();
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_MATCHERS, FileStorer.EXTENSION_MATCHER );
    JSONObject res;
    FileStorerForTests fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    fs.setInjectedConfig( injectedConfig );

    // won't match pdf to txt
    fs.setMimeTypeFromContent( MimeTypes.PDF );
    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );
    Assert.assertEquals( FileStorer.ERR_EXTENSION, res.getString( RES_MESSAGE ) );

    // will match txt to txt
    fs.setMimeTypeFromContent( MimeTypes.PLAIN_TEXT );
    res = fs.storeFile( FILE2, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( "result" ) );

  }

  @Test
  public void testFileStorerWithSecurityWht() throws JSONException {
    Properties injectedConfig = new Properties();
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_MATCHERS, FileStorer.WHITELIST_MATCHER );
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_WHITELIST, MimeTypes.PDF );
    JSONObject res;

    FileStorerForTests fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    fs.setInjectedConfig( injectedConfig );

    // plain text is not whitelisted
    fs.setMimeTypeFromContent( MimeTypes.PLAIN_TEXT );
    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );
    Assert
      .assertEquals( String.format( FileStorer.ERR_WHITELIST, MimeTypes.PLAIN_TEXT ), res.getString( RES_MESSAGE ) );

    // pdf is whitelisted
    fs.setMimeTypeFromContent( MimeTypes.PDF );
    res = fs.storeFile( FILE2_PDF, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

  }

  @Test
  public void testFileStorerWithSecurityBlk() throws JSONException {
    Properties injectedConfig = new Properties();
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_MATCHERS, FileStorer.BLACKLIST_MATCHER );
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_BLACKLIST, MimeTypes.PDF );
    JSONObject res;

    FileStorerForTests fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    fs.setInjectedConfig( injectedConfig );

    // pdf is blacklisted
    fs.setMimeTypeFromContent( MimeTypes.PDF );
    res = fs.storeFile( FILE1_PDF, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );
    Assert.assertEquals( String.format( FileStorer.ERR_BLACKLIST, MimeTypes.PDF ), res.getString( RES_MESSAGE ) );

    // plain text is not blacklisted
    fs.setMimeTypeFromContent( MimeTypes.PLAIN_TEXT );
    res = fs.storeFile( FILE2, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

  }

  @Test
  public void testFileStorerWithSecurityExtWht() throws JSONException {
    Properties injectedConfig = new Properties();
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_MATCHERS,
        FileStorer.EXTENSION_MATCHER + "," + FileStorer.WHITELIST_MATCHER );
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_WHITELIST, MimeTypes.PDF );
    JSONObject res;

    FileStorerForTests fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    fs.setInjectedConfig( injectedConfig );

    // pdf fails extension check, but is whitelisted
    fs.setMimeTypeFromContent( MimeTypes.PDF );
    res = fs.storeFile( FILE1, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

    // plain text passes extension test
    fs.setMimeTypeFromContent( MimeTypes.PLAIN_TEXT );
    res = fs.storeFile( FILE2, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

  }

  @Test
  public void testFileStorerWithSecurityExtBlk() throws JSONException {
    Properties injectedConfig = new Properties();
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_MATCHERS,
        FileStorer.EXTENSION_MATCHER + "," + FileStorer.BLACKLIST_MATCHER );
    injectedConfig.setProperty( FileStorer.UPLOAD_SECURITY_BLACKLIST, MimeTypes.PDF );
    JSONObject res;

    FileStorerForTests fs = new FileStorerForTests( new DefaultFileRepositoryForTests( true ) );
    fs.setInjectedConfig( injectedConfig );

    // pdf passes extension check, but is blacklisted
    fs.setMimeTypeFromContent( MimeTypes.PDF );
    res = fs.storeFile( FILE1_PDF, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertFalse( res.getBoolean( RES_STATUS ) );
    Assert.assertEquals( String.format( FileStorer.ERR_BLACKLIST, MimeTypes.PDF ), res.getString( RES_MESSAGE ) );

    // plain text passes extension test
    fs.setMimeTypeFromContent( MimeTypes.PLAIN_TEXT );
    res = fs.storeFile( FILE2, TEST_DIR, new byte[ 50 ], USER );
    Assert.assertTrue( res.getBoolean( RES_STATUS ) );

  }

}
