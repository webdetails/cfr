/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cfr.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cfr.CfrEnvironment;
import pt.webdetails.cfr.CfrService;
import pt.webdetails.cfr.auth.FilePermissionEnum;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.messaging.JsonSerializable;
import pt.webdetails.cpf.persistence.PersistenceEngine;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class MetadataReader {

  private static Log logger = LogFactory.getLog( MetadataReader.class );

  private CfrService service;

  public MetadataReader( CfrService service ) {
    this.service = service;
  }

  public List<ODocument> getFileEntities( String file ) throws JSONException {
    String query = String.format( "select * from %s where file = :fileName", FileStorer.FILE_METADATA_STORE_CLASS );
    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "fileName", file );

    return getPersistenceEngine().executeQuery( query, params );
  }

  public JSONArray listFilesFlat( String fileName, String user, String startDate, String endDate ) {
    Map<String, Object> params = new HashMap<String, Object>();
    String query = "select * from " + FileStorer.FILE_METADATA_STORE_CLASS + " ";
    String where = "";
    if ( fileName != null && fileName.length() > 0 ) {
      where = " file = :fileName ";
      params.put( "fileName", fileName );
    }
    if ( user != null && user.length() > 0 ) {
      if ( where.length() > 0 ) {
        where += " and ";
      }
      where += " user = :user";
      params.put( "user", user );
    }

    if ( startDate != null && startDate.length() > 0 ) {
      if ( where.length() > 0 ) {
        where += " and ";
      }
      where += " uploadDate >= :startDate";
      params.put( "startDate", startDate );

    }

    if ( endDate != null && endDate.length() > 0 ) {
      if ( where.length() > 0 ) {
        where += " and ";
      }
      where += " uploadDate <= :endDate";
      params.put( "endDate", endDate );

    }

    if ( where.length() > 0 ) {
      where = " where " + where;
    }

    JSONArray array = new JSONArray();

    for ( ODocument doc : getPersistenceEngine().executeQuery( query + where, params ) ) {
      String file = doc.field( "file", String.class );

      // checks if current user has read permissions on the folder/file
      if ( isCurrentUserAllowed( FilePermissionEnum.READ, file ) ) {
        array.put( getJson( doc ) );
      }
    }

    return array;
  }

  public JsonSerializable listFiles( String fileName, String user, String startDate, String endDate ) {

    return Result.getOK( listFilesFlat( fileName, user, startDate, endDate ) );

  }

  protected PersistenceEngine getPersistenceEngine() {
    return CfrEnvironment.getPersistenceEngine();
  }

  private static JSONObject getJson( ODocument doc ) {
    JSONObject json = new JSONObject();

    for ( String field : doc.fieldNames() ) {
      try {
        Object value = doc.field( field ); // doc.<Object>field(field)
        if ( value instanceof ODocument ) {
          ODocument docVal = (ODocument) value;
          json.put( field, getJson( docVal ) );
        } else if ( value != null ) {
          json.put( field, value );
        }
      } catch ( JSONException e ) {
        logger.error( e );
      }
    }

    return json;
  }

  /**
   * @param path
   * @return
   * @throws JSONException
   */
  public JsonSerializable getPermissions( String path ) throws JSONException {
    String query = "select * from " + FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS + " where file = :fileName";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "fileName", path );

    JSONArray result = new JSONArray();
    for ( ODocument doc : getPersistenceEngine().executeQuery( query, params ) ) {
      result.put( getJson( doc ) );
    }

    return Result.getOK( result );
  }

  /**
   * @param permission Permission to be checked
   * @param path       Folder or file to validate against the specified permission
   * @return true if current user is allowed, false otherwise
   */
  public boolean isCurrentUserAllowed( FilePermissionEnum permission, String path ) {
    if ( service.isCurrentUserAdmin() || isCurrentUserOwner( path ) ) {
      return true;
    }

    List<String> ids = new ArrayList<String>();
    // adds user roles to the list of ids to check permissions with
    ids.addAll( service.getUserRoles() );
    // adds username to the list of ids to check permissions with
    ids.add( service.getCurrentUserName() );

    List<ODocument> permissions = getPermissionEntities( path, ids, new FilePermissionEnum[] { permission } );

    return permissions != null && permissions.size() > 0;
  }

  /**
   * @param path Folder or file from which we want to retrieve the ownership information
   * @return username of the owner if path exists, otherwise an empty string
   */
  public String getOwner( String path ) {
    String result = "";
    final String ownerField = "user";

    if ( path != null ) {
      // build query to retrieve path metadata
      String query = String.format( "select %s from %s where file = :fileName",
          ownerField, FileStorer.FILE_METADATA_STORE_CLASS );
      Map<String, Object> params = new HashMap<String, Object>();
      params.put( "fileName", path );
      List<ODocument> doc = getPersistenceEngine().executeQuery( query, params );

      if ( doc.size() > 0 ) {
        result = doc.get( 0 ).field( ownerField );
      }
    }

    return result;
  }

  /**
   * @param path
   * @return
   */
  public boolean isCurrentUserOwner( String path ) {
    String owner = getOwner( path );
    return owner.equals( service.getCurrentUserName() );
  }

  public List<ODocument> getUniquePermissionEntities( String path, List<String> ids,
                                                      FilePermissionEnum[] allowedPermissions ) {
    return findPermissionsEntities( path, ids, allowedPermissions, false );
  }

  /**
   * @param path
   * @param ids                user/group names
   * @param allowedPermissions
   * @return
   */
  public List<ODocument> getPermissionEntities( String path, List<String> ids,
                                                FilePermissionEnum[] allowedPermissions ) {
    return findPermissionsEntities( path, ids, allowedPermissions, true );
  }

  private List<ODocument> findPermissionsEntities( String path, List<String> ids,
                                                   FilePermissionEnum[] allowedPermissions, boolean lookup ) {
    final Map<String, Object> params = new HashMap<String, Object>();

    String idsQuery = "";
    int idCounter = 1;
    if ( ids != null ) {
      idsQuery += " (";
      for ( String id : ids ) {
        if ( idsQuery.indexOf( "id =" ) > -1 ) {
          idsQuery += " or";
        }
        idsQuery += " id = :id" + idCounter;
        params.put( "id" + idCounter++, id );
      }
      idsQuery += ") ";
    }
    if ( allowedPermissions != null && allowedPermissions.length > 0 ) {
      if ( !StringUtils.isEmpty( idsQuery ) ) {
        idsQuery += "and";
      }
      idsQuery += " permissions in " + toStringArray( allowedPermissions );
    }
    List<ODocument> permissions = findPermissions( path, idsQuery, params, lookup );
    return permissions;
  }

  private List<ODocument> findPermissions( String path, String idsQuery, Map<String, Object> params, boolean lookup ) {
    List<ODocument> permissions = null;
    String query = "select * from " + FileStorer.FILE_PERMISSIONS_METADATA_STORE_CLASS;
    String folder = null;
    if ( path != null ) {
      CfrFile file = service.getRepository().getFile( path );
      if ( file != null ) {
        String filename = file.getFileName();
        int index = path.lastIndexOf( filename );
        if ( index > 0 ) {
          folder = path.substring( 0, index );
          if ( folder.endsWith( "/" ) ) {
            folder = folder.substring( 0, folder.length() - 1 );
          }
        } else {
          folder = "";
        }
      }
    }
    if ( !StringUtils.isEmpty( path ) ) {
      query += " where file = :fileName";
      params.put( "fileName", path );
    }

    permissions = getPersistenceEngine().executeQuery( query, params );
    if ( !lookup || ( permissions.size() > 0 && query.indexOf( "where" ) > -1 ) ) {
      return findPermissionsWithIdsQuery( query, idsQuery, params );
    } else if ( StringUtils.isEmpty( folder ) ) {
      return permissions;
    } else {
      return findPermissions( folder, idsQuery, params, lookup );
    }
  }

  private List<ODocument> findPermissionsWithIdsQuery( String query, String idsQuery, Map<String, Object> params ) {
    if ( query.indexOf( "where" ) > -1 ) {
      query += " and";
    } else {
      query += " where";
    }
    query += idsQuery;
    return getPersistenceEngine().executeQuery( query, params );
  }

  public JSONArray getPermissions( String file, String id, FilePermissionEnum[] allowedPermissions ) {
    JSONArray result = new JSONArray();

    List<String> ids = new ArrayList<String>();
    ids.add( id );
    for ( ODocument doc : getPermissionEntities( file, ids, allowedPermissions ) ) {
      result.put( getJson( doc ) );
    }

    return result;
  }

  private String toStringArray( FilePermissionEnum[] allowedPermissions ) {
    StringBuilder result = new StringBuilder( "[" );
    Set<FilePermissionEnum> permissions = new LinkedHashSet<FilePermissionEnum>( Arrays.asList( allowedPermissions ) );

    for ( FilePermissionEnum elem : permissions ) {
      result.append( "'" ).append( elem.getId() ).append( "'," );
    }

    int index = result.lastIndexOf( "," );
    if ( index > 0 ) {
      result.deleteCharAt( index );
    }

    result.append( "]" );

    return result.toString();
  }

}
