/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cfr.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

public class CfrConfig extends PluginSettings {
  private static Log logger = LogFactory.getLog( CfrConfig.class );

  public static final String PLUGIN_ID = "cfr";
  public static final String PLUGIN_TITLE = "cfr";
  public static final String PLUGIN_SYSTEM_PATH = PLUGIN_ID + "/";
  public static final String PLUGIN_SOLUTION_PATH = "system/" + PLUGIN_SYSTEM_PATH;

  private static CfrConfig instance;

  private CfrConfig() {
    super();
    setRepository( PentahoRepositoryAccess.getRepository() );
  }

  public static CfrConfig getConfig() {
    if ( instance == null ) {
      instance = new CfrConfig();
    }
    return instance;
  }

  @Override
  public String getPluginName() {
    return "cfr";
  }
}
