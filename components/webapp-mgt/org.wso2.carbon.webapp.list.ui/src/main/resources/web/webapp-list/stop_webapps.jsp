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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.webapp.list.ui.WebappAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.net.URLEncoder" %>
<%
    String[] webappKeySet = request.getParameterValues("webappKey");
    String pageNumber = request.getParameter("pageNumber");
    String undeployAll = request.getParameter("undeployAll");
    String hostName = request.getParameter("hostName");
    String httpPort = request.getParameter("httpPort");
    String defaultHostName = request.getParameter("defaultHostName");
    int pageNumberInt = 0;
    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }
    String webappState = "stopped";
    String redirectPage = request.getParameter("redirectPage");
    if (redirectPage == null) {
        redirectPage = "index.jsp";
        webappState = "all";
    }

    String redirectName = webappKeySet[0].split(":")[1];
%>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    ResourceBundle bundle = ResourceBundle
            .getBundle(WebappAdminClient.BUNDLE, request.getLocale());

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    WebappAdminClient client;
    try {
        client = new WebappAdminClient(cookie, backendServerURL, configContext, request.getLocale());
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    try {
        if (undeployAll != null) {
            client.stopAllWebapps();
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.stopped.all.webapps"),
                                                CarbonUIMessage.INFO, request);
        } else {
            client.stopWebapps(webappKeySet);
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.stopped.selected.webapps"),
                                                CarbonUIMessage.INFO, request);
        }
%>
<script>
    location.href = '<%= redirectPage%>?pageNumber=<%=pageNumberInt%>&webappFileName=<%= URLEncoder.encode(redirectName, "UTF-8")%>&webappState=<%= webappState %>&defaultHostName=<%= defaultHostName %>'
                    <% if (hostName != null && httpPort != null) { %>
                    + '&hostName=<%= hostName %>&httpPort=<%= httpPort %>'
                    <% } %> ;
</script>

<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "<%= redirectPage%>?pageNumber=<%=pageNumberInt%>&webappFileName=<%= URLEncoder.encode(redirectName, "UTF-8") %>&webappState=<%= webappState %>&defaultHostName=<%= defaultHostName %>"
                    <% if (hostName != null && httpPort != null) { %>
                    +"&hostName=<%= hostName %>&httpPort=<%= httpPort %>"
                    <% } %> ;
</script>
<%
        return;
    }
%>
