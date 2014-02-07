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
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServicesConstants" %>
<%@ page import="java.util.Random" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.jarservices.ui.i18n.Resources">

<script type="text/javascript">

var serviceGroupNameEditedByUser = false;
var rows = 1;
//add a new row to the table
function addRow() {
    rows++;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("archiveTbl").insertRow(-1);
    newRow.id = 'file' + rows;

    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<fmt:message key="artifact"/>";
    oCell.className = "formRow";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML =
    "<input type='file' name='resourceFileName' size='50'/>&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
    oCell.className = "formRow";

    alternateTableRows('archiveTbl', 'tableEvenRow', 'tableOddRow');
}

function deleteRow(rowId) {
    var tableRow = document.getElementById(rowId);
    tableRow.parentNode.deleteRow(tableRow.rowIndex - 1);
}

function updateServiceGroup() {
    if (serviceGroupNameEditedByUser) {
        return;
    }
    var fileName = document.getElementById('resourceFileName').value;
    fileName = fileName.substr(fileName.lastIndexOf("\\") + 1);
    if (fileName.indexOf(".") != -1) {
        fileName = fileName.substr(0, fileName.lastIndexOf("."));
    }
    document.getElementById('<%= JarServicesConstants.SERVICE_GROUP_NAME%>').value =
                                                              fileName + '<%= new Random().nextInt(100)%>';
}

</script>

    <carbon:breadcrumb label="add.service.archive"
                       resourceBundle="org.wso2.carbon.jarservices.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">
        function submitForm() {
            if(jQuery.trim(document.jarUpload.serviceGroupName.value) == ''){
                CARBON.showWarningDialog('<fmt:message key="specify.a.service.group.name"/>');
                return false;
            }

            // ~!@#$;%^*()+={}[]|\<> are invalid characters for hierarchical services
            var serviceHierarchy = document.jarUpload.serviceHierarchy.value;
            if (serviceHierarchy.lastIndexOf("~") != -1 || serviceHierarchy.lastIndexOf("!") != -1 || serviceHierarchy.lastIndexOf("@") != -1
                    || serviceHierarchy.lastIndexOf("#") != -1 || serviceHierarchy.lastIndexOf("$") != -1 || serviceHierarchy.lastIndexOf(";") != -1
                    || serviceHierarchy.lastIndexOf("%") != -1 || serviceHierarchy.lastIndexOf("^") != -1 || serviceHierarchy.lastIndexOf("*") != -1
                    || serviceHierarchy.lastIndexOf("(") != -1 || serviceHierarchy.lastIndexOf(")") != -1 || serviceHierarchy.lastIndexOf("+") != -1
                    || serviceHierarchy.lastIndexOf("=") != -1 || serviceHierarchy.lastIndexOf("{") != -1 || serviceHierarchy.lastIndexOf("}") != -1
                    || serviceHierarchy.lastIndexOf("[") != -1 || serviceHierarchy.lastIndexOf("]") != -1 || serviceHierarchy.lastIndexOf("|") != -1
                    || serviceHierarchy.lastIndexOf("\\") != -1 || serviceHierarchy.lastIndexOf("<") != -1 || serviceHierarchy.lastIndexOf(">") != -1) {
                CARBON.showWarningDialog('<fmt:message key="invalid.service.hierarchy"/>');
                return false;
            }

            var jarInput;
            var isNonJarFile = false;
            var foundFile = false;
            if (document.jarUpload.resourceFileName[0] != null) { // there is more than 1
                for (var j = 0; j < document.jarUpload.resourceFileName.length; j++) {
                    jarInput =  document.jarUpload.resourceFileName[j].value;
                    if(jarInput != null && jarInput != '') {
                        if (!foundFile) {
                            foundFile = true;
                        }
                        if (jarInput.toLowerCase().lastIndexOf(".jar") == -1) {
                            isNonJarFile = true;
                            break;
                        }
                    }
                }
            } else if (document.jarUpload.resourceFileName != null) { // only 1
                jarInput =  document.jarUpload.resourceFileName.value;
                if (jarInput != null && jarInput != '') {
                    if (!foundFile) {
                        foundFile = true;
                    }
                    if (jarInput.lastIndexOf(".jar") == -1) {
                        isNonJarFile = true;
                    }
                }
            }
            if (!foundFile) {
                CARBON.showWarningDialog('<fmt:message key="select.at.least.one.jar"/>');
                return false;
            } else if (isNonJarFile) {
                CARBON.showWarningDialog('<fmt:message key="select.jar.file.only"/>');
                return false;
            } else {
                document.jarUpload.submit();
                return true;
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="add.jar.service"/></h2>

        <div id="workArea">
            <form method="post" name="jarUpload" action="../../fileupload/jar"
                  enctype="multipart/form-data" target="_self">
                <table class="styledLeft" id="archiveTbl">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="upload.new.service"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <fmt:message key="artifact"/><font color="red">*</font>
                        </td>
                        <td>
                            <input type="file" id="resourceFileName" name="resourceFileName" size="50" onchange="updateServiceGroup()"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formRow">
                            <fmt:message key="wsdl"/>
                        </td>
                        <td>
                            <input type="file" name="wsdlFileName" size="50"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formRow">
                            <fmt:message key="service.hierarchy"/>
                        </td>
                        <td class="formRow">
                            <input name="<%= JarServicesConstants.SERVICE_HIERARCHY%>" type="text" size="50"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formRow">
                            <fmt:message key="service.group.name"/><font color="red">*</font>
                        </td>
                        <td class="formRow">
                            <input name="<%= JarServicesConstants.SERVICE_GROUP_NAME%>"
                                   id="<%= JarServicesConstants.SERVICE_GROUP_NAME%>"
                                   type="text" size="50" onkeypress="javascript:serviceGroupNameEditedByUser = true"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formRow" colspan="2">
                            <a onclick="addRow()" class="icon-link"
                               style="background-image:url(images/add.gif);">
                                <fmt:message key="add.more.dependencies"/>
                            </a>
                        </td>
                    </tr>
                </table>
                <table class="styledLeft">
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="next"/> "
                                   onclick="submitForm();"/>
                            <input type="button" class="button"
                                   onclick="location.href='../service-mgt/index.jsp';"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('archiveTbl', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
