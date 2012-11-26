/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.file;

import java.io.File;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cpf.JsonSerializable;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class MetadataReaderTest {
  
  private static void createFile(String user, String file, String date) throws JSONException {
     JSONObject obj = new JSONObject();

       obj.put("user", user);
       obj.put("file", file);
       obj.put("uploadDate", date);
     PersistenceEngine.getInstance().store(null, "UploadedFiles", obj) ;
    
    
  }
  
  
   @BeforeClass
    public static void setUp() throws Exception {
        PersistenceEngine.getInstance().startOrient();
        PersistenceEngine.getInstance().dropClass("UploadedFiles");
        PersistenceEngine.getInstance().initializeClass("UploadedFiles");
    
        
        createFile("user1", "test" + File.separator + "t1.txt", "2012-11-13 14:56:00");
        createFile("user1", "test" + File.separator + "t2.txt", "2012-11-13 16:56:00");
        createFile("user2", "test" + File.separator + "t1.txt", "2012-11-13 18:00:00");
                
    }  
  
   
   
    @Test
    public void testMetadataReadFull() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles(null, null, null, null);
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(3, arr.length());            
    }


    @Test
    public void testMetadataReadByUser() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles(null,"user2", null, null);
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(1, arr.length());            
      
      Assert.assertEquals("user2", arr.getJSONObject(0).getString("user"));
      Assert.assertEquals("test" + File.separator + "t1.txt", arr.getJSONObject(0).getString("file"));
    }

    

        @Test
    public void testMetadataReadByFile() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles("test" + File.separator + "t1.txt",null, null, null);
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(2, arr.length());            
      
    }


        @Test
    public void testMetadataReadByFileAndUser() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles("test" + File.separator + "t1.txt","user2", null, null);
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(1, arr.length());            
      
    }
        
        
        
        @Test
    public void testMetadataReadByStartDate() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles(null,null, "2012-11-13 16:50:00", null);
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(2, arr.length());            
      
    }
        

        
        @Test
    public void testMetadataReadByEndDate() throws JSONException {
      MetadataReader mr = new MetadataReader();
      
      JsonSerializable result = mr.listFiles(null,null, null, "2012-11-13 16:50:00");
      
      JSONObject obj = result.toJSON();
     
      JSONArray arr = obj.getJSONArray("result");
      
      Assert.assertEquals(1, arr.length());            
      
    }
        
        
   
}
