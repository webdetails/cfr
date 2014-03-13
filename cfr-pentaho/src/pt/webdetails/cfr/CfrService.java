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

package pt.webdetails.cfr;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;

import pt.webdetails.cfr.repository.IFileRepository;
import pt.webdetails.cpf.session.PentahoSession;

public class CfrService {

  protected static final Log logger = LogFactory.getLog( CfrService.class );

  public IFileRepository getRepository() {
    return new CfrEnvironment().getRepository();
  }

  public String getCurrentUserName() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    return session.getName();
  }

  public boolean isCurrentUserAdmin() {
    return SecurityHelper.isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }

  public List<String> getUserRoles() {
    PentahoSession sessionInfo = new PentahoSession( PentahoSessionHolder.getSession() );
    return Arrays.asList( sessionInfo.getAuthorities() );
  }

}
