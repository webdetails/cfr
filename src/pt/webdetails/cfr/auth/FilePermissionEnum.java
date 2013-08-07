package pt.webdetails.cfr.auth;

public enum FilePermissionEnum {

  READ("read"), WRITE("write"), DELETE("delete");

  private String id;

  private String description;

  private FilePermissionEnum(String id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  public static FilePermissionEnum resolve(String permission) {
    FilePermissionEnum result = null;

    if (permission != null) {
      for (FilePermissionEnum value : FilePermissionEnum.values()) {
        if (permission.equalsIgnoreCase(value.getId())) {
          result = value;
          break;
        }
      }
    }

    return result;
  }
}
