/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CfrFile implements IFile {

  private File file;

  private String fileName;

  private byte[] content;

  private String downloadPath;

  public CfrFile(String fileName, String downloadPath, byte[] content) {
    this.fileName = fileName;
    this.downloadPath = downloadPath;
    this.content = content;
  }

  public CfrFile(String fileName, String downloadPath, File file) {
    this.fileName = fileName;
    this.downloadPath = downloadPath;
    this.file = file;
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
    if (content == null && file != null) {
      if (file.isFile()) {
        content = new byte[(int) file.length()];
        try {
          DataInputStream dis = new DataInputStream(new FileInputStream(file));
          dis.readFully(content);
          dis.close();
        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

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
    if (file != null) {
      return file.isDirectory();
    }
    return false;
  }

  @Override
  public boolean isFile() {
    if (file != null) {
      return file.isFile();
    }
    return true;
  }

}
