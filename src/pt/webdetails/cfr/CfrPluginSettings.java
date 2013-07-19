/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;


public class CfrPluginSettings extends PluginSettings {

    public CfrPluginSettings(){
        super();
        setRepository(PentahoRepositoryAccess.getRepository());
    }
    
  @Override
  public String getPluginName() {
    return "cfr";
  }
 
  public String getRepositoryClass() {
    return getStringSetting("repositoryClass", "pt.webdetails.cfr.repository.DefaultFileRepository");
  }

  public String getBasePath() {
    return getStringSetting("basePath", PentahoSystem.getApplicationContext().getSolutionPath("/system/.cfr"));
  }
  
  
  
}
