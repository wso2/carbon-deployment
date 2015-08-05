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

<fmt:bundle basename="org.wso2.carbon.aarservices.ui.i18n.Resources">
<carbon:breadcrumb label="add.service.archive"
		resourceBundle="org.wso2.carbon.aarservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript">
        function validate() {

            // ~!@#$;%^*()+={}[]|\<> are invalid charactors for hierarchical services
            if (document.aarUpload.serviceHierarchy.value != null) {
                var serviceHierarchy = document.aarUpload.serviceHierarchy.value;
                if (serviceHierarchy.lastIndexOf("~") != -1 || serviceHierarchy.lastIndexOf("!") != -1 || serviceHierarchy.lastIndexOf("@") != -1
                        || serviceHierarchy.lastIndexOf("#") != -1 || serviceHierarchy.lastIndexOf("$") != -1 || serviceHierarchy.lastIndexOf(";") != -1
                        || serviceHierarchy.lastIndexOf("%") != -1 || serviceHierarchy.lastIndexOf("^") != -1 || serviceHierarchy.lastIndexOf("*") != -1
                        || serviceHierarchy.lastIndexOf("(") != -1 || serviceHierarchy.lastIndexOf(")") != -1 || serviceHierarchy.lastIndexOf("+") != -1
                        || serviceHierarchy.lastIndexOf("=") != -1 || serviceHierarchy.lastIndexOf("{") != -1 || serviceHierarchy.lastIndexOf("}") != -1
                        || serviceHierarchy.lastIndexOf("[") != -1 || serviceHierarchy.lastIndexOf("]") != -1 || serviceHierarchy.lastIndexOf("|") != -1
                        || serviceHierarchy.lastIndexOf("\\") != -1 || serviceHierarchy.lastIndexOf("<") != -1 || serviceHierarchy.lastIndexOf(">") != -1) {
                    CARBON.showWarningDialog('<fmt:message key="invalid.service.hierarchy"/>');
                    return;
                }
            } else if (document.aarUpload.serviceHierarchy[0].value !=null){
                for(var i=0; i<document.aarUpload.serviceHierarchy.length; i++) {
                    var serviceHierarchy = document.aarUpload.serviceHierarchy[i].value;
                    if (serviceHierarchy.lastIndexOf("~") != -1 || serviceHierarchy.lastIndexOf("!") != -1 || serviceHierarchy.lastIndexOf("@") != -1
                            || serviceHierarchy.lastIndexOf("#") != -1 || serviceHierarchy.lastIndexOf("$") != -1 || serviceHierarchy.lastIndexOf(";") != -1
                            || serviceHierarchy.lastIndexOf("%") != -1 || serviceHierarchy.lastIndexOf("^") != -1 || serviceHierarchy.lastIndexOf("*") != -1
                            || serviceHierarchy.lastIndexOf("(") != -1 || serviceHierarchy.lastIndexOf(")") != -1 || serviceHierarchy.lastIndexOf("+") != -1
                            || serviceHierarchy.lastIndexOf("=") != -1 || serviceHierarchy.lastIndexOf("{") != -1 || serviceHierarchy.lastIndexOf("}") != -1
                            || serviceHierarchy.lastIndexOf("[") != -1 || serviceHierarchy.lastIndexOf("]") != -1 || serviceHierarchy.lastIndexOf("|") != -1
                            || serviceHierarchy.lastIndexOf("\\") != -1 || serviceHierarchy.lastIndexOf("<") != -1 || serviceHierarchy.lastIndexOf(">") != -1) {
                        CARBON.showWarningDialog('<fmt:message key="invalid.service.hierarchy"/>');
                        return;
                    }
                }
            }

            if (document.aarUpload.aarFilename.value != null && document.aarUpload.aarFilename.length == undefined) {
                var aarinput = document.aarUpload.aarFilename.value;
                if (aarinput == '') {
                    CARBON.showWarningDialog('<fmt:message key="select.aar.service"/>');
                    return;
                } else if (aarinput.lastIndexOf(".aar") == -1) {
                    CARBON.showWarningDialog('<fmt:message key="select.aar.file"/>');
                    return;
                } else {
                    document.aarUpload.submit();
                }
            } else if (document.aarUpload.aarFilename[0].value != null) {
                var validFilenames = true;
                for (var i=0; i<document.aarUpload.aarFilename.length; i++) {
                    var aarinput = document.aarUpload.aarFilename[i].value;
                    if (aarinput == '') {
                        CARBON.showWarningDialog('<fmt:message key="select.aar.service"/>');
                        validFilenames = false;
                        break;
                    } else if (aarinput.lastIndexOf(".aar") == -1) {
                        CARBON.showWarningDialog('<fmt:message key="select.aar.file"/>');
                        validFilenames = false;
                        break;
                    }
                }
                if(validFilenames) {
                    document.aarUpload.submit();
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
            oCell.innerHTML = "<input type='file' name='aarFilename' size='50'/>";
            oCell.className = "formRow";

            oCell = newRow.insertCell(-1);
            oCell.innerHTML = "<input type='text' name='serviceHierarchy' size='50'/>&nbsp;&nbsp;<input type='button' width='20px' class='button' value='  -  ' onclick=\"deleteRow('file"+ rows +"');\" />";
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
        <h2><fmt:message key="add.service.archive"/></h2>

        <div id="workArea">
            <form method="post" name="aarUpload" action="../../fileupload/service"
                  enctype="multipart/form-data" target="_self">

                <input type="hidden" name="errorRedirectionPage"
                            value="../carbon/aarservices/index.jsp?region=region1&item=aar_menu"/>
                <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.new.service"/> (.aar)</label>
                <br/><br/>

                <table class="styledLeft" id="serviceTbl">
                    <tr>
                        <td class="formRow">
                             <label style="font-weight:bold;"><fmt:message key="service.archive"/> (.aar)<font color="red">*</font></label>
                        </td>
                        <td class="formRow">
                            <label style="font-weight:bold;"><fmt:message key="service.hierarchy"/></label>
                        </td>
                    </tr>

                    <tr>
                        <td class="formRow">
                            <input type="file" name="aarFilename" size="50"/>                            
                        </td>
                        <td class="formRow">
                            <input type="text" name="serviceHierarchy" size="50"/>&nbsp;
                            <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                        </td>
                     </tr>

                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="upload" type="button" class="button"
                                       value=" <fmt:message key="upload"/> "
                                       onclick="validate();"/>
                                <input type="button" class="button" onclick="javascript:location.href='../service-mgt/index.jsp'"
                                       value=" <fmt:message key="cancel"/> "/>
                            </td>
                        </tr>
                    </table>
                </table>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('serviceTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
