/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;


public class CfrFile {       

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
     
    
  
  
}
