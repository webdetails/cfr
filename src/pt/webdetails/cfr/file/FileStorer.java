/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.persistence.PersistenceEngine;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class FileStorer {

  public static final String FILE_METADATA_STORE_CLASS = "UploadedFiles";

  public static final String FILE_PERMISSIONS_METADATA_STORE_CLASS = "UploadedFilesPermissions";

  protected static final Log logger = LogFactory.getLog(FileStorer.class);

  private IFileRepository repository;

  public FileStorer(IFileRepository repository) {
    this.repository = repository;
  }

  protected static PersistenceEngine getPersistenceEngine() {
    return PersistenceEngine.getInstance();
  }

  /**
   * 
   * @param file  Name of the file to be stored
   * @param relativePath Path relative to repository root for the file to be stored in
   * @param contents File content
   * @param user
   * @return
   */
  public boolean storeFile(String file, String relativePath, byte[] contents, String user) {
    //Store file in FileRepository
    String path = relativePath;
    String fileName = file;
    boolean result = this.repository.storeFile(contents, fileName, path);
    if (!result) {
      logger.error("Could not save file in repository. Returning false");
      return false;
    }

    MetadataReader mr = new MetadataReader(new CfrService());
    List<ODocument> fileEntities = null;
    try {
      fileEntities = mr.getFileEntities(getFullFileName(relativePath, file));
    } catch (JSONException e) {
      logger.trace(String.format("unable to retrieve file %s metadata", file), e);
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

    String id = null;
    if (fileEntities != null && fileEntities.size() > 0) {
      id = fileEntities.get(0).getIdentity().toString();
    }
    
    result = getPersistenceEngine().store(id, FILE_METADATA_STORE_CLASS, obj) != null;

    return result;
  }

  public String getFullFileName(String path, String filename) {
    StringBuilder builder = new StringBuilder(filename);
    if (path != null && path.length() > 0 && !path.endsWith("/")) {
      builder.insert(0, path + "/");
    }
    return builder.toString();
  }

  public static boolean storeFilePermissions(FilePermissionMetadata permission) {
    boolean result = false;
    MetadataReader reader = new MetadataReader(new CfrService());
    final CfrService service = new CfrService();

    if (permission != null) {
      try {
        // TODO(rafa): verify that current user is the folder/file owner or is an admin
        if (service.isCurrentUserAdmin() || reader.isCurrentUserOwner(permission.getFile())) {

          logger.debug(String.format("current user is an administrator or the owner of the file: %s",
              permission.getFile()));

          JSONObject persistedPermissions = null;
          JSONObject permissionToPersist = permission.toJson();

          // verify that the file hasn't already permissions defined
          List<String> ids = new ArrayList<String>();
          ids.add(permission.getId());
          List<ODocument> currentPermissions = reader.getPermissionEntities(permission.getFile(), ids, null);
          if (currentPermissions == null || currentPermissions.size() == 0) {
            persistedPermissions = getPersistenceEngine().store(null, FILE_PERMISSIONS_METADATA_STORE_CLASS,
                permissionToPersist);
          } else {
            String id = currentPermissions.get(0).getIdentity().toString();
            persistedPermissions = getPersistenceEngine().store(id, FILE_PERMISSIONS_METADATA_STORE_CLASS,
                permissionToPersist);
          }

          result = null != persistedPermissions;
        }
      } catch (JSONException e) {
        logger.error("unable to store " + permission, e);
      }
    }

    if (result == false) {
      logger.warn(String.format("current user doesn't have permissions to set permissions on folder/file: %s",
          permission.getFile()));
    }

    return result;
  }

  /**
   * 
   * @param path Full path of the folder/file
   * @param id Group/User Id
   */
  public static boolean deletePermissions(String path, String id) {
    CfrService service = new CfrService();
    MetadataReader reader = new MetadataReader(service);
    if (service.isCurrentUserAdmin() || reader.isCurrentUserOwner(path)) {
      Map<String, Object> params = Collections.emptyMap();
      StringBuilder deleteCommandBuilder = new StringBuilder(String.format("delete from %s",
          FILE_PERMISSIONS_METADATA_STORE_CLASS));
      StringBuilder whereBuilder = new StringBuilder();

      if (path != null) {
        whereBuilder.append("file = '").append(path).append("'");
      }

      if (id != null) {
        if (whereBuilder.length() > 0) {
          whereBuilder.append(" and ");
        }

        whereBuilder.append("id = '").append(id).append("'");
      }

      if (whereBuilder.length() > 0) {
        whereBuilder.insert(0, " where ");
      }

      String cmd = deleteCommandBuilder.append(whereBuilder.toString()).toString();
      getPersistenceEngine().executeCommand(cmd, params);
      return true;
    } else
      return false;
  }
}
