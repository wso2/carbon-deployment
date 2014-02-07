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
<%@ page import="org.wso2.carbon.operation.mgt.ui.client.OperationAdminClient" %>
<%@ page import="org.wso2.carbon.operation.mgt.stub.types.OperationMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.operation.mgt.stub.types.OperationMetaDataWrapper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<carbon:breadcrumb label="operations.breadcrumbtext"
		resourceBundle="org.wso2.carbon.operation.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
    <%
        String serviceName = request.getParameter("serviceName");
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        OperationMetaData[] publishedOperations;
        OperationMetaData[] controlOperations;
        try {
            OperationMetaDataWrapper wrapper =
                    new OperationAdminClient(cookie, backendServerURL, configContext).
                                                                    listAllOperations(serviceName);
            publishedOperations = wrapper.getPublishedOperations();
            controlOperations = wrapper.getControlOperations();
        } catch (Exception e) {
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
                %>
                <jsp:include page="../admin/error.jsp"/>
                <%
                return;
        }
    %>
<fmt:bundle basename="org.wso2.carbon.operation.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key="operations">
                <fmt:param value="<%= serviceName%>"/>
            </fmt:message>
        </h2>

        <div id="workArea">
            <% if (publishedOperations != null && publishedOperations.length > 0){ %>
            <table id="publishedOpTable" class="styledLeft" width="100%">
                <thead>
                <tr>
                    <th><fmt:message key="published.operations"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (OperationMetaData operation : publishedOperations) {
                        String opName = operation.getName();
                %>
                <tr>
                    <td>
                        <a href="operation.jsp?serviceName=<%=serviceName%>&opName=<%=opName%>">
                            <%= opName %>
                        </a>
                    </td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <% } %>

            <% if (controlOperations != null && controlOperations.length > 0){ %>
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                <table id="controlOpTable" class="styledLeft" width="100%">
                    <thead>
                    <tr>
                        <th><fmt:message key="control.operations"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (OperationMetaData operation : controlOperations) {
                            String opName = operation.getName();
                    %>
                    <tr>
                        <td><%= opName %></td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            <% } %>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('publishedOpTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('controlOpTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>