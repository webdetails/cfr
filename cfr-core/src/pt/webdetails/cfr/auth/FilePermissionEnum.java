/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2002 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cfr.auth;

public enum FilePermissionEnum {

  READ( "read" ), WRITE( "write" ), DELETE( "delete" );

  private String id;

  private String description;

  private FilePermissionEnum( String id ) {
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

  public static FilePermissionEnum resolve( String permission ) {
    FilePermissionEnum result = null;

    if ( permission != null ) {
      for ( FilePermissionEnum value : FilePermissionEnum.values() ) {
        if ( permission.equalsIgnoreCase( value.getId() ) ) {
          result = value;
          break;
        }
      }
    }

    return result;
  }
}
