/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2014, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
O$.DateChooser = {
  _init: function(dateChooserId,
                  dateFormat,
                  calendarDate,
                  localeStr,
                  valueChangeHandler,
                  formatName) {
    var dc = O$.initComponent(dateChooserId, null, {
      _isDateChooser: true,
      _calendar: O$(dateChooserId + "--popup--calendar"),

      _dateFormat: dateFormat,
      _localeStr: localeStr,

      _valueChangeHandler: valueChangeHandler,

      _delimiter: null,

      _onremove: function(e){
        if (formatName != 'medium') {
          return true;
        }

        var str = dc._field.value;
        var start = dc._field.selectionStart;
        var end = dc._field.selectionEnd;
        var focusPosition = start;

        if (start == end){
          switch (e.keyCode){
            case 8: //backspace
              str = str.substring(0, start - 1) + str.substring(end);
              focusPosition--;
              break;
            case 46: //delete
              str = str.substring(0, start) + str.substring(end + 1);
              break;
          }
        } else {
          str = str.substring(0, start) + str.substring(end);
        }

        if (!O$.DateChooser._isValidDigitDate(str, dc._delimiter, dc._dateFormat)){
          return false;
        }

        dc._field.value = str;
        dc._field.setSelectionRange(focusPosition, focusPosition);

        return false;
      },

      //todo: This is temporary decision. Rewrite logic for the short format using MaskEdit for all formats.
      onkeypress: function(e){
        if (formatName != 'medium') {
          return true;
        }

        var ch = String.fromCharCode(e.charCode);

        if ((ch < "0" || ch > "9") && (ch != dc._delimiter)){
          return false;
        }

        var str = dc._field.value;
        var start = dc._field.selectionStart;
        var end = dc._field.selectionEnd;
        var strStart = str.substring(0, start);
        var strEnd = str.substring(end);
        var enteredStr = strStart + ch + strEnd;
        str = strStart + ch;


        if (!O$.DateChooser._isValidDigitDate(enteredStr, dc._delimiter, dc._dateFormat)){
          return false;
        }
        if (ch == dc._delimiter && (strStart.indexOf(dc._delimiter, strStart.length - dc._delimiter.length) !== -1)){
          return false;
        }

        var focusPosition = start + ch.length;
        var allDelimiters = new RegExp("\\" + dc._delimiter, "g");
        var digit = new RegExp("\\d", "g");

        if (dateFormat.charAt(0) != 'y'){
          if ((!enteredStr.match(allDelimiters) || (enteredStr.match(allDelimiters).length < 2))
              && (str.substring(str.length - 2).match(digit) && str.substring(str.length - 2).match(digit).length == 2)){
            str += dc._delimiter;
            focusPosition += dc._delimiter.length;
          }
        }

        str = str + strEnd;
        dc._field.value = str;
        dc._field.setSelectionRange(focusPosition, focusPosition);

        return false;
      }

    });

    //Initialization for custom date format.
    if (formatName === 'medium'){
      dc._delimiter = O$.DateChooser._findDelimiter(dateFormat);

      var dtf = O$.getDateTimeFormatObject(localeStr);
      if (dtf && calendarDate){
        var sDate = dtf.parse(calendarDate, "dd/MM/yyyy");
        O$.Calendar._setSelectedDate(dc._calendar.id, sDate);
      }
    };

    dc._field._oldValueHolder = dc._initialText;
    dc._field.onchange = function(e) {
      if (!e)
        e = event;
      e.cancelBubble = true;
      // works in IE only
      this._oldValueHolder = null;
      dc.validateInputAndUpdateCalendar();
    };

    if (valueChangeHandler) {
      dc._prevOnchange = dc.onchange;
      eval("dc._invokeChangeHandler = function(event) {" + valueChangeHandler + "}");
      dc.onchange = function(e) {
        if (e && e.target && e.target == dc._field) // works everywhere except IE
          return;
        if (this._prevOnchange)
          this._prevOnchange();
        dc._invokeChangeHandler(e);
      };
    }

    // Sometimes date chooser may be placed into container with "display: none;".
    // We have to catch the moment when container becomes to displayable one and set the date chooser width
    var timeoutID = setTimeout(function() {
      O$.DateChooser._checkCalendarWidth(dc);
    }, 100);

    if (!O$._dateChooserTimerID) {
      O$._dateChooserTimerID = timeoutID;
    }

    dc._showHidePopup = function () {
      O$.DateChooser._changeCalendarVisibility(dc);
    };

    dc._dateChangeListener = function(date) {
      if (dc._isSetDateFromInput) return;
      var popup = dc._popup;
      if (O$.validateById != undefined) {
        if (dc._of_messages) {
          dc._of_messages = undefined;
          O$.updateClientMessages(dc.id);
          O$.updateClientMessagesPosition();
        }
      }
      if (dc._keyDownEvent) {
        var e = dc._keyDownEvent;
        if (e.keyCode == 33 || e.keyCode == 34 || e.keyCode == 38 || e.keyCode == 40 || e.keyCode == 37
                || e.keyCode == 39) {
          dc._keyDownEvent = null;
          return;
        }
        dc._keyDownEvent = null;
      }
      popup.hide();

      try {
        dc._field.focus();
      } catch (e) {
        //IE
      }
      O$.DateChooser._updateDCField(dc, date != null ? new Date(date) : null);
    };

    var eventName = (O$.isSafariOnMac() || O$.isOpera() || O$.isMozillaFF()) ? "onkeypress" : "onkeydown";
    dc._prevKeyHandler_DC = dc[eventName];
    dc[eventName] = function(evt) {
      var e = evt ? evt : window.event;
      dc._keyDownEvent = e;
      switch (e.keyCode) {
        case 40: // down
          if (!popup.isVisible()) {
            dc._showHidePopup();
            O$.cancelEvent(e);
          }
          break;
        case 8: //backspace
          return dc._onremove(e);
        case 46: //delete
          return dc._onremove(e);
      }
      if (dc._prevKeyHandler_DC)
        return dc._prevKeyHandler_DC(evt);
    };

    var cal = dc._calendar;
    cal._prevKeyHandler_DC = cal[eventName];
    cal[eventName] = function(evt) {
      var e = evt ? evt : window.event;
      dc._keyDownEvent = e;
      switch (e.keyCode) {
        case 27: // escape
          dc._popup.hide();
          dc._field.focus();
          dc._keyDownEvent = null;
          return;
        case 13: // enter
          dc._dateChangeListener(O$.Calendar._getSelectedDate(cal.id));
          return false;
      }
      cal._prevKeyHandler_DC(evt);
    };

    cal._addDateChangeListener(dc._dateChangeListener);

    dc.validateInputAndUpdateCalendar = function() {
      var date;
      if (O$.validateById != undefined) {
        var converters = O$.getValidators(dc);
        for (var i = 0; i < converters.length; i ++) {
          var v = converters[i];
          if (v instanceof O$._DateTimeConverterValidator) {
            if (dc._of_messages) {
              var newMessages = [];
              dc._of_messages.forEach(function (msg) {
                if (!msg.validator || !(msg.validator instanceof O$._DateTimeConverterValidator))
                  newMessages.push(msg);
              });
              dc._of_messages = newMessages;
            }

            var result = v.validate(dc);

            if (result && result instanceof Date) {
              date = result;
            }
            if (!result) {
              // show invalid format message, but prevent hiding other messages such as required that might already be visible
              dc.valid = result;
            }
            O$.updateClientMessages(dc.id);
            O$.updateClientMessagesPosition();
            break;
          }
        }
      }
      /*
       var dtf = O$.getDateTimeFormatObject(dc._localeStr);
       if (!dtf) return;
       var date = dtf.parse(field.value, dc._dateFormat, valueHolder.value);
       */

      dc._isSetDateFromInput = true;
      if (O$.trim(dc._field.value).length > 0) {
        if (!date) {
          // this can be the case if clientValidation is set to "off", which prevents validators from being passed to
          // client-side and thus the date assignment above will be skipped
          var dtf = O$.getDateTimeFormatObject(dc._localeStr);
          if (dtf) {
            var dcValue = O$.getValue(dc);
            try {
              date = dtf.parse(dcValue, dc._dateFormat);
            } catch(e) {
              // incorrect string has been entered manually -- treating as an empty date
              date = null;
            }
          }
        }
        O$.Calendar._setSelectedDate(dc._calendar.id, date);
      } else
        O$.Calendar._noneClick(dc._calendar);
      dc._isSetDateFromInput = false;
      if (dc._field._oldValueHolder != dc._field.value) {
        if (!dc._updatingField && dc.onchange) {
          O$.sendEvent(dc, "change");
        }
        dc._field._oldValueHolder = dc._field.value;
      }
    };

    // Hide popup default border for date chooser
    var popup = dc._popup;
    popup.style.borderWidth = "0px";

    dc._clientValueFunctionExists = true;
    dc._clientValueFunction = function () {
      return dc._field.value;
    };

    popup._addVisibilityChangeListener(function () {
      var lastHoveredCell = dc._calendar._lastHoveredCell;
      if (lastHoveredCell)
        lastHoveredCell.onmouseout();
    });

    // Related to JSFC-2042. Adjust date for calendar inner component.
    if (calendarDate) {
      cal._valueDateHolder.value = calendarDate;
      var dtf = O$.getDateTimeFormatObject(cal._localeStr);
      if (!dtf) return;
      var sDate = dtf.parse(calendarDate, "dd/MM/yyyy");
      cal._valueHolder.value = sDate.getTime();
      O$.Calendar._updateCalendar(cal, sDate);
    }

    dc.getSelectedDate = function () {
      return O$.DateChooser._getDate(dc.id);
    };

    dc.setSelectedDate = function (date) {
      O$.DateChooser._setDate(dc.id, date);
    };
  },

  _changeCalendarVisibility: function(dc) {
    var popup = dc._popup;
    var calendar = dc._calendar;
    if (popup.isVisible()) {
      popup.hide();
      dc._field.focus();
    } else {
      dc.validateInputAndUpdateCalendar();
      popup._prepareForRearrangementBeforeShowing();
      O$.DropDown._initPopup(dc, calendar);
      O$.correctElementZIndex(popup, dc);
      popup.show();
      dc._calendar.focus();
    }
    if (O$.isOpera()) {
      var body = document.getElementsByTagName("body")[0];
      body.style.visibility = "hidden";
      body.style.visibility = "visible";
    }
  },

  _updateDCField: function(dc, date) {
    var field = dc._field;
    if (date) {
      var dtf = O$.getDateTimeFormatObject(dc._localeStr);
      if (!dtf) return;
      var sDate = dtf.format(date, dc._dateFormat);
      dc._updatingField = true;
      if (field.value != sDate) {
        try {
          field.value = sDate;
          field._oldValueHolder = field.value;
        } finally {
          dc._updatingField = false;
        }
        if (dc.onchange)
          O$.sendEvent(dc, "change");
      }
    } else {
      if (field.value != "") {
        dc._updatingField = true;
        try {
          field.value = "";
          field._oldValueHolder = field.value;
        } finally {
          dc._updatingField = false;
        }
        if (dc.onchange)
          O$.sendEvent(dc, "change");
      }

    }
    if (O$.isOpera()) {
      var body = document.getElementsByTagName("body")[0];
      body.style.visibility = "hidden";
      body.style.visibility = "visible";
    }
  },

  _getDate: function(dcId) {
    var dc = O$(dcId);
    return O$.Calendar._getSelectedDate(dc._calendar.id);
  },

  _setDate: function(dcId, date) {
    var dc = O$(dcId);
    O$.Calendar._setSelectedDate(dc._calendar.id, date);
  },

  _checkCalendarWidth: function(dc) {
    var isElementInDocument = O$.isElementPresentInDocument(dc);
    if (!isElementInDocument) {
      clearTimeout(O$._dateChooserTimerID);
      return;
    }
    var popup = dc._popup;
    var popupStyle = O$.getElementStyle(popup, ["display"]);
    if (popupStyle.display == "none") {
      // calendar cannot be measured if its container has the display property value of "hidden", so we're replacing
      // it with visibility="hidden" temporarily. Note that display="none" is used for hiding the calendar for the rest
      // of the component's lifetime instead of visibility="hidden" because using visibility for this purpose reserves
      // the space for this hidden element which might result in unwanted scrollers on the page where the DateChooser
      // is used
      popup.style.visibility = "hidden";
      popup.style.display = "block";
    }
    var cal = dc._calendar;
    var calendarWidth = cal.offsetWidth;
    if (calendarWidth != 0) {
      if (popup.style.visibility == "hidden") {
        // revert the visibility="hidden" declaration that was temporarily used instead of the display="none" one
        // (see above)
        popup.style.display = "none";
        popup.style.visibility = "visible";
      }
      dc.style.width = calendarWidth + "px";
      dc.style.visibility = "visible";
      O$.repaintAreaForOpera(dc, true);
    } else {
      setTimeout(function() {
        O$.DateChooser._checkCalendarWidth(dc);
      }, 100);
    }
  },

  _findDelimiter: function (dateInCurrentLocale){
    var possibleDelimiters = ['/', '-', '.', ',', ' '];
    for (var i = 0; i < dateInCurrentLocale.length; i++){
      for (var delimiterIndex = 0; delimiterIndex < possibleDelimiters.length; delimiterIndex++){
        if (dateInCurrentLocale.charAt(i) === possibleDelimiters[delimiterIndex]){
          return possibleDelimiters[delimiterIndex];
        }
      }
    }
  },

  _evaluateDateFormat: function(dateForFormatting, dateInCurrentLocale, delimiter){
    var day = dateForFormatting.getDate();
    var month = dateForFormatting.getMonth() + 1;
    var year = dateForFormatting.getYear();
    var dateInParts = dateInCurrentLocale.split(delimiter);
    var dateFormatBlank = [];
    var datePart;
    for (var index = 0; index < dateInParts.length; index++){
      datePart = O$.DateChooser._pullDigits(dateInParts[index]);
      switch (datePart){
        case day:
          dateFormatBlank[index] = dateInParts[index].length == 1 ? "d" : "dd";
          break;
        case month:
          dateFormatBlank[index] = dateInParts[index].length == 1 ? "M" : "MM";
          break;
        case year:
          dateFormatBlank[index] = "yy";
          break;
      }
    }

    var localFormat = dateFormatBlank[0];
    for(var index = 1; index < dateFormatBlank.length; index++){
      localFormat += delimiter + dateFormatBlank[index];
    }

    return localFormat;
  },

  _isValidDigitDate: function(date, delimiter, dateFormat){
    var maxLengthOfRepeatedDigitsSequence = 3; //excluding
    var yearFormatLength = new RegExp("y", "g");
    var maxLengthOfDigitsSequence = dateFormat.match(yearFormatLength).length + 1; //excluding
    var repeatedDigitsRestriction = new RegExp("(\\d{" + maxLengthOfRepeatedDigitsSequence + ",})", "g");
    var digitsRestriction = new RegExp("(\\d{" + maxLengthOfDigitsSequence + ",})");
    var allDelimiters = new RegExp("\\" + delimiter, "g");

    if (digitsRestriction.test(date)
        || (date.match(repeatedDigitsRestriction) && date.match(repeatedDigitsRestriction).length > 1)
        || (date.match(allDelimiters) && date.match(allDelimiters).length > 2)){
      return false;
    }

    return true;
  },

  _pullDigits: function(str){
    var result = '';
    for (var i = 0; i < str.length; i++){
      if (str.charAt(i) >= '0' && str.charAt(i) <= '9'){
        result += str.charAt(i);
      }
    }
    return parseInt(result, 10);
  }

};