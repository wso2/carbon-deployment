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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient" %>
<%@ page import="org.wso2.carbon.module.mgt.ui.util.ModuleManagementConstants" %>
<%@ page
        import="org.wso2.carbon.module.mgt.ui.util.ModuleManagementUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException" %>

<%
    String bundleName = "org.wso2.carbon.module.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, request.getLocale());

    String action = request.getParameter("action");
    String moduleId = request.getParameter("moduleId");
    String serviceName = request.getParameter("serviceName");

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    CarbonUIMessage carbonMessage;
    String msg = null;
    String url = "service_modules.jsp?serviceName=" + serviceName;

    try {

        ModuleManagementClient client = new ModuleManagementClient(configContext, serverURL,
                cookie, false);

        if (ModuleManagementConstants.ENGAGE.equals(action)) {
            client.engageModuleForService(moduleId, serviceName);
            msg = resourceBundle.getString("success.engage");
        } else if (ModuleManagementConstants.DISENGAGE.equals(action)) {
            client.disengageModuleForService(moduleId, serviceName);
            msg = resourceBundle.getString("success.disengage");
        }

        carbonMessage = new CarbonUIMessage(msg, CarbonUIMessage.INFO);
        request.getSession().setAttribute(CarbonUIMessage.ID, carbonMessage);
        response.sendRedirect(url);
        return;

    } catch (ModuleAdminServiceModuleMgtExceptionException e) {
        ModuleManagementUtils.handleModuleMgtErrors(e, request, response,url);
        return;
    }
%>