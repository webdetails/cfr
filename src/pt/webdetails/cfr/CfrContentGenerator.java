/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.FileStorer;
import pt.webdetails.cfr.file.MetadataReader;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.InvalidOperationException;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.olap.OlapUtils;
import pt.webdetails.cpf.persistence.PersistenceEngine;

/**
 *
 * @author pdpi
 */
public class CfrContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;
  private static Map<String, Method> exposedMethods = new HashMap<String, Method>();

  static {
    //to keep case-insensitive methods
    exposedMethods = getExposedMethods(CfrContentGenerator.class, true);
  }

  @Override
  protected Method getMethod(String methodName) throws NoSuchMethodException {
    Method method = exposedMethods.get(StringUtils.lowerCase(methodName));
    if (method == null) {
      throw new NoSuchMethodException();
    }
    return method;
  }

  @Override
  public String getPluginName() {
    return "cfr";
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void xpto(OutputStream out) throws IOException {

    IParameterProvider requestParams = getRequestParameters();
//    IParameterProvider pathParams = getPathParameters();
    ServletRequest wrapper = getRequest();
    String root = wrapper.getScheme() + "://"+ wrapper.getServerName() + ":" + wrapper.getServerPort();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("solution", "system");
    params.put("path", "cfr/presentation/");
    params.put("file", "cfr.wcdf");
    params.put("absolute", "true");
    params.put("inferScheme", "false");
    params.put("root", root);

    //add request parameters
    copyParametersFromProvider(params, requestParams);

    if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {

      // Send this to CDE

      redirectToCdeEditor(out, params);
      return;

    }

    InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
    pluginCall.setResponse(getResponse());
    pluginCall.setOutputStream(out);
    pluginCall.run();

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void store(OutputStream out) throws IOException, InvalidOperationException, Exception {
        
    
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List /* FileItem */ items = upload.parseRequest(getRequest());
    
    String fileName = null, savePath = null;
    byte[] contents = null;
    for (int i=0; i < items.size(); i++) {
      FileItem fi = (FileItem) items.get(i);
      
      if ("path".equals(fi.getFieldName()))
        savePath = fi.getString();
      if ("file".equals(fi.getFieldName())) {
        contents = fi.get();                 
        fileName= fi.getName();
      }
    }
    

    if (fileName == null) {
      logger.error("parameter fileName must not be null");
      throw new Exception("paramete fileName must not be null");
    }
    if (savePath == null) {
      logger.error("parameter path must not be null");
      throw new Exception("parameter path must not be null");
    }   
    if (contents == null) {
      logger.error("File content must not be null");
      throw new Exception("File content must not be null");
    }
    
    FileStorer fileStorer = new FileStorer(getRepository());
    
    boolean result = fileStorer.storeFile(fileName, savePath, contents, userSession.getName());
    writeOut(out, "<result>" + result + "</result>");
  }

  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listFiles(OutputStream out) throws IOException {
    String baseDir = getRequestParameters().getStringParameter("path", "");
    File[] files = getRepository().listFiles(baseDir);
    
    writeOut(out, toJQueryFileTree(baseDir, files));
    
  }
  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getFile(OutputStream out) throws IOException, JSONException, Exception {
    String fullFileName = getRequestParameters().getStringParameter("fileName", null);
    
    if (fullFileName == null) {
      logger.error("parameter fullFileName must not be null");
      throw new Exception("parameter fullFileName must not be null");
      
    }
    
    CfrFile file = getRepository().getFile(fullFileName);
    
    setResponseHeaders(getMimeType(file.getFileName()), -1, null);
    ByteArrayInputStream bais = new ByteArrayInputStream(file.getContent());
    IOUtils.copy(bais, out);
    IOUtils.closeQuietly(bais);
  }
  
  

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listFilesJSON(OutputStream out) throws IOException, JSONException {
    String baseDir = getRequestParameters().getStringParameter("path", "");
    File[] files = getRepository().listFiles(baseDir);
    JSONArray arr = new JSONArray();
    for (File file : files) {
      JSONObject obj = new JSONObject();
      obj.put("fileName", file.getName());
      obj.put("isDirectory", file.isDirectory());
      obj.put("path", baseDir);
      arr.put(obj);
    }
    
    writeOut(out, arr.toString(2));
    
  }
  
  
  
  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listUploads(OutputStream out) throws IOException, JSONException {
    MetadataReader reader = new MetadataReader();
    writeOut(out, reader.listFiles(getRequestParameters().getStringParameter("fileName", ""), 
            getRequestParameters().getStringParameter("user", ""),
            getRequestParameters().getStringParameter("startDate", ""), 
            getRequestParameters().getStringParameter("endDate", "")));
  }
  

  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listUploadsFlat(OutputStream out) throws IOException, JSONException {
    MetadataReader reader = new MetadataReader();
    writeOut(out, reader.listFilesFlat(getRequestParameters().getStringParameter("fileName", ""), 
            getRequestParameters().getStringParameter("user", ""),
            getRequestParameters().getStringParameter("startDate", ""), 
            getRequestParameters().getStringParameter("endDate", "")).toString(2));
  }
  
  

  
  private static String toJQueryFileTree(String baseDir, File[] files) {
	  StringBuilder out = new StringBuilder();
      out.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
      
      for (File file : files) {
          if (file.isDirectory()) {
              out.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "/\">"+ file.getName() + "</a></li>");
          }
      }
      
      for (File file : files) {
          if (!file.isDirectory()) {
              int dotIndex = file.getName().lastIndexOf('.');
              String ext = dotIndex > 0 ? file.getName().substring(dotIndex + 1) : "";
              out.append("<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "\">"+ file.getName() + "</a></li>");
          }
      }
      out.append("</ul>");
      return out.toString();
	}
    
  
  private IFileRepository getRepository() {
    String repositoryClass = new CfrPluginSettings().getRepositoryClass();
    try {
      return (IFileRepository)Class.forName(repositoryClass).newInstance();    
    } catch (ClassNotFoundException cnfe) {
      logger.fatal("Class for repository " + repositoryClass + " not found. CFR will not be available", cnfe);
    } catch (InstantiationException ie) {
      logger.fatal("Instantiaton of class failed", ie);
    } catch (IllegalAccessException iae) {
      logger.fatal("Illegal access to repository class", iae);
    }
    return null;
  }
  

  private void redirectToCdeEditor(OutputStream out, Map<String, Object> params) throws IOException {

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append("../pentaho-cdf-dd/edit");
    if (params.size() > 0) {
      urlBuilder.append("?");
    }

    List<String> paramArray = new ArrayList<String>();
    for (String key : params.keySet()) {
      Object value = params.get(key);
      if (value instanceof String) {
        paramArray.add(key + "=" + URLEncoder.encode((String) value, getEncoding()));
      }
    }

    urlBuilder.append(StringUtils.join(paramArray, "&"));
    redirect(urlBuilder.toString());
  }

}
