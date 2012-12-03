/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.JsonSerializable;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.persistence.PersistenceEngine;


public class MetadataReader {

  private static Log logger = LogFactory.getLog(MetadataReader.class); 
  
  
  public JSONArray listFilesFlat(String fileName, String user, String startDate, String endDate) {
    Map<String,Object> params = new HashMap<String, Object>();
    String query = "select * from UploadedFiles ";
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
    for(ODocument doc : getPersistenceEngine().executeQuery(query + where, params)){
      array.put(getJson(doc));
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
    
    for(String field : doc.fieldNames()){
      try{
      Object value = doc.field(field); //doc.<Object>field(field)
      if(value instanceof ODocument){
        ODocument docVal = (ODocument) value;
        json.put(field, getJson(docVal));
      }
      else if(value != null) {
        json.put(field, value);
      }
      } catch(JSONException e){
        logger.error(e);
      }
    }
    
    return json;
  }  
  
  
  
}
