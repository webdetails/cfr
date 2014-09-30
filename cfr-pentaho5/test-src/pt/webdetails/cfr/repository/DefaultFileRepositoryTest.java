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

package pt.webdetails.cfr.repository;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;

public class DefaultFileRepositoryTest {

  @AfterClass
  public static void onTestFinish() {
    File f = new File( "." + File.separator + "tests" + File.separator + "my_tests" + File.separator + "anotherLevel" );
    if ( f.exists() ) {
      File[] files = f.listFiles();

      for ( File fi : files ) {
        fi.delete();
      }
      f.delete();
    }
    f = new File( "." + File.separator + "tests" + File.separator + "my_tests" );
    if ( f.exists() ) {
      File[] files = f.listFiles();
      for ( File fi : files ) {
        fi.delete();
      }
      f.delete();
      f.getParentFile().delete();
    }

    f = new File( "." + File.separator + "tests" + File.separator + "list_tests/anotherLevel" );
    if ( f.exists() ) {
      File[] files = f.listFiles();
      for ( File fi : files ) {
        fi.delete();
      }
      f.delete();
      f = f.getParentFile();
      files = f.listFiles();
      for ( File fi : files ) {
        fi.delete();
      }
      f.delete();

    }

  }

  @Test
  public void testFolderCreation() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    Assert.assertTrue( fileRep.createFolder( "my_tests/created_folder" ) );

    // Second create should return true - nothing to do
    Assert.assertTrue( fileRep.createFolder( "my_tests/created_folder" ) );
  }

  @Test
  public void testFileDeletion() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    Assert.assertTrue( fileRep.storeFile( new byte[2], "t.txt", "my_tests" ) );

    Assert.assertTrue( fileRep.deleteFile( "my_tests/t.txt" ) );
  }

  @Test
  public void testFolderDeletion() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();
    Assert.assertTrue( fileRep.createFolder( "my_tests/created_folder_2" ) );

    Assert.assertTrue( fileRep.deleteFile( "my_tests/created_folder_2" ) );
  }

  @Test
  public void testNonExistentFileDeletion() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    Assert.assertFalse( fileRep.deleteFile( "my_tests/does_not_exist.dne" ) );
  }

  @Test
  public void testFileCreation() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    byte[] content = new byte[100];
    Assert.assertTrue( fileRep.storeFile( content, "t.txt", "my_tests" ) );

  }

  @Test
  public void testFileCreationMultipleLevels() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    byte[] content = "Hello, World".getBytes();
    Assert.assertTrue( fileRep.storeFile( content, "t.txt", "my_tests/anotherLevel" ) );

  }

  @Test
  public void testCreateFileGetFile() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    byte[] content = "Hello, World".getBytes();
    Assert.assertTrue( fileRep.storeFile( content, "t.txt", "my_tests/anotherLevel" ) );

    // Now get
    CfrFile f = fileRep.getFile( "my_tests/anotherLevel/t.txt" );
    byte[] readContent = f.getContent();

    Assert.assertEquals( "Hello, World", new String( readContent ) );
    Assert.assertEquals( "t.txt", f.getFileName() );
    Assert.assertEquals( "./test-resources/my_tests/anotherLevel/", f.getDownloadPath() );
  }

  @Test
  public void testGetFileNonExistentFile() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    // Now get
    CfrFile f = fileRep.getFile( "my_tests/anotherLevel/does_not_exist.txt" );
    Assert.assertNull( f );
  }

  @Test
  public void testListFilesWithInvalidPath() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    Assert.assertEquals( 0, fileRep.listFiles( "../teste" ).length );

    Assert.assertEquals( 0, fileRep.listFiles( "test/../teste" ).length );
  }

  @Test
  public void testListFiles() {
    DefaultFileRepositoryForTests fileRep = new DefaultFileRepositoryForTests();

    byte[] content = new byte[100];
    Assert.assertTrue( fileRep.storeFile( content, "first.txt", "list_tests" ) );

    Assert.assertTrue( fileRep.storeFile( content, "second.txt", "list_tests/newLevel" ) );
    Assert.assertTrue( fileRep.storeFile( content, "third.txt", "list_tests/newLevel" ) );

    IFile[] files = fileRep.listFiles( "list_tests" );

    Assert.assertEquals( 2, files.length );

    if ( "first.txt".equals( files[0].getName() ) ) {
      Assert.assertEquals( true, files[0].isFile() );
      Assert.assertEquals( "first.txt", files[0].getName() );
      Assert.assertEquals( true, files[1].isDirectory() );
      Assert.assertEquals( "newLevel", files[1].getName() );
    } else {
      Assert.assertEquals( true, files[1].isFile() );
      Assert.assertEquals( "first.txt", files[1].getName() );
      Assert.assertEquals( true, files[0].isDirectory() );
      Assert.assertEquals( "newLevel", files[0].getName() );
    }

    files = fileRep.listFiles( "list_tests/newLevel" );

    final List<String> expectedFileNames = com.google.common.collect.ImmutableList.of( "second.txt", "third.txt" );
    Assert.assertEquals( "files count", 2, files.length );
    Assert.assertEquals( true, files[0].isFile() );
    Assert.assertEquals( true, files[1].isFile() );
    Assert.assertTrue( expectedFileNames.contains( files[0].getName() ) );
    Assert.assertTrue( expectedFileNames.contains( files[1].getName() ) );

  }

}
