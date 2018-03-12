
define(['cdf/lib/jquery','./js/cfr-base','cdf/components/BaseComponent','cde/components/PopupComponent','amd!cfr/components/FileUploader/require/lib/jquery.form'],function($,Endpoints,BaseComponent,PopupComponent){
'use strict';

  var FileUploaderComponent = BaseComponent.extend({

    // success callback for the upload
    successCallback: undefined,

    // error callback for the upload
    errorCallback: undefined,

    // function to call prior to upload, returning false here will cancel the upload
    beforeSubmitCallback: undefined,

    update: function() {
      var $ph = $("#" + this.htmlObject),
        root = this.rootFolder.charAt(this.rootFolder.length - 1) === "/" ?
          this.rootFolder.substring(0, this.rootFolder.length - 1) : this.rootFolder;

      var $fileSelect = $('<div></div>').addClass('fileSelect');
      $ph.empty();
      $ph.addClass('fileUploaderComponent');
      $ph.append($fileSelect);

      var $selectFile = $('<div></div>').addClass('uploadButton');
      var $fileObj = $('<div></div>').addClass('hide').addClass('fileTextRow');
      var $cancelButtonDiv = $('<div></div>').addClass('cancelButton');

      /*
       * initialize upload dialog container
       */
      var $uploadDialogContainer = $('<div id="uploaderPopupContainer" class="content"></div>');
      var $uploadDialogHeader = $('<div></div>').addClass('popupHeader');
      var $uploadDialogBody = $('<div></div>').addClass('popupBody');
      var $uploadDialogFooter = $('<div></div>').addClass('popupFooter');

      // dialog header
      var $uploadPopupCloseButton = $('<button class="popupButton">&times;</button>');
      $uploadDialogHeader.append($uploadPopupCloseButton);
      var $uploadDialogHeaderTitle = $('<h4>Uploading File</h4>');
      $uploadDialogHeader.append($uploadDialogHeaderTitle);
      $uploadDialogContainer.append($uploadDialogHeader);


      // upload progress bar
      var $uploadProgressBarContainer = $('<div></div>').addClass('uploadBar');
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
      popup.popupClass = "uploadPopup";
      popup.popupOverlayClass = "fileUploaderComponentOverlay";
      popup.update();
      popup.ph.find(".close").remove();

      $uploadPopupCloseButton.click(function(event) {
        popup.hide();
      });
      $uploadDialogDismissButton.click(function(event) {
        popup.hide();
      });

      var mySelf = this;

      $fileSelect.append($selectFile)
        .append($fileObj).append($cancelButtonDiv);

      var myNav = navigator.userAgent.toLowerCase();
      // will need to know if is IE < 10
      var isIEBelow10 = (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) < 10 : false;
      // IE < 10 can't handle json response type in forms, a special endpoint which returns html was created
      // for this situation. The jquery.form plugin then extracts the json from a textarea element before
      // returning it to the success handler
      var $uploadForm = $('<form id="uploadForm" action="' + Endpoints.getStore(isIEBelow10 ? "IE" : "") + '" method="post" enctype="multipart/form-data"></form>');
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
          fileUploadErrorCallback(response.message);
        }
      };

      var fileUploadErrorCallback = function(message) {
        // update alert
        $uploadAlerts.attr('class', 'alert error').text('Error uploading file' +
          (typeof message === "string" ? " - " + message : ""));
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

      var fireUploaded = function(fileName) {
        mySelf.dashboard.fireChange('uploadedFileParam', '');
      };

      // configure file upload form
      $uploadForm.ajaxForm({
        dataType: 'json',
        beforeSubmit: this.beforeSubmitCallback || fileUploadBeforeSubmitCallback,
        success: this.successCallback || fileUploadedCallback,
        error: this.errorCallback || fileUploadErrorCallback,
        uploadProgress: fileUploadProgressCallback
      });

      $selectFile.append($uploadForm);

      var $label = $('<label>').text('Select File').addClass('select'),
        $fileInput = $('<input type="file" class="file" name="file"/>').css({ position: "absolute", left: "-9999em" }),
        $pathInput = $('<input type="hidden" name="path" value="' + root + '"/>'),
        $submitInput = $('<button type="submit" class="submitBtn hide">Upload File</button>');

      $uploadForm.append($label).append($pathInput).append($submitInput);
      $label.append($fileInput);
      $fileInput.attr("id", this.htmlObject + "_file");
      $fileInput.change(function() {
        if ($fileInput.val() !== "") {
          $selectFile.addClass('zeroHeight');
          $label.addClass('hide');
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

return FileUploaderComponent;
});