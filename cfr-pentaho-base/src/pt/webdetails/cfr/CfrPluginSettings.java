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

import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.PluginSettings;

public class CfrPluginSettings extends PluginSettings {
  private static final String BASE_PATH = "";

  public CfrPluginSettings( ) {
    super( CfrEnvironment.getInstance().getContentAccessFactory().getPluginSystemWriter( "" ) );
  }

  public String getBasePath() {
    return getStringSetting( "basePath", PentahoSystem.getApplicationContext().getSolutionPath( BASE_PATH ) );
  }

}
