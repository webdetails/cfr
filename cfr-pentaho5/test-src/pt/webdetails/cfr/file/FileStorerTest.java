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

package pt.webdetails.cfr.file;

import java.io.File;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.webdetails.cfr.CfrEnvironmentForTests;
import pt.webdetails.cfr.repository.DefaultFileRepositoryForTests;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class FileStorerTest {

  @BeforeClass
  public static void setUp() throws Exception {
    PluginEnvironment.init( new CfrEnvironmentForTests() );
    PersistenceEngine.getInstance().startOrient();
  }

  @AfterClass
  public static void setDown() throws Exception {
    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
  }

  @Before
  public void resetRepo() {
    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().dropClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().initializeClass( FileStorer.FILE_METADATA_STORE_CLASS );
    PersistenceEngine.getInstance().initializeClass( FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS );
  }

  @Test
  public void testFileStorer() throws JSONException {
    FileStorer fs = new FileStorer( new DefaultFileRepositoryForTests( true ) );

    Assert.assertTrue( fs.storeFile( "t.txt", "my_test", new byte[ 50 ], "User" ) );

    PersistenceEngine pe = PersistenceEngine.getInstance();
    JSONObject result = pe.query( "select from " + FileStorer.FILE_METADATA_STORE_CLASS, null );

    JSONArray resultArray = result.getJSONArray( "object" );
    Assert.assertEquals( 1, resultArray.length() );

    JSONObject resultElt = resultArray.getJSONObject( 0 );

    Assert.assertEquals( "User", resultElt.getString( "user" ) );
    Assert.assertEquals( "my_test" + File.separator + "t.txt", resultElt.getString( "file" ) );

  }

  @Test
  public void testFileStorerFailLoading() throws JSONException {
    FileStorer fs = new FileStorer( new DefaultFileRepositoryForTests( true ) );

    Assert.assertTrue( fs.storeFile( "t.txt", "my_test", new byte[ 50 ], "User" ) );

    fs = new FileStorer( new DefaultFileRepositoryForTests( false ) );

    Assert.assertFalse( fs.storeFile( "t.txt", "my_test", new byte[ 50 ], "User" ) );

    PersistenceEngine pe = PersistenceEngine.getInstance();
    JSONObject result = pe.query( "select from " + FileStorer.FILE_METADATA_STORE_CLASS, null );

    JSONArray resultArray = result.getJSONArray( "object" );
    Assert.assertEquals( 1, resultArray.length() ); // There should be only the result of the first test
  }

}
