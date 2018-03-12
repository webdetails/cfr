//define('cdf/Dashboard.Bootstrap',function(Dashboard){

 // var dashboard = new Dashboard;
define(function(){
  var Endpoints = {

    //The webAppPath is defined at the start of Dashboards.js
    getWebappBasePath: function () {
      return CONTEXT_PATH;
    },

    getFile: function () {
     // return dashboard.getWebAppPath() + "/plugin/cfr/api/getFile";
      return this.getWebappBasePath() + "plugin/cfr/api/getFile";
    },

    getListFiles: function () {
      return this.getWebappBasePath() + "plugin/cfr/api/listFiles";
    },

    getStore: function(suffix) {
      return this.getWebappBasePath() + "plugin/cfr/api/store" + (suffix ? suffix : "");
    }
  };

  return Endpoints;
});