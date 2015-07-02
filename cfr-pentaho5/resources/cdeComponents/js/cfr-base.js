var Endpoints = {

  //The webAppPath is defined at the start of Dashboards.js
  getWebappBasePath: function () {
    return webAppPath;
  },

  getFile: function () {
    return this.getWebappBasePath() + "/plugin/cfr/api/getFile";
  },

  getListFiles: function () {
    return this.getWebappBasePath() + "/plugin/cfr/api/listFiles";
  },

  getStore: function(suffix) {
    return this.getWebappBasePath() + "/plugin/cfr/api/store" + (suffix ? suffix : "");
  }
};