'use strict';

var FileUploaderComponent = BaseComponent.extend({
  update: function () {
    var myself = this,
      $ph = $("#" + this.htmlObject),
      root = this.rootFolder.charAt(this.rootFolder.length - 1) === "/" ? this.rootFolder.substring(0, this.rootFolder.length - 1) : this.rootFolder;

    var $fileSelect = $('<div>').addClass('fileSelect');

    $ph.empty();
    $ph.addClass('uploadRow');
    $ph.append($fileSelect);

    var $selectFile = $('<div>').addClass('uploadButton');
    var $fileObj = $('<div>').addClass('hide').addClass('fileTextRow');
    var $cancelButtonDiv = $('<div>').addClass('cancelButton');

    $fileSelect.append($selectFile)
      .append($fileObj).append($cancelButtonDiv);

    var $fileSelectTitle = $('<div>').text('Select File').addClass('select');
    var $uploadForm = $('<form action="../cfr/store" method="post" enctype="multipart/form-data">');

    var resetUploadForm = function () {
      $cancelButton.click();
    };

    var fileUploaded = function (response) {
      if (response.result) {
        alert('File uploaded');
        fireUploaded();
        resetUploadForm();
      } else {
        fileUploadError()
      }
    };

    var fileUploadError = function () {
      alert('Error uploading file');
    };

    var fireUploaded = function (filename) {
      Dashboards.fireChange('uploadedFileParam', '');
    };

    // configure file upload form
    $uploadForm.ajaxForm({
      dataType: 'json',
      success: fileUploaded,
      error: fileUploadError
    });

    $selectFile.append($fileSelectTitle);
    $selectFile.append($uploadForm);

    var $label = $('<label>').addClass('cabinet'),
      $fileInput = $('<input type="file" class="file" name="file" required/>'),
      $pathInput = $('<input type="hidden" name="path" value="' + root + '"/>'),
      $submitInput = $('<button type="submit">').addClass('submitBtn').addClass('hide').text('Upload File');

    // bind click event of file name div to input file selector
    $fileSelectTitle.click(function () {
      $fileInput.click();
    });

    $uploadForm.append($label).append($pathInput).append($submitInput);
    $label.append($fileInput);
    $fileInput.attr("id", this.htmlObject + "_file");
    $fileInput.change(function () {
      if ($fileInput.val() !== "") {
        $selectFile.addClass('zeroHeight');
        $label.addClass('hide');
        $fileSelectTitle.addClass('hide');
        $fileObj.removeClass('hide');
        $submitInput.removeClass('hide');

        var a = $fileInput.val();
        if (a.slice(3, 11) == "fakepath") a = a.slice(12, a.length);
        $fileObj.html(a);
      }
    });


    var $cancelButton = $('<button>').text('Cancel');
    $cancelButton.click(function () {
      $selectFile.removeClass('zeroHeight');
      $label.removeClass('hide');
      $fileSelectTitle.removeClass('hide');
      $fileObj.addClass('hide');
      $fileObj.html('');
      $submitInput.addClass('hide');
      $fileInput.val('');
    });

    $cancelButtonDiv.append($cancelButton);

  }
});
