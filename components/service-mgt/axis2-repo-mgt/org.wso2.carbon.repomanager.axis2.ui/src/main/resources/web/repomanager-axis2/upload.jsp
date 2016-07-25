<%@ page import="org.wso2.carbon.repomanager.axis2.ui.Axis2RepoManagerUIConstants" %>
<%--
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
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.repomanager.axis2.ui.i18n.Resources">
<carbon:breadcrumb label="upload.axis2.artifact"
		resourceBundle="org.wso2.carbon.repomanager.axis2.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

<%
    String dirPath = null;
    String dirName = null;
    String service_type = null;
    if (session.getAttribute(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH) == null ||
            session.getAttribute(Axis2RepoManagerUIConstants.DIR_NAME) == null) {
%>
    <script type="text/javascript">
        location.href = "index.jsp?region=region1&item=axis2_repo_mgt_menu";
    </script>
<%
    } else {
        dirPath = (String) session.getAttribute(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH);
        session.removeAttribute(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH);
        dirName = (String) session.getAttribute(Axis2RepoManagerUIConstants.DIR_NAME);
        session.removeAttribute(Axis2RepoManagerUIConstants.DIR_NAME);
        service_type = (String) session.getAttribute(Axis2RepoManagerUIConstants.SERVICE_TYPE);
        session.removeAttribute(Axis2RepoManagerUIConstants.SERVICE_TYPE);
    }
%>

<script type="text/javascript">
    function validate() {

        if (document.axis2artifactUploadForm.libFileName.value != null) {
                var archiveInput = document.axis2artifactUploadForm.libFileName.value;
                if (archiveInput == '') {
                    CARBON.showWarningDialog('<fmt:message key="select.artifacts.to.upload"/>');
                    return;
                } else if (archiveInput.lastIndexOf(".jar") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="select.jar.to.upload"/>');
                    return;
                } else {
                    document.forms["axis2artifactUploadForm"].elements["dirPath"].value = '<%=dirPath%>';
                    document.forms["axis2artifactUploadForm"].elements["dirName"].value = '<%=dirName%>';
                    document.forms["axis2artifactUploadForm"].submit();
                }
            } else if (document.axis2artifactUploadForm.libFileName[0].value != null) {
                var validFilenames = true;
                for (var i=0; i<document.axis2artifactUploadForm.libFileName.length; i++) {
                    var archiveInput = document.axis2artifactUploadForm.libFileName[i].value;
                    if (archiveInput == '') {
                        CARBON.showWarningDialog('<fmt:message key="select.artifacts.to.upload"/>');
                        validFilenames = false;
                        break;
                    } else if (archiveInput.lastIndexOf(".jar") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="select.jar.to.upload"/>');
                        validFilenames = false;
                        break;
                    }
                }
                if(validFilenames) {
                    document.forms["axis2artifactUploadForm"].elements["dirPath"].value = '<%=dirPath%>';
                    document.forms["axis2artifactUploadForm"].elements["dirName"].value = '<%=dirName%>';
                    document.forms["axis2artifactUploadForm"].submit();
                } else {
                    return;
                }
            }


	}

    var rows = 1;
    //add a new row to the table
    function addRow() {
    rows++;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("serviceTbl").insertRow(-1);
    newRow.id = 'file' + rows;

    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<label><fmt:message key="axis2.artifact.archive"/></label>";
    oCell.className = "formRow";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input type='file' name='libFileName' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
    oCell.className = "formRow";

    alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
    }

    function deleteRow(rowId) {
        var tableRow = document.getElementById(rowId);
        tableRow.parentNode.deleteRow(tableRow.rowIndex);
        alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
    }

</script>

<div id="middle">
    <h2><fmt:message key="axis2.repo.mgt.title"/></h2>

    <div id="workArea">
        <form method="post" name="axis2artifactUploadForm" action="../../fileupload/axis2repomanager?<csrf:tokenname/>=<csrf:tokenvalue/>"
                  enctype="multipart/form-data" target="_self">

            <input type="hidden" name="errorRedirectionPage"
                                value="../carbon/repomanager-axis2/index.jsp?region=region1&item=axis2_repo_mgt_menu"/>
            <input type="hidden" name="dirPath">
            <input type="hidden" name="dirName">

            <label style="font-weight:bold;">&nbsp;<fmt:message key="add.msg.1"/> <%= service_type%> <fmt:message key="add.msg.2"/> <%= dirPath%></label>
            <br/><br/>

            <table class="styledLeft" id="serviceTbl">
                <tr>
                    <td class="formRow">
                        <label><fmt:message key="axis2.artifact.archive"/></label>
                    </td>
                    <td class="formRow">
                        <input type="file" name="libFileName" size="50"/>&nbsp;
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
                            onclick="javascript:location.href='index.jsp?region=region1&item=axis2_repo_mgt_menu'"
                            value=" <fmt:message key="cancel"/> "/>
                    </td>
                </tr>
            </table>
            
        </form>
    </div>
</div>

<script type="text/javascript">
    alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
