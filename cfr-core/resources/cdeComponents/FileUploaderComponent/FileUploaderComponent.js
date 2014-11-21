'use strict';

var FileUploaderComponent = BaseComponent.extend({
  update: function() {
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
    var $uploadDialogContainer = $('<div id="uploaderPopupContainer" class="content"></div>');
    var $uploadDialogHeader = $('<div>').addClass('popupHeader');
    var $uploadDialogBody = $('<div>').addClass('popupBody');
    var $uploadDialogFooter = $('<div>').addClass('popupFooter');

    // dialog header
    var $uploadPopupCloseButton = $('<button class="popupButton">&times;</button>');
    $uploadDialogHeader.append($uploadPopupCloseButton);
    var $uploadDialogHeaderTitle = $('<h4>Uploading File</h4>');
    $uploadDialogHeader.append($uploadDialogHeaderTitle);
    $uploadDialogContainer.append($uploadDialogHeader);


    // upload progress bar
    var $uploadProgressBarContainer = $('<div>').addClass('uploadBar');
    var $uploadProgressBar = $('<div class="uploadProgress" ></div>');

    $uploadProgressBarContainer.append($uploadProgressBar);
    $uploadDialogBody.append($uploadProgressBarContainer);

    // upload alert section
    var $uploadAlerts = $('<div class="hide" ></div>');
    $uploadDialogBody.append($uploadAlerts);

    $uploadDialogContainer.append($uploadDialogBody);

    var $uploadDialogDismissButton = $('<button type="button" class="popupButton" disabled>OK</button>');

    $uploadDialogFooter.append($uploadDialogDismissButton);

    $uploadDialogContainer.append($uploadDialogFooter);

    var $popupContentContainer = $('<div class="hide" ></div>');
    $popupContentContainer.append($uploadDialogContainer);
    $ph.append($popupContentContainer);

    // END: initialize upload dialog container

    var popup = new PopupComponent();
    popup.htmlObject = "uploaderPopupContainer";
    popup.draggable = false;
    popup.closeOnClickOutside = true;
    popup.update();
    popup.ph.addClass('uploadPopup');
    popup.ph.find(".close").remove();

    $uploadPopupCloseButton.click(function(event) {
      popup.hide();
    });
    $uploadDialogDismissButton.click(function(event) {
      popup.hide();
    });

    $fileSelect.append($selectFile)
      .append($fileObj).append($cancelButtonDiv);

    var $fileSelectTitle = $('<div>').text('Select File').addClass('select');
    var $uploadForm = $('<form action="' + Endpoints.getStore() + '" method="post" enctype="multipart/form-data">');

    var resetUploadForm = function() {
      $cancelButton.click();
    };

    var fileUploadedCallback = function(response) {
      if (response.result) {

        fileUploadProgressCallback(null, 0, 0, 100);

        // update alert
        $uploadAlerts.attr('class', 'alert success').text('File successfully uploaded');

        // activate upload dialog OK button
        $uploadDialogDismissButton.enable();

        //reset upload progress bar
        //fileUploadProgressCallback(null, 0, 0, 0);

        fireUploaded();
        resetUploadForm();
      } else {
        fileUploadErrorCallback()
      }
    };

    var fileUploadErrorCallback = function() {
      // update alert
      $uploadAlerts.attr('class', 'alert error').text('Error uploading file');
      // activate upload dialog OK button
      $uploadDialogDismissButton.enable();
    };

    var fileUploadProgressCallback = function(event, position, total, percent) {
      $uploadProgressBar.width(percent +"%");
    };

    var fileUploadBeforeSubmitCallback = function(arr, form, options) {
      // reset upload bar
      fileUploadProgressCallback(null, 0, 0, 0);

      // reset alerts
      $uploadAlerts.attr('class', 'hide').text('');

      // show upload dialog box
      popup.popup($ph);
      popup.ph.attr("style", "top: 15px; left:10px; right:10px; position:fixed;");
    };

    var fireUploaded = function(filename) {
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
      $fileInput = $('<input type="file" class="file" name="file"/>'),
      $pathInput = $('<input type="hidden" name="path" value="' + root + '"/>'),
      $submitInput = $('<button type="submit">').addClass('submitBtn').addClass('hide').text('Upload File');

    // bind click event of file name div to input file selector
    $fileSelectTitle.click(function() {
      $fileInput.click();
    });

    $uploadForm.append($label).append($pathInput).append($submitInput);
    $label.append($fileInput);
    $fileInput.attr("id", this.htmlObject + "_file");
    $fileInput.change(function() {
      if ($fileInput.val() !== "") {
        $selectFile.addClass('zeroHeight');
        $label.addClass('hide');
        $fileSelectTitle.addClass('hide');
        $fileObj.removeClass('hide');
        $submitInput.removeClass('hide');
        $cancelButton.removeClass('hide');

        var a = $fileInput.val();
        if (a.slice(3, 11) == "fakepath") a = a.slice(12, a.length);
        $fileObj.html(a);
      }
    });


    var $cancelButton = $('<button>').text('Cancel');
    $cancelButton.click(function() {
      $selectFile.removeClass('zeroHeight');
      $label.removeClass('hide');
      $fileSelectTitle.removeClass('hide');
      $fileObj.addClass('hide');
      $fileObj.html('');
      $submitInput.addClass('hide');
      $fileInput.val('');
      $cancelButton.addClass('hide');
    });

    $cancelButtonDiv.append($cancelButton);
    $cancelButton.addClass('hide');
  }
});