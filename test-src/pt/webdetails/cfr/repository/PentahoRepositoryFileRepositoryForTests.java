package pt.webdetails.cfr.repository;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.pentaho.platform.api.engine.IPentahoSession;

import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.IRepositoryFileFilter;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

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
