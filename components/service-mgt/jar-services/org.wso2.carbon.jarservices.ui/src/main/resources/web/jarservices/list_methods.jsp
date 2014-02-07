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
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServiceAdminClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.Utils" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.Service" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.Operation" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServicesConstants" %>
<%@ page import="org.wso2.carbon.jarservices.stub.DuplicateServiceExceptionException" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="js/jarservices.js" type="text/javascript"></script>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    Service[] services = Utils.getServices(request, "clazz");

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    try {
        JarServiceAdminClient jarServiceAdminClient = new JarServiceAdminClient(configContext, backendServerURL, cookie);
        String path = ((UploadArtifactsResponse) session.getAttribute(JarServicesConstants.UPLOAD_ARTIFACTS_RESPONSE)).getResourcesDirPath();
        services = jarServiceAdminClient.getClassMethods(path, services);
    } catch (DuplicateServiceExceptionException e) {
        response.setStatus(500);
        CarbonUIMessage.sendCarbonUIMessage(e.getFaultMessage().getDuplicateServiceException().getMsg(),
                                            CarbonUIMessage.WARNING, request);
%>
<jsp:include page="list_classes.jsp"/>
<%
        return;
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.jarservices.ui.i18n.Resources">
    <script type="text/javascript">
        var classes = new Array();

        function validate() {
            return true;
        }

        function selectMethod(className, methodName, checked) {
            for (var i = 0; i < classes.length; i++) {
                if (classes[i].className == className) {
                    classes[i].getMethod(methodName).selected = checked;
                    break;
                }
            }
        }

        function submitForm() {
            var selected;
            for (var i = 0; i < classes.length; i++) {
                var methods = classes[i].methods;
                var atLeastOneSelected = false;
                for (var k = 0; k < methods.length; k++) {
                    if (methods[k].selected) {
                        atLeastOneSelected = true;
                        break;
                    }
                }
                if (!atLeastOneSelected) {
                    CARBON.showWarningDialog('Select  at least one method in class ' + classes[i].className);
                    return false;
                }
            }

            // Inverting
            if (document.methodsForm.methodChk[0] != null) { // there is more than 1
                for (var j = 0; j < document.methodsForm.methodChk.length; j++) {
                    selected = document.methodsForm.methodChk[j].checked;
                    if (!selected) {
                        document.methodsForm.method[j].value = document.methodsForm.methodChk[j].value;
                    }
                }
            } else if (document.methodsForm.methodChk != null) { // only 1
                selected = document.methodsForm.methodChk.checked;
                if (!selected) {
                    document.methodsForm.method.value = document.methodsForm.methodChk.value;
                }
            }
            document.methodsForm.submit();
            return true;
        }
    </script>

    <carbon:breadcrumb label="select.operations"
                       resourceBundle="org.wso2.carbon.jarservices.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="add.jar.service"/></h2>

        <div id="workArea">
            <form method="post" action="finish.jsp" target="_self" name="methodsForm">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="operations"/></th>
                    </tr>
                    </thead>

                    <%
                        for (Service service : services) {
                    %>
                    <script type="text/javascript">
                        var clazz = new CarbonClass();
                        clazz.setClassName('<%= service.getClassName()%>');
                        classes.push(clazz);
                    </script>
                    <tr>
                        <td class="formRow">
                            <input type="hidden" name="clazz" value="<%= service.getClassName()%>"/>
                            <table class="styledLeft" id="<%= service.getClassName()%>Tbl">
                                <tbody>
                                <tr>
                                    <td colspan="2"><strong><fmt:message
                                            key="class"/></strong> <%= service.getClassName()%>
                                    </td>
                                </tr>
                                <%
                                    for (Operation operation : service.getOperations()) {
                                %>
                                <script type="text/javascript">
                                    var method = new CarbonMethod();
                                    method.setMethodName('<%= operation.getOperationName()%>');
                                    clazz.addMethod(method);
                                </script>
                                <tr>
                                    <td width="15px">
                                        <input type="checkbox" name="methodChk"
                                               value="<%= service.getClassName()  + "#" + service.getServiceName() + "#" + service.getDeploymentScope() + "#" + service.getUseOriginalWsdl() + "#" + operation.getOperationName()%>"
                                               checked="true"
                                               onclick="selectMethod('<%= service.getClassName()%>', 
                                               '<%= operation.getOperationName()%>',
                                               this.checked);"/>
                                        <input type="hidden" name="method"
                                               value="<%= service.getClassName() + "#" + service.getServiceName() + "#" + service.getDeploymentScope() + "#" + service.getUseOriginalWsdl() %>" />
                                    </td>
                                    <td>
                                        <%= operation.getOperationName() %>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                            <script type="text/javascript">
                                alternateTableRows('<%= service.getClassName()%>Tbl', 'tableEvenRow', 'tableOddRow');
                            </script>
                        </td>
                    </tr>
                    <% } %>
                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" class="button"
                                   value=" <fmt:message key="back"/> " onclick="history.back();"/>
                            <input type="button" class="button"
                                   value=" <fmt:message key="finish"/> " onclick="submitForm();"/>
                            <input type="button" class="button"
                                   onclick="location.href='../service-mgt/index.jsp';"
                                   value=" <fmt:message key="cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
