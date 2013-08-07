/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class FileStorer {

  public static final String FILE_METADATA_STORE_CLASS = "UploadedFiles";

  public static final String FILE_PERMISSIONS_METADATA_STORE_CLASS = "UploadedFilesPermissions";

  static Log logger = LogFactory.getLog(FileStorer.class);

  private IFileRepository repository;

  public FileStorer(IFileRepository repository) {
    this.repository = repository;
  }

  protected static PersistenceEngine getPersistenceEngine() {
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
      obj.put("file", getFullFileName(path, fileName));
      obj.put("uploadDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    } catch (JSONException jse) {
      logger.error("An error ocurred while creating json object representing the upload.", jse);
      return false;
    }
    return getPersistenceEngine().store(null, FILE_METADATA_STORE_CLASS, obj) != null;
  }

  public String getFullFileName(String path, String filename) {
    return new StringBuilder(path).append(File.separator).append(filename).toString();
  }

  public static boolean storeFilePermissions(FilePermissionMetadata permission) {
    boolean result = false;

    if (permission != null) {
      try {
        if (new MetadataReader().isCurrentUserOwner(permission.getFile())) {
          JSONObject permissionToPersist = permission.toJson();
          JSONObject persistedPermission = getPersistenceEngine().store(null, FILE_PERMISSIONS_METADATA_STORE_CLASS,
              permissionToPersist);
          result = null != persistedPermission;
        }
      } catch (JSONException e) {
        logger.error("unable to store " + permission, e);
      }
    }

    return result;
  }

}
