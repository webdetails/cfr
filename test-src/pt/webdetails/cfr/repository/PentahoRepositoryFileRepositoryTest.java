/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.repository;

import junit.framework.Assert;

import org.junit.Test;

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;

public class PentahoRepositoryFileRepositoryTest {

  @Test
  public void testFolderCreation() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    Assert.assertTrue(fileRep.createFolder("my_tests/created_folder"));

    //Second create should return true - nothing to do
    Assert.assertTrue(fileRep.createFolder("my_tests/created_folder"));
  }

  @Test
  public void testFileDeletion() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();
    Assert.assertTrue(fileRep.storeFile(new byte[2], "t.txt", "my_tests"));

    Assert.assertTrue(fileRep.deleteFile("my_tests/t.txt"));
  }

  @Test
  public void testFolderDeletion() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();
    Assert.assertTrue(fileRep.createFolder("my_tests/created_folder_2"));

    Assert.assertTrue(fileRep.deleteFile("my_tests/created_folder_2"));
  }

  @Test
  public void testFileDeletionNoAccess() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests(false);

    Assert.assertFalse(fileRep.deleteFile("my_tests/does_not_exist.dne"));
  }

  @Test
  public void testFileCreation() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    byte[] content = new byte[100];
    Assert.assertTrue(fileRep.storeFile(content, "t.txt", "my_tests"));

  }

  @Test
  public void testFileCreationMultipleLevels() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    byte[] content = "Hello, World".getBytes();
    Assert.assertTrue(fileRep.storeFile(content, "t.txt", "my_tests/anotherLevel"));

  }

  @Test
  public void testCreateFileGetFile() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    byte[] content = "Hello, World".getBytes();
    Assert.assertTrue(fileRep.storeFile(content, "t.txt", "my_tests/anotherLevel"));

    //Now get
    CfrFile f = fileRep.getFile("my_tests/anotherLevel/t.txt");
    byte[] readContent = f.getContent();

    Assert.assertEquals("Hello, World", new String(readContent));
    Assert.assertEquals("t.txt", f.getFileName());
    Assert.assertEquals("my_tests/anotherLevel/", f.getDownloadPath());
  }

  @Test
  public void testGetFileNonExistentFile() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    //Now get
    CfrFile f = fileRep.getFile("my_tests/anotherLevel/does_not_exist.txt");
    Assert.assertNull(f);
  }

  @Test
  public void testListFiles() {
    PentahoRepositoryFileRepositoryForTests fileRep = new PentahoRepositoryFileRepositoryForTests();

    byte[] content = new byte[100];
    Assert.assertTrue(fileRep.storeFile(content, "first.txt", "list_tests"));

    Assert.assertTrue(fileRep.storeFile(content, "second.txt", "list_tests/newLevel"));
    Assert.assertTrue(fileRep.storeFile(content, "third.txt", "list_tests/newLevel"));

    IFile[] files = fileRep.listFiles("list_tests");

    Assert.assertEquals(2, files.length);

    Assert.assertEquals("first.txt", files[0].getName());
    Assert.assertEquals("newLevel", files[1].getName());

  }

}
