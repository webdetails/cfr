/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.messaging.JsonSerializable;
import pt.webdetails.cpf.persistence.PersistenceEngine;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class MetadataReader {

  private static Log logger = LogFactory.getLog(MetadataReader.class);

  private static CfrService service = new CfrService();

  public JSONArray listFilesFlat(String fileName, String user, String startDate, String endDate) {
    Map<String, Object> params = new HashMap<String, Object>();
    String query = "select * from " + FileStorer.FILE_METADATA_STORE_CLASS + " ";
    String where = "";
    if (fileName != null && fileName.length() > 0) {
      where = " file = :fileName ";
      params.put("fileName", fileName);
    }
    if (user != null && user.length() > 0) {
      if (where.length() > 0)
        where += " and ";
      where += " user = :user";
      params.put("user", user);
    }

    if (startDate != null && startDate.length() > 0) {
      if (where.length() > 0)
        where += " and ";
      where += " uploadDate >= :startDate";
      params.put("startDate", startDate);

    }

    if (endDate != null && endDate.length() > 0) {
      if (where.length() > 0)
        where += " and ";
      where += " uploadDate <= :endDate";
      params.put("endDate", endDate);

    }

    if (where.length() > 0)
      where = " where " + where;

    JSONArray array = new JSONArray();
    for (ODocument doc : getPersistenceEngine().executeQuery(query + where, params)) {
      String file = doc.field("file", String.class);
      String owner = doc.field("user", String.class);

      // TODO: optimization to take advantage of OrientDB LINK relation

      // check if current user is the file owner
      if (owner.equals(service.getUserName())) {
        array.put(getJson(doc));
      } else if (isCurrentUserAllowed(FilePermissionEnum.READ, file)) { // check if current user has read permissions
        array.put(getJson(doc));
      }
    }

    return array;

  }

  public JsonSerializable listFiles(String fileName, String user, String startDate, String endDate) {

    return Result.getOK(listFilesFlat(fileName, user, startDate, endDate));

  }

  protected PersistenceEngine getPersistenceEngine() {
    return PersistenceEngine.getInstance();
  }

  private static JSONObject getJson(ODocument doc) {
    JSONObject json = new JSONObject();

    for (String field : doc.fieldNames()) {
      try {
        Object value = doc.field(field); //doc.<Object>field(field)
        if (value instanceof ODocument) {
          ODocument docVal = (ODocument) value;
          json.put(field, getJson(docVal));
        } else if (value != null) {
          json.put(field, value);
        }
      } catch (JSONException e) {
        logger.error(e);
      }
    }

    return json;
  }

  public JsonSerializable getFilePermissions(String file) throws JSONException {
    String query = "select * from " + FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS + " where file = :file";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("file", file);

    JSONArray result = new JSONArray();
    for (ODocument doc : getPersistenceEngine().executeQuery(query, params)) {
      result.put(getJson(doc));
    }

    return Result.getOK(result);
  }

  public boolean isCurrentUserAllowed(FilePermissionEnum permission, String file) {
    boolean result = false;

    try {
      result = getFilePermissions(file, service.getUserName(), FilePermissionMetadata.DEFAULT_PERMISSIONS).length() > 0;
    } catch (JSONException e) {
      // TODO: make pretty print of error
      logger.error(e.getMessage());
    }

    return result;
  }

  public boolean isCurrentUserOwner(String file) {
    boolean result = false;

    // TODO : implementation
    result = true;

    return result;
  }

  /**
   * 
   * @param file
   * @param id
   * @param allowedPermissions
   * @return
   * @throws JSONException
   */
  public JSONArray getFilePermissions(String file, String id, Set<FilePermissionEnum> allowedPermissions)
      throws JSONException {
    StringBuilder queryBuilder = new StringBuilder("select * from ")
        .append(FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS);

    StringBuilder whereBuilder = new StringBuilder();
    if (file != null)
      whereBuilder.append(" file = '").append(file).append("'");
    if (id != null) {
      if (whereBuilder.length() != 0) {
        whereBuilder.append(" and");
      }
      whereBuilder.append(" id = '").append(id).append("'");
    }
    if (allowedPermissions != null && allowedPermissions.size() > 0) {
      if (whereBuilder.length() != 0) {
        whereBuilder.append(" and");
      }
      whereBuilder.append(" permissions in ").append(toStringArray(allowedPermissions));
    }

    if (whereBuilder.length() > 0) {
      whereBuilder.insert(0, " where ");
    }

    JSONArray result = new JSONArray();
    String query = queryBuilder.append(whereBuilder).toString();
    for (ODocument doc : getPersistenceEngine().executeQuery(query, Collections.EMPTY_MAP)) {
      result.put(getJson(doc));
    }

    return result;
  }

  private String toStringArray(Collection<FilePermissionEnum> allowedPermissions) {
    StringBuilder result = new StringBuilder("[");

    for (FilePermissionEnum elem : allowedPermissions) {
      result.append("'").append(elem.getId()).append("',");
    }

    int index = result.lastIndexOf(",");
    if (index > 0) {
      result.deleteCharAt(index);
    }

    result.append("]");

    return result.toString();
  }

  private String[] toArrayString(FilePermissionEnum[] allowedPermissions) {
    List<String> result = new ArrayList<String>();

    for (FilePermissionEnum elem : allowedPermissions) {
      result.add(elem.getId());
    }

    return (String[]) result.toArray();
  }

}
