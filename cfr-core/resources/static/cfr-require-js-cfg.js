/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

/**
 * RequireJS configuration file for sparkl
 */

(function() {

  if(!requireCfg.map) requireCfg.map = {};
  if(!requireCfg.map['*']) requireCfg.map['*'] = {};
  

  var requirePaths = requireCfg.paths,
    requireShims = requireCfg.shim,
    requireConfig = requireCfg.config;

  if(!requireConfig['amd']) {
    requireConfig['amd'] = {};
  }
  if(!requireConfig['amd']['shim']) {
    requireConfig['amd']['shim'] = {};
  }
  var amdShim = requireConfig['amd']['shim'];


  var prefix;
  if(typeof CONTEXT_PATH !== "undefined") { // production
    prefix = requirePaths['cfr']  = CONTEXT_PATH + 'api/repos/cfr';

  } else if(typeof FULL_QUALIFIED_URL != "undefined") { // embedded production
    prefix = requirePaths['cfr']  = FULL_QUALIFIED_URL + 'api/repos/cfr';
  } 

  requirePaths['cfr/components'] = prefix + '/cdeComponents';

  requirePaths['cfr/components/FileUploaderComponent'] = requirePaths['cfr/components'] + '/FileUploader/FileUploaderComponent';

  requirePaths['cfr/components/FileBrowserComponent'] = requirePaths['cfr/components'] + '/FileBrowser/FileBrowserComponent';

  requirePaths['cfr/components/VersionCheckComponent'] = requirePaths['cfr/components'] + '/VersionCheckerComp/versionChecker';

  requirePaths['cfr/components/CurrentVersionComponent'] = requirePaths['cfr/components'] + '/VersionCheckerComp/currentVersion';



  amdShim['cfr/components/FileBrowser/require/lib/jqueryFileTree/jqueryFileTree'] = {
    exports: 'jQuery',
    deps: {
      'cdf/lib/jquery': 'jQuery',
      'css!cfr/components/FileBrowser/require/lib/jqueryFileTree/jqueryFileTree.css': ''
    }
  };


  amdShim['cfr/components/FileUploader/require/lib/jquery.form'] = {
    exports: 'jQuery',
    deps: {
      'cdf/lib/jquery': 'jQuery'
    }
  };

  amdShim['cfr/components/FileUploader/require/lib/jquery.ie.cors'] = {
    exports: 'jQuery',
    deps: {
      'cdf/lib/jquery': 'jQuery'
    }
  };

  amdShim['cfr/components/FileUploader/require/lib/jQuery.XDomainRequest'] = {
    exports: 'jQuery',
    deps: {
      'cdf/lib/jquery': 'jQuery'
    }
  };




})();
