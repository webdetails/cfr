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


var CurrentVersionComponent = BaseComponent.extend({
  ph: null,


  update : function(){
    var self = this;
    this.ph = $('#' + this.htmlObject).empty();
    $.get(this.versionUrl, function(result){
      var msgHolder = $('<div/>').html(result);
      self.ph.append(msgHolder);
    });
  }
  
});

