/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cfr.repository;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.BufferedFSInputStream;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cfr.file.CfrFile;


/***
 * The default file repository takes system/.cfr as its base path.
 * All files and folders are created here.
 * @author pedrovale
 */
public class DefaultFileRepository implements IFileRepository {

static Log logger = LogFactory.getLog(DefaultFileRepository.class);
  
  protected String getBasePath() {
    return PentahoSystem.getApplicationContext().getSolutionPath("/system/.cfr");
  }
  
  
  @Override
  public void init() {
    
  }
  
  @Override 
  public void shutdown() {
    
  }
  
  @Override
  public boolean storeFile(byte[] content, String fileName, String relativePath) {
    String fullPath = getBasePath() + File.separator + relativePath;
    File f = new File(fullPath, fileName);
    
    if (!f.getParentFile().exists()) {
      f.getParentFile().mkdirs();
    }
    FileOutputStream fos;
    
    try {
      fos = new FileOutputStream(f, false);    
      fos.write(content);    
      fos.flush();
      fos.close();
    } catch (FileNotFoundException fnfe) {
      logger.error("Unable to create file. Check permissions on folder " + fullPath, fnfe);
      return false;
    } catch (IOException ioe) {
      logger.error("Error caught while writing file", ioe);
      return false;
    } 
    
    
    return true;
  }

  @Override
  public File[] listFiles(String startPath) {
    File f = new File(getBasePath() + File.separator + startPath);
    return f.listFiles();
  }
  
  
  @Override 
  public CfrFile getFile(String fullName) {
    File f = new File(getBasePath() + File.separator + fullName);
    if (!f.exists()) {
      logger.error("File not found for " + fullName + ". Returning null.");
      return null;
    }
        
    byte [] fileData;
    try {
        fileData = new byte[(int)f.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        dis.readFully(fileData);
        dis.close();    
    } catch (FileNotFoundException fnfe) {
      logger.error("File not found for " + fullName + ". Returning null.", fnfe);
      return null;      
    } catch (IOException ioe) {
      logger.error("Error caught while reading file from disk. Returning null", ioe);
      return null;
    }
    CfrFile result = new CfrFile(f.getName(), f.getPath().replace(f.getName(), ""), fileData);
    
    return result;
    
  }
  
}
