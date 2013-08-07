/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.SaveFileStatus;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

public class PentahoRepositoryFileRepository implements IFileRepository {

  static Log logger = LogFactory.getLog(PentahoRepositoryFileRepository.class);  
  
  
  protected IRepositoryAccess getRepositoryAccess() {
    return PentahoRepositoryAccess.getRepository();
  }
  
  
  protected IPentahoSession getUserSession() {
    return PentahoSessionHolder.getSession();
  }
  
  @Override
  public void init() {

  }

  @Override
  public boolean storeFile(byte[] content, String fileName, String relativePath) {
    SaveFileStatus sfs = getRepositoryAccess().publishFile(relativePath + "/" + fileName, content, true);
    return sfs == SaveFileStatus.OK;
  }

  @Override
  public IFile[] listFiles(String startPath) {
    IRepositoryFile[] repositoryFiles = ((PentahoRepositoryAccess)getRepositoryAccess()).getFileList(startPath, null, null, getUserSession());
    IFile[] result = new IFile[repositoryFiles.length];
    for (int i=0; i < repositoryFiles.length; i++) {
      final IRepositoryFile f = repositoryFiles[i];
      result[i] = new IFile() {

        @Override
        public String getFullPath() {
          return f.getFullPath();
        }

        @Override
        public String getName() {
          return f.getFileName();
        }

        @Override
        public boolean isDirectory() {
          return f.isDirectory();
        }

        @Override
        public boolean isFile() {
          return !f.isDirectory();
        }
      
      };
    }
    
    return result;
  }

  @Override
  public CfrFile getFile(String fullName) {
    InputStream is = null;
    try {
      is = getRepositoryAccess().getResourceInputStream(fullName, FileAccess.READ);
    } catch (FileNotFoundException fnfe) {
      logger.error("file not found: " + fullName, fnfe);
      return null;
    }
    
    byte[] contents = null;
    try {
      contents = new byte[is.available()];
      is.read(contents);    
      is.close();
    } catch (IOException ioe) {
      logger.error("IOException while reading file content", ioe);
      return null;
    }
    String[] pathComponents = fullName.split("/");
    String fileName = pathComponents[pathComponents.length - 1];
    CfrFile resultFile = new CfrFile(fileName, fullName.replace(fileName, ""), contents);
    return resultFile;
  }

  @Override
  public boolean createFolder(String fullPathName) {
    try {
      return getRepositoryAccess().createFolder(fullPathName);
    } catch (IOException ioe) {
      logger.error("Error while creating folder", ioe);
      return false;
    }
  }

  @Override
  public boolean deleteFile(String fullName) {
    if (getRepositoryAccess().hasAccess(fullName, FileAccess.DELETE))
      return getRepositoryAccess().removeFile(fullName);
    return false;
  }

  @Override
  public void shutdown() {
  }
  
}
