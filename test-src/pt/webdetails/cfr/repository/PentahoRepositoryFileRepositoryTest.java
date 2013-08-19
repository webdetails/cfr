/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.repository;

import com.orientechnologies.orient.core.processor.block.OIterateBlock;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;
import pt.webdetails.cpf.repository.IRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.IRepositoryFileFilter;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;


public class PentahoRepositoryFileRepositoryTest {
  
  
  public class PentahoRepositoryFileRepositoryForTests extends PentahoRepositoryFileRepository {
  
    private boolean hasAccess;
    
    public PentahoRepositoryFileRepositoryForTests() {
      this(true);
    }

    public PentahoRepositoryFileRepositoryForTests(boolean hasAccess) {
      this.hasAccess = hasAccess;
    }
    
    
    
    @Override 
    protected IRepositoryAccess getRepositoryAccess(){
      return new PentahoRepositoryAccess(null){

        @Override
        public SaveFileStatus publishFile(String path, String fileName, byte[] data, boolean overwrite) {
          return SaveFileStatus.OK;
        }        
        
        @Override
        public IRepositoryFile[] getFileList(String dir, final String fileExtensions, String access, IPentahoSession userSession) {
          IRepositoryFile[] files = new IRepositoryFile[2];
          
                  
          files[0] = new IRepositoryFile() {
            @Override
            public String getFullPath() {
              return "/tmp/first.txt";
            }

            @Override
            public boolean isDirectory() {
              return false;
            }

            @Override
            public String getFileName() {
              return "first.txt";
            }

            @Override
            public String getSolutionPath() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getSolution() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile[] listFiles() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile[] listFiles(IRepositoryFileFilter iff) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isRoot() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile retrieveParent() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte[] getData() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean exists() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getLastModified() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getExtension() {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          };

          
          files[1] = new IRepositoryFile() {
            @Override
            public String getFullPath() {
              return "/tmp/newLevel/";
            }

            @Override
            public boolean isDirectory() {
              return true;
            }

            @Override
            public String getFileName() {
              return "newLevel";
            }

            @Override
            public String getSolutionPath() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getSolution() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile[] listFiles() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile[] listFiles(IRepositoryFileFilter iff) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isRoot() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public IRepositoryFile retrieveParent() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte[] getData() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean exists() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getLastModified() {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getExtension() {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          };
          
          
          return files;
        }        
        
        
        @Override
        public InputStream getResourceInputStream(String filePath, FileAccess fileAccess) throws FileNotFoundException{        
          if (filePath.equals("my_tests/anotherLevel/does_not_exist.txt"))
            throw new FileNotFoundException();
          
          return new ByteArrayInputStream("Hello, World".getBytes());
        }        
        
        @Override
        public boolean createFolder(String solutionFolderPath) throws IOException {        
          return true;
        }
        
        @Override
        public boolean removeFile(String solutionPath){
          return true;
        }

        @Override
        public boolean hasAccess(String filePath, FileAccess access) {
          return hasAccess;
        }
        
        
        
      };
    } 
    
    @Override
    protected IPentahoSession getUserSession() {
      return null;
    }        
  }
  
    
    
  
  
  
    
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
    Assert.assertEquals( "t.txt", f.getFileName());
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
    

    Assert.assertEquals("first.txt",files[0].getName());        
    Assert.assertEquals("newLevel",files[1].getName());
        
  }
  
  
  
}
