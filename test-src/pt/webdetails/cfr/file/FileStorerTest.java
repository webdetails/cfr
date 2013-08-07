/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.io.File;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cfr.repository.DefaultFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class FileStorerTest {

  @BeforeClass
  public static void setUp() throws Exception {
    PersistenceEngine.getInstance().startOrient();
    PersistenceEngine.getInstance().dropClass(FileStorer.FILE_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().initializeClass(FileStorer.FILE_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().dropClass(FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().initializeClass(FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS);
  }

  @Test
  public void testFileStorer() throws JSONException {
    FileStorer fs = new FileStorer(new DefaultFileRepositoryForTests());

    Assert.assertTrue(fs.storeFile("t.txt", "my_test", new byte[50], "User"));

    PersistenceEngine pe = PersistenceEngine.getInstance();
    JSONObject result = pe.query("select from UploadedFiles", null);

    JSONArray resultArray = result.getJSONArray("object");
    Assert.assertEquals(1, resultArray.length());

    JSONObject resultElt = resultArray.getJSONObject(0);

    Assert.assertEquals("User", resultElt.getString("user"));
    Assert.assertEquals("my_test" + File.separator + "t.txt", resultElt.getString("file"));

  }

  @Test
  public void testFileStorerFailLoading() throws JSONException {
    FileStorer fs = new FileStorer(new DefaultFileRepositoryForTests(false));

    Assert.assertFalse(fs.storeFile("t.txt", "my_test", new byte[50], "User"));

    PersistenceEngine pe = PersistenceEngine.getInstance();
    JSONObject result = pe.query("select from UploadedFiles", null);

    JSONArray resultArray = result.getJSONArray("object");
    Assert.assertEquals(1, resultArray.length()); //There should be only the result of the first test      
  }

  public class DefaultFileRepositoryForTests extends DefaultFileRepository {

    private boolean result;

    public DefaultFileRepositoryForTests() {
      this.result = true;
    }

    public DefaultFileRepositoryForTests(boolean expectedResult) {
      this.result = expectedResult;
    }

    @Override
    protected String getBasePath() {
      return "./tests";
    }

    @Override
    public boolean storeFile(byte[] content, String fileName, String relativePath) {
      return this.result;
    }
  }

}
