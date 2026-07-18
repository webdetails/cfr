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
    return SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }

  public List<String> getUserRoles() {
    PentahoSession sessionInfo = new PentahoSession( PentahoSessionHolder.getSession() );
    return Arrays.asList( sessionInfo.getAuthorities() );
  }

}
