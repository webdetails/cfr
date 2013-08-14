/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cfr.auth.FilePermissionMetadata;
import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.FileStorer;
import pt.webdetails.cfr.file.IFile;
import pt.webdetails.cfr.file.MetadataReader;
import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.InvalidOperationException;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.utils.PluginUtils;

/**
 *
 * @author pdpi
 */
public class CfrContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;

  private static Map<String, Method> exposedMethods = new HashMap<String, Method>();

  private CfrService service = new CfrService();

  private MetadataReader metadata = new MetadataReader();

  static {
    //to keep case-insensitive methods
    exposedMethods = getExposedMethods(CfrContentGenerator.class, true);
  }

  static String checkRelativePathSanity(String path) {
    String result = path;

    if (path != null) {
      if (result.startsWith("./")) {
        result = result.replaceFirst("./", "");
      }
      if (result.startsWith(".")) {
        result = result.replaceFirst(".", "");
      }
      if (result.startsWith("/")) {
        result = result.replaceFirst(".", "");
      }
      
      if (result.endsWith("/")) {
        result = result.substring(0, result.length() - 1);
      }
    }

    return result;
  }

  static String relativeFilePath(String baseDir, String file) {
    String _baseDir = checkRelativePathSanity(baseDir);
    String _file = checkRelativePathSanity(file);
    String result = null;

    if (_baseDir == null || _baseDir.length() == 0) {
      return _file;
    } else {
      if (_baseDir.endsWith("/")) {
        result = new StringBuilder(_baseDir).append(_file).toString();
      } else {
        result = new StringBuilder(_baseDir).append('/').append(_file).toString();
      }
    }

    return result;
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
  public void home(OutputStream out) throws IOException {

    IParameterProvider requestParams = getRequestParameters();
    //    IParameterProvider pathParams = getPathParameters();
    ServletRequest wrapper = getRequest();
    String root = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("solution", "system");
    params.put("path", "cfr/presentation/");
    params.put("file", "cfr.wcdf");
    params.put("absolute", "true");
    params.put("inferScheme", "false");
    params.put("root", root);

    //add request parameters
    PluginUtils.copyParametersFromProvider(params, WrapperUtils.wrapParamProvider(requestParams));

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
  public void createFolder(OutputStream out) throws Exception {
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter("path", ""));

    if (path == null || StringUtils.isBlank(path))
      throw new Exception("path is null or empty");

    boolean createResult = service.getRepository().createFolder(path);
    writeOut(out, "{result: " + createResult + "}");
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void remove(OutputStream out) throws Exception {
    String fullFileName = checkRelativePathSanity(getRequestParameters().getStringParameter("fileName", null));

    if (fullFileName == null || StringUtils.isBlank(fullFileName))
      throw new Exception("fileName is null or empty");

    boolean createResult = service.getRepository().deleteFile(fullFileName);
    if (createResult) {
      FileStorer.deletePermissions(fullFileName, null);
    }
    writeOut(out, "{result: " + createResult + "}");
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void store(OutputStream out) throws IOException, InvalidOperationException, Exception {

    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List /* FileItem */items = upload.parseRequest(getRequest());

    String fileName = null, savePath = null;
    byte[] contents = null;
    for (int i = 0; i < items.size(); i++) {
      FileItem fi = (FileItem) items.get(i);

      if ("path".equals(fi.getFieldName()))
        savePath = fi.getString();
      if ("file".equals(fi.getFieldName())) {
        contents = fi.get();
        fileName = fi.getName();
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

    FileStorer fileStorer = new FileStorer(service.getRepository());

    boolean result = fileStorer.storeFile(checkRelativePathSanity(fileName), checkRelativePathSanity(savePath),
        contents, userSession.getName());
    writeOut(out, "{result: " + result + "}");
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listFiles(OutputStream out) throws IOException {
    String baseDir = URLDecoder.decode(getRequestParameters().getStringParameter("dir", ""), "ISO-8859-1");
    IFile[] files = service.getRepository().listFiles(baseDir);
    List<IFile> allowedFiles = new ArrayList<IFile>(files.length);
    String extensions = getRequestParameters().getStringParameter("fileExtensions", "");
    MetadataReader mr = new MetadataReader();

    // checks permissions
    /*
     * remarks: ideally the repository must list only
     * the files that the current user is allowed to access?
     */
    for (IFile file : files) {
      String relativePath = relativeFilePath(baseDir, file.getName());
      if (mr.isCurrentUserAllowed(FilePermissionEnum.READ, relativePath)) {
        allowedFiles.add(file);
      }
    }

    String[] exts = null;
    if (!StringUtils.isBlank(extensions)) {
      exts = extensions.split(" ");
    }

    IFile[] allowedFilesArray = new IFile[allowedFiles.size()];
    writeOut(out, toJQueryFileTree(baseDir, allowedFiles.toArray(allowedFilesArray), exts));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getFile(OutputStream out) throws IOException, JSONException, Exception {
    String fullFileName = checkRelativePathSanity(getRequestParameters().getStringParameter("fileName", null));

    MetadataReader mr = new MetadataReader();

    if (fullFileName == null) {
      logger.error("request query parameter fileName must not be null");
      throw new Exception("request query parameter fileName must not be null");
    }

    if (mr.isCurrentUserAllowed(FilePermissionEnum.READ, fullFileName)) {
      CfrFile file = service.getRepository().getFile(fullFileName);

      setResponseHeaders(getMimeType(file.getFileName()), -1, file.getFileName());
      ByteArrayInputStream bais = new ByteArrayInputStream(file.getContent());
      IOUtils.copy(bais, out);
      IOUtils.closeQuietly(bais);
    } else {
      getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED, "you don't have permissions to access the file");
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listFilesJSON(OutputStream out) throws IOException, JSONException {
    String baseDir = checkRelativePathSanity(getRequestParameters().getStringParameter("dir", ""));
    IFile[] files = service.getRepository().listFiles(baseDir);
    MetadataReader mr = new MetadataReader();
    JSONArray arr = new JSONArray();
    if (files != null) {
      for (IFile file : files) {
        if (mr.isCurrentUserAllowed(FilePermissionEnum.READ, relativeFilePath(baseDir, file.getName()))) {
          JSONObject obj = new JSONObject();
          obj.put("fileName", file.getName());
          obj.put("isDirectory", file.isDirectory());
          obj.put("path", baseDir);
          arr.put(obj);
        }
      }
    }

    writeOut(out, arr.toString(2));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listUploads(OutputStream out) throws IOException, JSONException {
    MetadataReader reader = new MetadataReader();
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter("fileName", ""));
    writeOut(out, reader.listFiles(path, getRequestParameters().getStringParameter("user", ""), getRequestParameters()
        .getStringParameter("startDate", ""), getRequestParameters().getStringParameter("endDate", "")));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void listUploadsFlat(OutputStream out) throws IOException, JSONException {
    MetadataReader reader = new MetadataReader();
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter("fileName", ""));
    writeOut(
        out,
        reader.listFilesFlat(path, getRequestParameters().getStringParameter("user", ""),
            getRequestParameters().getStringParameter("startDate", ""),
            getRequestParameters().getStringParameter("endDate", "")).toString(2));
  }

  private static String toJQueryFileTree(String baseDir, IFile[] files, String[] extensions) {
    StringBuilder out = new StringBuilder();
    out.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");

    for (IFile file : files) {
      if (file.isDirectory()) {
        out.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "/\">"
            + file.getName() + "</a></li>");
      }
    }

    for (IFile file : files) {
      if (!file.isDirectory()) {
        int dotIndex = file.getName().lastIndexOf('.');
        String ext = dotIndex > 0 ? file.getName().substring(dotIndex + 1) : "";
        boolean accepted = ext.equals("");
        if (!ext.equals("")) {
          if (extensions == null || extensions.length == 0)
            accepted = true;
          else {
            for (String acceptedExtension : extensions) {
              if (ext.equals(acceptedExtension)) {
                accepted = true;
                break;
              }
            }
          }
        }
        if (accepted)
          out.append("<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + baseDir + file.getName() + "\">"
              + file.getName() + "</a></li>");
      }
    }
    out.append("</ul>");
    return out.toString();
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

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void setPermissions(OutputStream out) throws JSONException, IOException {
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter(pathParameterPath, null));
    String[] userOrGroupId = getRequestParameters()
        .getStringArrayParameter(pathParameterGroupOrUserId, new String[] {});
    String[] _permissions = getRequestParameters().getStringArrayParameter(pathParameterPermission,
        new String[] { FilePermissionEnum.READ.getId() });

    JSONObject result = new JSONObject();
    if (path != null && userOrGroupId.length > 0 && _permissions.length > 0) {
      // build valid permissions set
      Set<FilePermissionEnum> validPermissions = new TreeSet<FilePermissionEnum>();
      for (String permission : _permissions) {
        FilePermissionEnum perm = FilePermissionEnum.resolve(permission);
        if (perm != null) {
          validPermissions.add(perm);
        }
      }
      JSONArray permissionAddResultArray = new JSONArray();
      for (String id : userOrGroupId) {
        JSONObject individualResult = new JSONObject();
        boolean storeResult = FileStorer.storeFilePermissions(new FilePermissionMetadata(path, id, validPermissions));
        if (storeResult)
          individualResult.put("status", String.format("Added permission for path %s and user/role %s", path, id));
        else
          individualResult.put("status", String.format("Failed to add permission for path %s and user/role %s", path, id));
        permissionAddResultArray.put(individualResult);
      }
      result.put("status", "Operation finished. Check statusArray for details.");
      result.put("statusArray", permissionAddResultArray);
    } else
      result.put("status", "Path or user group parameters not found");
    
    writeOut(out, result.toString(2));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void deletePermissions(OutputStream out) throws JSONException, IOException {
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter(pathParameterPath, null));
    String[] userOrGroupId = getRequestParameters()
        .getStringArrayParameter(pathParameterGroupOrUserId, new String[] {});

    JSONObject result = new JSONObject();

    
    if (path != null || (userOrGroupId != null && userOrGroupId.length > 0)) {
      if (userOrGroupId == null || userOrGroupId.length == 0) {
        if (FileStorer.deletePermissions(path, null))
          result.put("status", "Permissions deleted");
        else
          result.put("status", "Error deleting permissions");
      } else {
        JSONArray permissionDeleteResultArray = new JSONArray();
        for (String id : userOrGroupId) {
          JSONObject individualResult = new JSONObject();
          boolean deleteResult = FileStorer.deletePermissions(path, id);
          if (deleteResult) 
            individualResult.put("status", String.format("Permission for %s and path %s deleted.", id, path));
          else
            individualResult.put("status", String.format("Failed to delete permission for %s and path %s.", id, path));            
          
          permissionDeleteResultArray.put(individualResult);
        }
        result.put("status", "Multiple permission deletion. Check Status array");
        result.put("statusArray", permissionDeleteResultArray);
      }
    } else
          result.put("status", "Required arguments user/role and path not found");
    
    writeOut(out, result.toString(2));
    
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
  public void getPermissions(OutputStream out) throws IOException, JSONException {
    String path = checkRelativePathSanity(getRequestParameters().getStringParameter(pathParameterPath, null));
    String id = getRequestParameters().getStringParameter(pathParameterGroupOrUserId, null);
    if (path != null || id != null) {
      JSONArray permissions = metadata.getPermissions(path, id, FilePermissionMetadata.DEFAULT_PERMISSIONS);
      writeOut(out, permissions.toString(0));
    }
  }

  @Exposed(accessLevel = AccessLevel.ADMIN)
  public void resetRepository(OutputStream out) {
    PersistenceEngine.getInstance().dropClass(FileStorer.FILE_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().initializeClass(FileStorer.FILE_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().dropClass(FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS);
    PersistenceEngine.getInstance().initializeClass(FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS);

    IFileRepository repo = new CfrService().getRepository();
    for (IFile file : repo.listFiles("")) {
      repo.deleteFile(file.getFullPath());
    }

  }

  private static final String pathParameterGroupOrUserId = "id";

  private static final String pathParameterPath = "path";

  private static final String pathParameterPermission = "permission";

}
