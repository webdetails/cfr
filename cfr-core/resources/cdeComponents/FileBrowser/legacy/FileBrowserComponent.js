
var FileBrowserComponent = BaseComponent.extend({
  update: function(){    
    var myself = this,
        $ph = $("#"+this.htmlObject),
        root = this.rootFolder.charAt(this.rootFolder.length - 1) == "/" ? this.rootFolder : this.rootFolder+"/",
        $content;

    if (!this.fileExtensions)
        this.fileExtensions = "";

    $ph.addClass('fileBrowserComponent');
    if(this.chartDefinition.height != undefined){
      $ph.css('height',this.chartDefinition.height+'px');
    }
    if(this.chartDefinition.width != undefined){
      $ph.css('width',this.chartDefinition.width+'px');
    }

    $ph.css('overflow','auto');



    $ph.fileTree(
      {
        root: root,
        script: myself.buildTreeURL(),
        expandSpeed: 1,
        collapseSpeed: 1,
        multiFolder: true,
        htmlTreeModifier: function(content){
          return myself.modifyTree(content);
        }
      },
      function(){});
  },


  getValue: function() {

  },


  buildTreeURL: function(){
    return Endpoints.getListFiles() + "?fileExtensions=" + this.fileExtensions;
  },

  buildGetURL: function(rel){
    return Endpoints.getFile() + "?fileName=" + rel;
  },

  modifyTree: function(content){
    var myself = this;
    var $content = content;

    if(!$content.hasClass('directory'))
      $content.find('ul').addClass("treeview filetree");

    $content.find('li:last').addClass("last");

    $.each($content.find('li.directory'),function(){
      //get rel from a
      var rel = $(this).find('a').attr('rel');

      $("<div/>").addClass("hitarea expandable-hitarea").attr('rel',rel).prependTo($(this));
    });

    $.each($content.find('li.directory a'), function(){
      $(this).addClass('folder');
    });

    $.each($content.find('li.file'), function(){
      $("<div/>").addClass("file").prependTo($(this));
    });

    $.each($content.find('li.file a'), function(){
      var rel = $(this).attr('rel');
      //$(this).attr({target: '_blank', href  : myself.buildGetURL(rel)});
      $(this).click(function(){
        window.location.assign(myself.buildGetURL(rel),'_blank');
      });
    });


    return $content;
  },

  downloadDataURI :function(options) {
    if(!options) {
      return;
    }
    $.isPlainObject(options) || (options = {data: options});
    if(!$.browser.webkit) {
      location.href = options.data;
    }
    options.filename || (options.filename = "download." + options.data.split(",")[0].split(";")[0].substring(5).split("/")[1]);
    $('<form method="post" action="'+options.url+'" style="display:none"><input type="hidden" name="filename" value="'+options.filename+'"/><input type="hidden" name="data" value="'+options.data+'"/></form>').submit().remove();
  }
}); 
