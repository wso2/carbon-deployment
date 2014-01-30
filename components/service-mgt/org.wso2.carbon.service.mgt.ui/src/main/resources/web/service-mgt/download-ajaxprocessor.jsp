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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    if (cookie == null) {
        response.sendRedirect("/carbon/admin/login.jsp");
        return;
    }
    if (!CarbonUtils.isRunningOnLocalTransportMode())
    {
       %><jsp:forward page="index.jsp" /><%
    }

    ServiceAdminClient client;
    String serviceGroupName = request.getParameter("serviceName");
    String serviceType = request.getParameter("serviceType");

    out.clear();
    out = pageContext.pushBody();
    out.clearBuffer();
    client = new ServiceAdminClient(cookie, backendServerURL, configContext, request.getLocale());
    client.downloadServiceArchive(serviceGroupName, serviceType, response);
    out.close();

%>