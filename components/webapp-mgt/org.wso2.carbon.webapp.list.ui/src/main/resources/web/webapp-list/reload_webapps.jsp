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
    String reloadAll = request.getParameter("reloadAll");
    String hostName = request.getParameter("hostName");
    String httpPort = request.getParameter("httpPort");
    String webappType = request.getParameter("webappType");
    String defaultHostName = request.getParameter("defaultHostName");
    int pageNumberInt = 0;
    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }
    String redirectPage = request.getParameter("redirectPage");
    if (redirectPage == null) {
        redirectPage = "index.jsp";
    }

    String redirectUrl = "";
    if (redirectPage.startsWith("index.jsp")) {
        redirectUrl = redirectPage + "?pageNumber=" + pageNumberInt;
    } else {
        if (webappKeySet[0].split(":").length > 1) {
            redirectUrl = redirectPage + "?pageNumber=" + pageNumberInt + "&webappFileName=" +
                    URLEncoder.encode(webappKeySet[0].split(":")[1], "UTF-8");
        } else {
            redirectUrl = redirectPage + "?pageNumber=" + pageNumberInt + "&webappFileName=" +
                    URLEncoder.encode(webappKeySet[0], "UTF-8");
        }

        if (hostName != null && httpPort != null) {
            redirectUrl += "&hostName=" + hostName + "&httpPort=" + httpPort;
        }

        redirectUrl += "&webappType=" + webappType + "&webappState=all" + "&defaultHostName=" + defaultHostName;
    }
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
        if (reloadAll != null) {
            client.reloadAllWebapps();
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.reloaded.all.webapps"),
                                                CarbonUIMessage.INFO, request);
        } else {
            client.reloadWebapps(webappKeySet);
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("successfully.reloaded.selected.webapps"),
                                                CarbonUIMessage.INFO, request);
        }
%>
<script>
    debugger;
    location.href = '<%=redirectUrl%>';
</script>

<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    debugger;
    location.href = '<%=redirectUrl%>';
</script>
<%
        return;
    }
%>
