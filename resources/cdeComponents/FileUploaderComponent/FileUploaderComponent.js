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

    /*
     * initialize upload dialog container
     */
    var $uploadDialogContainer = $('<div>').addClass('modal-content');
    var $uploadDialogParent = $('<div>').addClass('modal fade').append($('<div>').addClass('modal-dialog').append($uploadDialogContainer));
    var $uploadDialogHeader = $('<div>').addClass('modal-header');
    var $uploadDialogBody = $('<div>').addClass('modal-body');
    var $uploadDialogFooter = $('<div>').addClass('modal-footer');

    // dialog header
    $uploadDialogHeader.append($('<button data-dismiss="modal" aria-hidden="true" class="close">&times;</button>'));
    var $uploadDialogHeaderTitle = $('<h4>Uploading File</h4>');
    $uploadDialogHeader.append($uploadDialogHeaderTitle);

    $uploadDialogContainer.append($uploadDialogHeader);


    // upload progress bar
    var $uploadProgressBarContainer = $('<div>').addClass('progress progress-striped active');
    var $uploadProgressBar = $('<div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>');

    $uploadProgressBarContainer.append($uploadProgressBar);
    $uploadDialogBody.append($uploadProgressBarContainer);

    // upload alert section
    var $uploadAlerts = $('<div class="hide" ></div>');
    $uploadDialogBody.append($uploadAlerts);

    $uploadDialogContainer.append($uploadDialogBody);

    // dialog footer
    var $uploadDialogCancelButton = $('<button class="btn btn-warning"></button>');
    $uploadDialogCancelButton.click(function (e){

    });
    var $uploadDialogDismissButton = $('<button type="button" class="btn btn-default" data-dismiss="modal" disabled>OK</button>');
    $uploadDialogFooter.append($uploadDialogDismissButton);

    $uploadDialogContainer.append($uploadDialogFooter);

    $ph.append($uploadDialogParent);
    // END: initialize upload dialog container

    $fileSelect.append($selectFile)
      .append($fileObj).append($cancelButtonDiv);

    var $fileSelectTitle = $('<div>').text('Select File').addClass('select');
    var $uploadForm = $('<form action="../cfr/store" method="post" enctype="multipart/form-data">');

    var resetUploadForm = function () {
      $cancelButton.click();
    };

    var fileUploadedCallback = function (response) {
      if (response.result) {

        fileUploadProgressCallback(null, 0, 0, 100);

        // update alert
        $uploadAlerts.attr('class', 'alert alert-success').text('File successfully uploaded');

        // activate upload dialog OK button
        $uploadDialogDismissButton.enable();

        // dismiss upload dialog
        // $uploadDialogParent.modal('hide');

        //reset upload progress bar
        //fileUploadProgressCallback(null, 0, 0, 0);

        fireUploaded();
        resetUploadForm();
      } else {
        fileUploadErrorCallback()
      }
    };

    var fileUploadErrorCallback = function () {
        // update alert
        $uploadAlerts.attr('class', 'alert alert-danger').text('Error uploading file');
        // activate upload dialog OK button
        $uploadDialogDismissButton.enable();
    };

    var fileUploadProgressCallback = function (event, position, total, percent) {
      $uploadProgressBar.attr('aria-valuenow', percent);
      $uploadProgressBar.attr('style', 'width:' + percent + '%;');
    };

    var fileUploadBeforeSubmitCallback = function (arr, form, options) {
        // reset upload bar
        fileUploadProgressCallback(null, 0, 0, 0);

        // reset alerts
        $uploadAlerts.attr('class', 'hide').text('');

        // show upload dialog box
        $uploadDialogParent.modal();
    };

    var fireUploaded = function (filename) {
      Dashboards.fireChange('uploadedFileParam', '');
    };

    // configure file upload form
    $uploadForm.ajaxForm({
      dataType: 'json',
      beforeSubmit: fileUploadBeforeSubmitCallback,
      success: fileUploadedCallback,
      error: fileUploadErrorCallback,
      uploadProgress: fileUploadProgressCallback
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
