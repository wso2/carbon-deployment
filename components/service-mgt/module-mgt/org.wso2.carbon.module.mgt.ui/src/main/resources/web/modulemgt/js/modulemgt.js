function validate() {
    if(document.marUpload.marFilename.value  != null) {
        var jarinput = document.marUpload.marFilename.value;
        if (jarinput == '') {
            CARBON.showWarningDialog('Please select required fields to upload a module');
        } else if (jarinput.lastIndexOf(".mar") == -1) {
            CARBON.showWarningDialog('Please select a .mar file');
        } else {
            document.marUpload.submit();
        }
    } else if (document.marUpload.marFilename[0].value != null) {
        var validFileNames = true;
        for (var i=0; i<document.marUpload.marFilename.length; i++) {
            var jarinput = document.marUpload.marFilename[i].value;
            if (jarinput == '') {
                CARBON.showWarningDialog('Please select required fields to upload a module');
                validFileNames = false; break;
            } else if (jarinput.lastIndexOf(".mar") == -1) {
                CARBON.showWarningDialog('Please select a .mar file');
                validFileNames = false; break;
            } 
        }
    }

    if (validFileNames) {
        document.marUpload.submit();
    } else {
        return;
    }
}

function engage(backendURL, moduleId) {
    location.href = "./global_eng_ajaxprocessor.jsp?action=engage&moduleId=" + moduleId;
}

function disengage(backendURL, moduleId) {
    disengage.moduleId = moduleId;
    if (moduleId.indexOf("addressing") != -1) {
        CARBON.showErrorDialog(jsi18n["cant.disengage"]);
        return;
    }
    CARBON.showConfirmationDialog(jsi18n["do.you.want.to.globally.disengage"] + " " + moduleId +
                                  " " + jsi18n["module"] + " ?", disengageCallback, null);
}

function removeModule(moduleId) {
    removeModule.moduleId = moduleId;
    if (moduleId.indexOf("addressing") != -1) {
        CARBON.showErrorDialog(jsi18n["addressing.cant.be.removed"]);
        return;
    }
    CARBON.showConfirmationDialog(jsi18n["do.you.want.to.delete"] + " " + moduleId + " " +
                                  jsi18n["module"] + " ?", removeModuleCallback, null);
}

function removeModuleCallback() {
    location.href = "./remove_modules_ajaxprocessor.jsp?moduleId=" + removeModule.moduleId;
}

function disengageCallback() {
    location.href =
    "./global_eng_ajaxprocessor.jsp?action=disengage&moduleId=" + disengage.moduleId;
}

function restartServerCallback() {
    var url = "../server-admin/proxy_ajaxprocessor.jsp?action=restart";
    jQuery.noConflict();
    jQuery("#workArea").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (jQuery.trim(responseText) != '') {
            CARBON.showWarningDialog(responseText);
            return;
        }
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["restart.error"]);
        } else {
            CARBON.showInfoDialog(jsi18n["restart.in.progress.message"]);
        }
    });
}

function restartServer() {
    jQuery(document).ready(function() {
        CARBON.showConfirmationDialog(jsi18n["restart.message"], restartServerCallback, null);
    });
}

function showModuleUploadedMsg () {
    CARBON.showInfoDialog(jsi18n["module.successfully.uploaded.message"]);
}

var rows = 1;
//add a new row to the table
function addRow() {
    rows++;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("moduleTbl").insertRow(-1);
    newRow.id = 'file' + rows;

    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = '<label>Module Archive (.mar)<font color="red">*</font></label>';
    oCell.className = "formRow";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input type='file' name='marFilename' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file" + rows + "');\" />";
    oCell.className = "formRow";

    alternateTableRows('moduleTbl', 'tableEvenRow', 'tableOddRow');
}

function deleteRow(rowId) {
    var tableRow = document.getElementById(rowId);
    tableRow.parentNode.deleteRow(tableRow.rowIndex);
    alternateTableRows('moduleTbl', 'tableEvenRow', 'tableOddRow');
}
