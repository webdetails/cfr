/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
var Endpoints = {

  //The webAppPath is defined at the start of Dashboards.js
  getWebappBasePath: function () {
    return webAppPath;
  },

  getFile: function() {
    return this.getWebappBasePath() + "/content/cfr/getFile";
  },

  getListFiles: function () {
    return this.getWebappBasePath() + "/content/cfr/listFiles";
  },
  getStore: function(suffix) {
    return this.getWebappBasePath() + "/content/cfr/store" + (suffix ? suffix : "");
  }
};