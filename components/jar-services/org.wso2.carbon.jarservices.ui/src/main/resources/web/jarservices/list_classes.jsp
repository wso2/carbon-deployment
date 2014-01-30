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
<%@ page import="org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.Service" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServicesConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    UploadArtifactsResponse uploadResponse =
                                (UploadArtifactsResponse) session.getAttribute(JarServicesConstants.UPLOAD_ARTIFACTS_RESPONSE);

    String BUNDLE = "org.wso2.carbon.jarservices.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    Service[] servicesList = uploadResponse.getServices();
    if (servicesList == null || servicesList.length == 0) {
        CarbonUIMessage.sendCarbonUIMessage(bundle.getString("no.classes.found.in.jar"),
                CarbonUIMessage.WARNING, request);
%>
<script type="text/javascript">
    location.href = "index.jsp";
</script>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.jarservices.ui.i18n.Resources">
    <script type="text/javascript">
        function selectAll(isSelected) {
            if (document.classesForm.clazzChk[0] != null) { // there is more than 1
                for (var j = 0; j < document.classesForm.clazzChk.length; j++) {
                    document.classesForm.clazzChk[j].checked=isSelected;
                    setValue(document.classesForm.clazzChk[j].value);
                }
            } else if (document.classesForm.clazzChk != null) { // only 1
                document.classesForm.clazzChk.checked=isSelected;
            }
        }

        function setValue(clazzId) {
            var chk = document.getElementById(clazzId + "Chk"); // ClassName
            var txt = document.getElementById(clazzId + "Txt"); // ServiceName
            var hid = document.getElementById(clazzId + "Hid"); // Value to be posted to FE server
            var scope = document.getElementById(clazzId + "Scope"); // DeploymentScope
            var originalWsdl = document.getElementById(clazzId + "UseOriginalWsdl"); // useOriginalWsdl

            if (chk.checked) {
                hid.value = chk.value + "#" + txt.value + "#" + scope[scope.selectedIndex].value + "#" + originalWsdl[originalWsdl.selectedIndex].value;
                txt.disabled = '';
                scope.disabled = '';
                <%
                if(uploadResponse.getWsdlProvided()){
                %>
                    originalWsdl.disabled = '';
                <%
                }
                %>
            } else {
                hid.value = "";             
                txt.disabled = 'disabled';
                scope.disabled = 'disabled';
                <%
                if(uploadResponse.getWsdlProvided()){
                %>
                    originalWsdl.disabled = 'disabled';
                <%
                }
                %>
            }
        }

        function submitForm() {
            var selected;
            if (document.classesForm.clazzChk[0] != null) { // there is more than 1
                for (var j = 0; j < document.classesForm.clazzChk.length; j++) {
                    selected = document.classesForm.clazzChk[j].checked;
                    if (selected) {
                        if(jQuery.trim(document.classesForm.clazzTxt[j].value) == ''){
                            CARBON.showWarningDialog('<fmt:message key="service.name.cannot.be.empty"/>');
                            return false;
                        }
                        break;
                    }
                }
            } else if (document.classesForm.clazzChk != null) { // only 1
                selected = document.classesForm.clazzChk.checked;
                if(selected){
                    if(jQuery.trim(document.classesForm.clazzTxt.value) == ''){
                        CARBON.showWarningDialog('<fmt:message key="service.name.cannot.be.empty"/>');
                        return false;
                    }
                }
            }
            if (!selected) {
                CARBON.showWarningDialog('<fmt:message key="select.at.least.one.class"/>');
                return false;
            }
            document.classesForm.submit();
            return true;
        }
    </script>

    <carbon:breadcrumb label="select.classes"
                       resourceBundle="org.wso2.carbon.jarservices.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="add.jar.service"/></h2>

        <div id="workArea">
            <form method="post" action="list_methods.jsp" target="_self" name="classesForm">
                <table class="styledLeft" id="archiveTbl">
                    <thead>
                    <tr>
                        <th colspan="5"><fmt:message key="classes"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td colspan="5">
                            <carbon:simpleItemGroupSelector
                                    selectAllFunction="selectAll(true)"
                                    selectNoneFunction="selectAll(false)"
                                    resourceBundle="org.wso2.carbon.jarservices.ui.i18n.Resources"
                                    selectAllKey="selectAll"
                                    selectNoneKey="selectNone"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td><strong><fmt:message key="class1"/></strong></td>
                        <td><strong><fmt:message key="service.name"/></strong></td>
                        <td><strong><fmt:message key="deployment.scope"/></strong></td>
                        <td><strong><fmt:message key="use.original.wsdl"/></strong></td>
                    </tr>
                    <%
                        Service[] services = uploadResponse.getServices();
                        for (Service clazz : services) {
                    %>
                    <tr>
                        <td class="formRow" width="15px">
                            <input id="<%= clazz.getClassName()%>Chk" name="clazzChk" type="checkbox" value="<%= clazz.getClassName() %>"
                                   onclick="setValue('<%= clazz.getClassName()%>');"/>
                        </td>
                        <td class="formRow" width="20%">
                            <%= clazz.getClassName() %>
                            <input id="<%= clazz.getClassName()%>Hid" type="hidden" name="clazz"
                                   value=""/>
                        </td>
                        <td class="formRow" width="20%">
                            <input id="<%= clazz.getClassName()%>Txt" type="text" size="50"
                                   name="clazzTxt"
                                   value="<%= clazz.getServiceName() %>"
                                   onchange="setValue('<%= clazz.getClassName()%>');"
                                   disabled="disabled"/>
                        </td>
                        <td class="formRow">
                            <select id="<%= clazz.getClassName()%>Scope" onclick="setValue('<%= clazz.getClassName()%>');">
                                <option value="application">Application</option>
                                <option selected="true" value="request">Request</option>
                                <option value="soapsession">SOAP Session</option>
                                <option value="transportsession">Transport Session</option>
                            </select>
                        </td>
                        <td class="formRow">
                            <select id="<%= clazz.getClassName()%>UseOriginalWsdl"
                                    onclick="setValue('<%= clazz.getClassName()%>');"
                                    disabled='<%= (uploadResponse.getWsdlProvided())?"":"disabled"%>'>
                                <option value="false" selected="true"><fmt:message key="false"/></option>
                                <option value="true"><fmt:message key="true"/></option>
                            </select>
                        </td>
                    </tr>
                    <% } %>
                    <tr>
                        <td colspan="5">
                            <carbon:simpleItemGroupSelector
                                    selectAllFunction="selectAll(true)"
                                    selectNoneFunction="selectAll(false)"
                                    resourceBundle="org.wso2.carbon.jarservices.ui.i18n.Resources"
                                    selectAllKey="selectAll"
                                    selectNoneKey="selectNone"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow" colspan="5">
                            <input type="button" class="button"
                                   value=" <fmt:message key="back"/> " onclick="history.back();"/>
                            <input type="button" class="button"
                                   value=" <fmt:message key="next"/> " onclick="submitForm();"/>
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
