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
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="js/modulemgt.js"></script>
<fmt:bundle basename="org.wso2.carbon.module.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="add.modules"
            resourceBundle="org.wso2.carbon.modulemgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="add.modules"/></h2>

        <div id="workArea">
            <div id="formset">
                <form method="post" name="marUpload" action="../../fileupload/module" enctype="multipart/form-data"
                      target="_self">
                    <input type="hidden" name="errorRedirectionPage"
                                value="../carbon/modulemgt/add_modules.jsp?region=region1&item=modules_add_menu"/>
                    <label style="font-weight:bold;">&nbsp;<fmt:message key="upload.new.modules.mar"/></label>
                    <br/><br/>

                    <table class="styledLeft" id="moduleTbl">
                        <tr>
                            <td class="formRow" width="20%">
                                <label><fmt:message key="module.archive.mar"/><font color="red">*</font></label>
                            </td>
                            <td class="formRow">
                                <input type="file" name="marFilename" size="50"/>&nbsp;
                                <input type="button"  width='20px' class="button" onclick="addRow();" value=" + "/>
                            </td>
                        </tr>
                    </table>

                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="upload" class="button" type="button"
                                       value="<fmt:message key="button.upload"/>" onclick="validate();"/>
                                <input type="button" class="button" value="<fmt:message key="button.cancel"/>"
                                       onclick="javascript:location.href='../modulemgt/index.jsp'"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('moduleTbl', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
