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

  protected static final Log logger = LogFactory.getLog(CfrService.class);

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

  public String getCurrentUserName() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    return session.getName();
  }

  public boolean isCurrentUserAdmin() {
    return SecurityHelper.isPentahoAdministrator(PentahoSessionHolder.getSession());
  }

  public List<String> getUserRoles() {
    PentahoSession sessionInfo = new PentahoSession(PentahoSessionHolder.getSession());
    return Arrays.asList(sessionInfo.getAuthorities());
  }

}
