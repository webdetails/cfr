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