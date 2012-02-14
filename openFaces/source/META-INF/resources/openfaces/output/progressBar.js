/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2011, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

O$.ProgressBar = {
  _init: function(componentId, value, labelAlignment, labelFormat, uploadedProgressImgUrl, notUploadedProgressImgUrl) {
    var progressBar = O$.initComponent(componentId, null);
    O$.extend(progressBar, {
      _progressValue : 0,
      _uploadedDiv : progressBar.childNodes[0],
      _notUploadedDiv : progressBar.childNodes[1],
      _labelDiv : progressBar.childNodes[2],
      _uploadedProgressImgUrl : uploadedProgressImgUrl,
      _notUploadedProgressImgUrl : notUploadedProgressImgUrl,
      _labelAlignment : labelAlignment,
      _labelFormat: labelFormat,
      _previousValue:0,
      _isBusy:false,
      _queue:[],
      getValue : function() {
        return progressBar._progressValue;
      },
      setValue : function(progressValue) {
        if (progressValue != null &&
                progressValue <= 100 && progressValue >= 0) {
          progressBar._progressValue = progressValue;
          var labelShouldDisplay = !(O$.getElementStyle(progressBar._labelDiv,"display") == "none");
          if (O$.isExplorer6() || O$.isExplorer7() || (O$.isExplorer() && O$.isQuirksMode())) {
              progressBar._labelDiv.style.display = "none";
          }
          progressBar._setLabelValue(progressValue);

          var val = progressValue / 100;//between 0 and 1
          progressBar._smoothChangeValueTo(val);

          if (O$.isExplorer6() || O$.isExplorer7() || (O$.isExplorer() && O$.isQuirksMode())) {
            // weird bug from IE - without this row of code, label will be displayed not inside progressBar
            if (labelShouldDisplay) {
              progressBar._labelDiv.style.marginLeft = progressBar._labelDiv.style.marginLeft;
              progressBar._labelDiv.style.display = "inline";
            }
          }
        }
      },
      _getUploadedProgressImgUrl: function () {
        return progressBar._uploadedProgressImgUrl;
      },
      _getNotUploadedProgressImgUrl: function () {
        return progressBar._notUploadedProgressImgUrl;
      },
      _getLabelFormat:function(){
        return progressBar._labelFormat;
      },
      _getLabelAlignment:function() {
        return progressBar._labelAlignment;
      },
      _setWidthForProgress:function (uploadedWidth, notUploadedWidth){
        progressBar._uploadedDiv.style.width = uploadedWidth + "px";
        progressBar._notUploadedDiv.style.width = notUploadedWidth + "px";
      },
      _setLabelValue:function (value) {
        progressBar._labelDiv.innerHTML = progressBar._labelFormat.replace("{value}", value);
      },
      _smoothChangeValueTo:function (val) {
        function resolveQueue(){
          var valInQueue = progressBar._queue.shift();
          if (valInQueue != null) {
            progressBar._smoothChangeValueTo(valInQueue);
          }
        }
        if (progressBar._isBusy){
          progressBar._queue.push(val);
        }else{
          progressBar._isBusy = true;
          if (progressBar._previousValue < val) { //smooth part
            var TIME_TAKES = 340;
            var INTERVAL = 20;
            var TIMES_TO_CHANGE_PROGRESS = TIME_TAKES / INTERVAL;

            var goalUploadedWidth = progressBar.clientWidth * val;
            var nowUploadedWidth = progressBar._uploadedDiv.clientWidth;

            var addByTime = (goalUploadedWidth - nowUploadedWidth) / TIMES_TO_CHANGE_PROGRESS;
            if (addByTime < 1) {
              addByTime = 1;
            }
            function changeProgress() {
              if (progressBar._uploadedDiv.clientWidth + addByTime >= goalUploadedWidth) {
                progressBar._setWidthForProgress(progressBar.clientWidth * val, progressBar.clientWidth * (1 - val));
                progressBar._setLabelValue(Math.round(val * 100));
                progressBar._isBusy = false;
                resolveQueue();
              } else {
                var uploadedWidth = progressBar._uploadedDiv.clientWidth + addByTime;
                var percentsNow = (uploadedWidth ) / progressBar.clientWidth;
                progressBar._setWidthForProgress(uploadedWidth,
                        (progressBar._notUploadedDiv.clientWidth - addByTime < 0) ? 0
                                : progressBar._notUploadedDiv.clientWidth - addByTime);
                progressBar._setLabelValue(Math.round(percentsNow * 100));
                setTimeout(changeProgress, INTERVAL);
              }
            }

            changeProgress();
          } else {
            progressBar._setWidthForProgress(progressBar.clientWidth * val, progressBar.clientWidth * (1 - val));
            progressBar._setLabelValue(Math.round(val * 100));
            progressBar._isBusy = false;
            resolveQueue();
          }
          progressBar._previousValue = val;
        }
      }
    });
    new function setHeightAndWidthForProgressEls(){
      /*IE 8 doesn't see if we assign height to some percents %*/
      if (O$.isExplorer8() || document.documentMode == 8){
        progressBar._uploadedDiv.style.height  = O$.getElementClientRectangle(progressBar).height + "px";
        progressBar._notUploadedDiv.style.height = O$.getElementClientRectangle(progressBar).height + "px";
      }

    }();

    progressBar.setValue(value);
    if (labelAlignment == "center"){
        progressBar._labelDiv.style.marginLeft = progressBar.clientWidth / 2 - O$.getElementSize(progressBar._labelDiv).width / 2 + "px";
    }
    progressBar._uploadedDiv.style.backgroundImage = "url('" + progressBar._uploadedProgressImgUrl + "')";
    if (progressBar._notUploadedProgressImgUrl != null && progressBar._notUploadedProgressImgUrl != "") {
      progressBar._notUploadedDiv.style.backgroundImage = "url('" + progressBar._notUploadedProgressImgUrl + "')";
    }
  },
  initCopyOf: function (progressBar, copyOfProgressBar) {
    this._init(copyOfProgressBar.id,
            progressBar.getValue(),
            progressBar._getLabelAlignment(),
            progressBar._getLabelFormat(),
            progressBar._getUploadedProgressImgUrl(),
            progressBar._getNotUploadedProgressImgUrl());
  }
};