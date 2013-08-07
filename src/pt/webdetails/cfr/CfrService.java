package pt.webdetails.cfr;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cfr.repository.IFileRepository;

import org.pentaho.platform.api.engine.IPentahoSession;
// import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
// import org.pentaho.platform.engine.core.system.PentahoSystem;

public class CfrService {

  static Log logger = LogFactory.getLog(CfrService.class);

  public CfrService() {
  }

  public IFileRepository getRepository() {
    String repositoryClass = new CfrPluginSettings().getRepositoryClass();
    try {
      return (IFileRepository) Class.forName(repositoryClass).newInstance();
    } catch (ClassNotFoundException cnfe) {
      logger.fatal("Class for repository " + repositoryClass + " not found. CFR will not be available", cnfe);
    } catch (InstantiationException ie) {
      logger.fatal("Instantiaton of class failed", ie);
    } catch (IllegalAccessException iae) {
      logger.fatal("Illegal access to repository class", iae);
    }
    return null;
  }

  //  protected IUserDetailsRoleListService getUserDetailsRoleListService() {
  //    return PentahoSystem.get(IUserDetailsRoleListService.class);
  //  }

  public String getUserName() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    return session.getName();
  }

  public List<String> getUserRoles() {
    List<String> roles = Collections.emptyList();

    // TODO: implementation
    //    try {
    //      roles = getUserDetailsRoleListService().getRolesForUser(getUserName());
    //    } catch (Exception e) {
    //      logger.error("unable to determine current user roles", e);
    //    }
    //    IUserRoleDao userRoleDao = PentahoSystem.get(IUserRoleDao.class, "userRoleDaoProxy",
    //        PentahoSessionHolder.getSession());

    return roles;
  }

}
