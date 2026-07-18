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

import org.apache.commons.io.IOUtils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;


public class InterPluginBroker {

  public static void run( Map<String, Object> params, OutputStream out ) throws Exception {
    CallParameters parameters = new CallParameters();
    Iterator<String> it = params.keySet().iterator();
    while ( it.hasNext() ) {
      String key = it.next();
      Object value = params.get( key );
      parameters.put( key, value.toString() );
    }
    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDE.getId(), "renderApi", "render" );
    String returnVal = pluginCall.call( parameters.getParameters() );
    IOUtils.write( returnVal, out );
    out.flush();
  }

}
