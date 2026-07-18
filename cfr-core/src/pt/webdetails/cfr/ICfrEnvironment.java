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

import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public interface ICfrEnvironment {

  public IPluginUtils getPluginUtils();

  public IContentAccessFactory getContentAccessFactory();

  public String getPluginName();

  public ISessionUtils getSessionUtils();

  public void reload();

}
