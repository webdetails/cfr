var Endpoints = {
	
    //The webAppPath is defined at the start of Dashboards.js
    getWebappBasePath: function () {
        return webAppPath;
    },

	getListFiles: function () {
		return this.getWebappBasePath() + "/content/cfr/listFiles";
	},
	getStore: function() {
		return this.getWebappBasePath() + "/content/cfr/store";
	}
};