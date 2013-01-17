/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.io.File;


public class CfrFile implements IFile {       

  private String fileName;
  private byte[] content;
  private String downloadPath;
 
  
  
  public CfrFile(String fileName, String downloadPath, byte[] content) {
    this.fileName = fileName;
    this.downloadPath = downloadPath;
    this.content = content;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return the content
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * @return the downloadPath
   */
  public String getDownloadPath() {
    return downloadPath;
  }

  @Override
  public String getFullPath() {
    return downloadPath + File.pathSeparator + fileName;
  }

  @Override
  public String getName() {
    return fileName;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isFile() {
    return true;
  }
     
    
  
  
}
