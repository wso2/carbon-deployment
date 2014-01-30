<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.jaxws.webapp.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="add.webapp"
                       resourceBundle="org.wso2.carbon.jaxws.webapp.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">
        function validate() {

            var validFileNames = true;
            var emptyFields = true;

            if (document.webappUploadForm.warFileName.value != null) {

                var jarinput = document.webappUploadForm.warFileName.value;
                if (jarinput != '') {
                    emptyFields = false;
                }
                if (jarinput != '' && jarinput.lastIndexOf(".war") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                    validFileNames = false;
                } else if(jarinput.indexOf("#") != -1) {
                    CARBON.showWarningDialog('<fmt:message key="unsupported.characters.webapp"/>');
                    validFileNames = false;
                }  else if(validateName(jarinput,true)) {
                    CARBON.showWarningDialog('<fmt:message key="unsupported.characters.webapp"/>');
                    validFileNames = false;
                } else if(jarinput.trim().indexOf(" ") != -1) {
                    CARBON.showWarningDialog('<fmt:message key="whitespace.contains.webapp"/>');
                    validFileNames = false;
                }
            } else if (document.webappUploadForm.warFileName[0].value != null) {
              

                for (var i=0; i<document.webappUploadForm.warFileName.length; i++) {
                    var jarinput = document.webappUploadForm.warFileName[i].value;
                    if (jarinput != '') {
                        emptyFields = false;
                    }
                    if (jarinput != '' && jarinput.lastIndexOf(".war") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="invalid.webapp.file"/>');
                        validFileNames = false; break;
                    } else if(jarinput.indexOf("#") != -1) {
                        CARBON.showWarningDialog('<fmt:message key="unsupported.characters.webapp"/>');
                        validFileNames = false; break;
                    } else if(validateName(jarinput,true)) {
                        CARBON.showWarningDialog('<fmt:message key="unsupported.characters.webapp"/>');
                        validFileNames = false; break;
                    } else if(jarinput.trim().indexOf(" ") != -1) {
                        CARBON.showWarningDialog('<fmt:message key="whitespace.contains.webapp"/>');
                        validFileNames = false;
                    }
                }
            }

             if(document.webappUploadForm.version.value != null){

                 var appVersion =  document.webappUploadForm.version.value;
                 if(appVersion.indexOf("#")!=-1) {
                     CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                     validFileNames = false;
                 } else if(appVersion.indexOf("/")!=-1 || appVersion.indexOf("\\")!=-1) {
                     CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                     validFileNames = false;
                 } else if(validateName(appVersion,false)) {
                     CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                     validFileNames = false;
                 } else if(appVersion.trim().charAt(appVersion.trim().length-1) == "."){
                     CARBON.showWarningDialog('<fmt:message key="dot.contains.version"/>');
                     validFileNames = false
                 }  else if(appVersion.trim().indexOf(" ") != -1) {
                     CARBON.showWarningDialog('<fmt:message key="whitespace.contains.version"/>');
                     validFileNames = false;
                 }
             } else if (document.webappUploadForm.version[0].value != null){
                 for (var i=0; i<document.webappUploadForm.version.length; i++) {
                     var appVersion =  document.webappUploadForm.version[i].value;
                     if(appVersion.indexOf("#")!=-1) {
                         CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                         validFileNames = false; break;
                     } else if(appVersion.indexOf("/")!=-1 || appVersion.indexOf("\\")!=-1) {
                         CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                         validFileNames = false; break;
                     } else if(validateName(appVersion,false)) {
                         CARBON.showWarningDialog('<fmt:message key="unsupported.characters.version"/>');
                         validFileNames = false; break;
                     } else if(appVersion.trim().charAt(appVersion.trim().length-1) == "."){
                         CARBON.showWarningDialog('<fmt:message key="dot.contains.version"/>');
                         validFileNames = false
                     } else if(appVersion.trim().indexOf(" ") != -1) {
                         CARBON.showWarningDialog('<fmt:message key="whitespace.contains.version"/>');
                         validFileNames = false;
                     }
                 }
             }

                if(emptyFields){
                    CARBON.showWarningDialog('<fmt:message key="select.webapp.file"/>');
                }else if(validFileNames) {
                    document.webappUploadForm.submit();
                } else {
                    return;
                }


        }

        function validateName(fileName, val){
            var regex = ".*[\\]\\[!\"#$%&'()*+,/:;<=>?@~{|}^`].*";
            if(val == true){
                regex = ".*[\\]\\[!\"#$%&'()*+,;<=>?@~{|}^`].*";
            }
            if(fileName.match(regex)){
                return true;
            } else {
                return false;
            }
        }

        if (typeof String.prototype.trim != 'function') { // detect native implementation
            String.prototype.trim = function () {
                return this.replace(/^\s+/, '').replace(/\s+$/, '');
            };
        }

        var rows = 1;
        //add a new row to the table
        function addRow() {
            rows++;

            //add a row to the rows collection and get a reference to the newly added row
            var newRow = document.getElementById("webappTbl").insertRow(-1);
            newRow.id = 'file' + rows;

            var oCell = newRow.insertCell(-1);
            oCell.innerHTML = '<label><fmt:message key="webapp.archive"/> (.war)<font color="red">*</font></label>';
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='file' name='warFileName' size='50'/>";
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<label>Version</label>";
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='text' name='version' value=''><input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
            oCell.className = "formRow";

            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }

        function deleteRow(rowId) {
            var tableRow = document.getElementById(rowId);
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
        }


    </script>

    <div id="middle">
        <h2><fmt:message key="upload.web.application"/></h2>

        <div id="workArea">
            <form method="post" name="webappUploadForm" action="../../fileupload/jaxwebapp"
                  enctype="multipart/form-data" target="_self">
                <input type="hidden" name="errorRedirectionPage"
                            value="../carbon/jax-webapp-mgt/upload.jsp?region=region1&item=jax_webapps_add_menu"/>
                <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.new.webapp"/> (.war)</label>
                <br/><br/>

                <table class="styledLeft" id="webappTbl">
                    <tr>
                        <td class="formRow">
                            <label><fmt:message key="webapp.archive"/> (.war)<font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <input type="file" name="warFileName" size="50"/>&nbsp;
                        </td>
                        <td class="formRow">
                            <label>Version</label>
                        </td>
                        <td class="formRow">
                            <input type="text" name="version" value="">
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                    </tr>
                </table>

                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button"
                                   onclick="location.href='../webapp-list/index.jsp'"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('webappTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
