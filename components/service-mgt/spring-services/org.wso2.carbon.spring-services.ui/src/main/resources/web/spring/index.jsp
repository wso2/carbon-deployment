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

<fmt:bundle basename="org.wso2.carbon.springservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="add.spring.service"
		resourceBundle="org.wso2.carbon.springservices.ui.i18n.Resources"
		topPage="true" 
		request="<%=request%>" />
		
    <script type="text/javascript">
        function validate() {

            // ~!@#$;%^*()+={}[]|\<> are invalid charactors for hierarchical services
            var serviceHierarchy = document.springUpload.serviceHierarchy.value;
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

            var scinput = document.springUpload.springContext.value;
            var sbinput = document.springUpload.springBeans.value;
            if (scinput == '' || sbinput == '') {
                CARBON.showWarningDialog('<fmt:message key="please.select.required.fields"/>');
            } else if (scinput.lastIndexOf(".xml") == -1) {
                CARBON.showWarningDialog('<fmt:message key="please.select.xml"/>');
            } else if (sbinput.lastIndexOf(".jar") == -1) {
                CARBON.showWarningDialog('<fmt:message key="please.select.jar"/>');
            } else {
                document.springUpload.submit();
            }
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="add.spring.service"/></h2>

        <div id="workArea">
            <form method="post" name="springUpload" action="../../fileupload/spring"
                  enctype="multipart/form-data" target="_self">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="upload.new.spring.service"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                 <tr>
                                    <td>
                                        <label><fmt:message key="spring.beans.jar"/><font color="red">*</font></label>
                                    </td>
                                    <td>
                                        <input type="file" id="springBeans" name="springBeans"
                                               size="50"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label><fmt:message key="spring.context.xml"/><font color="red">*</font></label>
                                    </td>
                                    <td>
                                        <input type="file" id="springContext" name="springContext"
                                               size="50"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label><fmt:message key="service.hierarchy"/></label>
                                    </td>
                                    <td>
                                        <input type="text" id="serviceHierarchy" name="serviceHierarchy"
                                               size="50"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
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
            </form>
        </div>
    </div>
</fmt:bundle>
