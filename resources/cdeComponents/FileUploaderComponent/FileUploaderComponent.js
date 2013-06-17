
var FileUploaderComponent = BaseComponent.extend({
      
  update: function() {    
    var myself = this,
        $ph = $("#"+this.htmlObject),
        root = this.rootFolder.charAt(this.rootFolder.length - 1) == "/" ? this.rootFolder.substring(0,this.rootFolder.length - 1) : this.rootFolder;

    var $fileSelect = $('<div>').addClass('fileSelect');


    $ph.empty();
    $ph.addClass('uploadRow');
    $ph.append($fileSelect);



    var $selectFile = $('<div>').addClass('uploadButton'),
        $fileObj = $('<div>').addClass('hide').addClass('fileTextRow'),
        $cancelButtonDiv = $('<div>').addClass('cancelButton');

    $fileSelect.append($selectFile)
    .append($fileObj).append($cancelButtonDiv);
    
    var $fileSelectTitle = $('<div>Select File</div>').addClass('select');
    var $uploadForm = $('<form action="../cfr/store" method="post"  enctype="multipart/form-data" target="' + this.htmlObject + '_upload_target">');
    var $targetIFrame = $('<iframe src="#" style="width:0;height:0;border:0px solid #fff;display:none">');
    $targetIFrame.attr("id", this.htmlObject + "_upload_target");
    $targetIFrame.attr("name", this.htmlObject + "_upload_target");    
    $targetIFrame.load(function (myself) {
        var result = window.frames[$targetIFrame.attr("id")].document.body.innerHTML;
        
        //Add success and error handlers here
        if (result == "{result: true}") {
            alert('File uploaded succesfully');
            $cancelButton.click();
            
            // signal that file was uploaded
            Dashboards.fireChange("uploadedFileParam", "");
        } else if (result == "{result: false}") {
            alert('Error uploading. Check server logs');
        }
        
    });

    $selectFile.append($fileSelectTitle).append($uploadForm).append($targetIFrame);
    
    var $label = $('<label class="cabinet">'),
        $fileInput = $('<input type="file" class="file" name="file" required/>'),
        $pathInput = $('<input type="hidden" name="path" value="' + root + '"/>'),
        $submitInput = $('<button type="submit" class="submitBtn">Upload File</button>').addClass('hide');

    // bind click event of file name div to input file selector 
    $fileSelectTitle.click(function() {
        $fileInput.click();
    });

    $uploadForm.append($label).append($pathInput).append($submitInput);
    $label.append($fileInput);
    $fileInput.attr("id", this.htmlObject + "_file");
    $fileInput.change(function () {
        if ( $fileInput.val() !== "" ){
            $selectFile.addClass('zeroHeight');
            $label.addClass('hide');
            $fileSelectTitle.addClass('hide');
            $fileObj.removeClass('hide');
            $submitInput.removeClass('hide');
            
            var a = $fileInput.val();
            if ( a.slice(3,11) == "fakepath" ) a = a.slice(12,a.length);
            $fileObj.html(a);
        }        
    });
   
    
    var $cancelButton = $('<button>Cancel</button>');
    $cancelButtonDiv.append($cancelButton);
    $cancelButton.click (function (){
        $selectFile.removeClass('zeroHeight');
        $label.removeClass('hide');
        $fileSelectTitle.removeClass('hide');
        $fileObj.addClass('hide');
        $submitInput.addClass('hide');
        $fileInput.val('');
    });
    
    
    SI.Files.stylizeAll();

  },


  getValue: function() {
  },

  fireUploaded: function(filename) {
    Dashboards.fireChange(this.parameters, filename);
  }


}); 
