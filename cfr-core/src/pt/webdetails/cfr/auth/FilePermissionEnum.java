/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
