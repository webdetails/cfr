package pt.webdetails.cfr.auth;

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilePermissionMetadata {

  private String file;

  // username or groupname
  private String id;

  private Set<FilePermissionEnum> permissions;
  
  public static Set<FilePermissionEnum> DEFAULT_PERMISSIONS;
  static {
    DEFAULT_PERMISSIONS = new TreeSet<FilePermissionEnum>();
    DEFAULT_PERMISSIONS.add(FilePermissionEnum.READ);
  }

  /**
   * 
   * @param file Full path of the file
   * @param id Username or Group name to associate the default permissions
   */
  public FilePermissionMetadata(String file, String id) {
    this(file, id, DEFAULT_PERMISSIONS);
  }

  /**
   * 
   * @param file
   * @param id
   * @param permissions
   */
  public FilePermissionMetadata(String file, String id, Set<FilePermissionEnum> permissions) {
    this.file = file;
    this.id = id;
    this.permissions = permissions;
  }

  /**
   * @return the file
   */
  public String getFile() {
    return file;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the permissions
   */
  public Set<FilePermissionEnum> getPermissions() {
    return permissions;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FilePermissionMetadata [");
    if (file != null) {
      builder.append("file=");
      builder.append(file);
      builder.append(", ");
    }
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (permissions != null) {
      builder.append("permissions=");
      builder.append(permissions);
    }
    builder.append("]");
    return builder.toString();
  }

  public JSONObject toJson() throws JSONException {
    JSONObject obj = new JSONObject();

    obj.put("file", this.file);
    obj.put("id", id);

    JSONArray array = new JSONArray();
    for (FilePermissionEnum permission : permissions) {
      array.put(permission.getId());
    }
    obj.put("permissions", array);

    return obj;
  }

  public static FilePermissionMetadata fromJson(JSONObject obj) throws JSONException {
    String file = obj.getString("file");
    String id = obj.getString("id");
    
    Object _permissions = obj.get("permissions");
    Set<FilePermissionEnum> permissions = new TreeSet<FilePermissionEnum>();
    if (_permissions instanceof JSONArray) {
      JSONArray permissionsArray = (JSONArray) _permissions;
      for (int i = 0; i < permissionsArray.length(); i++) {
        FilePermissionEnum permission = FilePermissionEnum.resolve(permissionsArray.getString(i));
        if (permission != null) {
          permissions.add(permission);
        }
      }
    }
    
    return new FilePermissionMetadata(file, id, permissions);
  }

}
