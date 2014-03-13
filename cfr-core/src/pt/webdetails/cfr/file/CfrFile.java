/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

  public CfrFile( String fileName, String downloadPath, byte[] content ) {
    this.fileName = fileName;
    this.downloadPath = downloadPath;
    this.content = content;
  }

  public CfrFile( String fileName, String downloadPath, File file ) {
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
    if ( content == null && file != null ) {
      if ( file.isFile() ) {
        content = new byte[(int) file.length()];
        try {
          DataInputStream dis = new DataInputStream( new FileInputStream( file ) );
          dis.readFully( content );
          dis.close();
        } catch ( FileNotFoundException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch ( IOException e ) {
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
    if ( file != null ) {
      return file.isDirectory();
    }
    return false;
  }

  @Override
  public boolean isFile() {
    if ( file != null ) {
      return file.isFile();
    }
    return true;
  }

}
