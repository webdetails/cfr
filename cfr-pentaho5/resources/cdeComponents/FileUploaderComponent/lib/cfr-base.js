var Endpoints = {
	
    //The webAppPath is defined at the start of Dashboards.js
    getWebappBasePath: function () {
        return webAppPath;
    },

	getListFiles: function () {
		return this.getWebappBasePath() + "/plugin/cfr/api/listFiles";
	},
	getStore: function() {
		return this.getWebappBasePath() + "/plugin/cfr/api/store";
	}
};