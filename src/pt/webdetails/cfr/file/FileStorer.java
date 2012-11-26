/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import com.ibm.icu.text.DateFormat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;


public class FileStorer {
  
  static Log logger = LogFactory.getLog(FileStorer.class);
  
  private IFileRepository repository;
  
  public FileStorer(IFileRepository repository) {
    this.repository = repository;
  }
  
  
  protected PersistenceEngine getPersistenceEngine() {
    return PersistenceEngine.getInstance();
  }
  
  
  public boolean storeFile(String fileName, String path, byte[] contents, String user) {
    //Store file in FileRepository
    boolean result = this.repository.storeFile(contents, fileName, path);
    if (!result) {
      logger.error("Could not save file in repository. Returning false");
      return false;      
    }
    
    JSONObject obj = new JSONObject();
    try {
      obj.put("user", user);
      obj.put("file", path + File.separator + fileName);
      obj.put("uploadDate", new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date()) );
    } catch (JSONException jse) {
      logger.error("An error ocurred while creating json object representing the upload.", jse);
      return false;
    }
    return getPersistenceEngine().store(null, "UploadedFiles", obj) != null;        
  }
  
}
