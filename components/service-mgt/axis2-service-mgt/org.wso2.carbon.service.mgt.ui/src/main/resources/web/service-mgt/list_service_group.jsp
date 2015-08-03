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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceGroupAdminClient" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<jsp:include page="javascript_include.jsp" />

<%
    String serviceGroupName = CharacterEncoder.getSafeText(request.getParameter("serviceGroupName"));
%>
<script type="text/javascript">
    function createServiceArchive() {
        var url = "create_service_archive_ajaxprocessor.jsp?serviceGroupName=<%= serviceGroupName%>";
        try {
            jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                if (status != "success") {
                    document.getElementById('result').innerHTML = responseText;
                }
            });
        } catch (e) {
        } // ignored
    }
</script>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

    <%
        String failed;
        ServiceGroupMetaData serviceGroupData = null;

        failed = request.getParameter("failed");
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        if (serviceGroupName != null && serviceGroupName.trim().length() > 0) {
            ConfigurationContext configContext;
            String cookie;       
            ServiceGroupAdminClient client;

            configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);

            cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            try {
                client = new ServiceGroupAdminClient(cookie, backendServerURL, configContext, request.getLocale());
                serviceGroupData = client.listServiceGroup(serviceGroupName);
            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
                %>
                        <script type="text/javascript">
                               location.href = "../admin/error.jsp";
                        </script>
                <%
            return;
        }
      }        		
    %>
    <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="service.group.dashboard"
            resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
            
    <div id="middle">
        <h2><fmt:message key="service.group.dashboard"/> (<%= serviceGroupName %>)</h2>

        <div id="workArea">
            <div id="result"></div>
            <table class="styledLeft" id="serviceGroupTable" width="100%">
                <% if (failed != null) { %>
                <tr>
                    <td colspan="2"><fmt:message key="could.not.generate.service.archive"/></td>
                </tr>
                <% } %>
                <thead>
                <tr>
                    <th colspan="2" align="left"><fmt:message key="service.group.details"/></th>
                </tr>
                </thead>
                <tr>
                    <td width="20%"><fmt:message key="service.group.name"/></td>
                    <td>&nbsp;&nbsp;<%=serviceGroupName%>
                    </td>
                </tr>
            </table>
            <p>&nbsp;</p>
            
            <table class="styledLeft" width="100%">
                <thead>
                    <tr>
                        <th colspan="5"><fmt:message key="services"/></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for(ServiceMetaData service: serviceGroupData.getServices()) {
                            String serviceName = service.getName();
                    %>
                    <tr>
                        <td width="200px">
                            <a href="./service_info.jsp?serviceName=<%=serviceName%>"><%=serviceName%>
                            </a>
                        </td>
                        <td width="20px" style="text-align:center;">
                            <img src="../<%= service.getServiceType()%>/images/type.gif"
                                 title="<%= service.getServiceType()%>"
                                 alt="<%= service.getServiceType()%>"/>
                        </td>
                        <td width="100px">
                            <% if (service.getActive()) {%>
                            <a href="<%=service.getWsdlURLs()[0]%>" class="icon-link"
                               style="background-image:url(images/wsdl.gif);" target="_blank">
                                WSDL1.1
                            </a>
                            <% } %>
                        </td>
                        <td width="100px">
                            <% if (service.getActive()) {%>
                            <a href="<%=service.getWsdlURLs()[1]%>" class="icon-link"
                               style="background-image:url(images/wsdl.gif);" target="_blank">
                                WSDL2.0
                            </a>
                            <% } %>
                        </td>
                        <%
                            if (!service.getDisableTryit()) {
                        %>
                        <td width="100px">
                            <% if (service.getActive()) {%>
                            <nobr>
                                <a href="<%=service.getTryitURL()%>" class="icon-link"
                                   style="background-image:url(images/tryit.gif);" target="_blank">
                                    <fmt:message key="try.this.service"/>
                                </a>
                            </nobr>
                            <% } %>
                        </td>
                        <% } %>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>

</fmt:bundle>
