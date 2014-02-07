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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%--<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>--%>
<%--<jsp:include page="../admin/layout/ajaxheader.jsp"/>--%>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.operation.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="operation.dashboard.breadcrumbtext"
		resourceBundle="org.wso2.carbon.operation.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
     <%
        String serviceName = request.getParameter("serviceName");
        String opName = request.getParameter("opName");

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        OperationMetaData data;
        try {
            data = new OperationAdminClient(cookie, backendServerURL, configContext).
                    getOperationMetaData(serviceName, opName);
        } catch (Exception e) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
     %>
            <jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
        boolean statisticsComponentFound = CarbonUIUtil.isContextRegistered(config, "/statistics/");
        if (statisticsComponentFound) {
//        int responseTimeGraphWidth = 500;
//    responseTimeGraphWidth = Utils.getValue(session, request, responseTimeGraphWidth, "responseTimeGraphWidth");
    %>
        <script type="text/javascript" src="../statistics/js/statistics.js"></script>
        <script type="text/javascript" src="../statistics/js/graphs.js"></script>

        <script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
        <script type="text/javascript" src="../admin/js/excanvas.js"></script>
        <script type="text/javascript">
            initResponseTimeGraph('50');
        </script>
    <%
        }
    %>

    <script type="text/javascript">
        function submitHiddenForm(action) {
            document.getElementById("hiddenField").value = location.href + "&ordinal=3";
            document.dataForm.action = action;
            document.dataForm.submit();
        }
    </script>

    <div id="middle">
        <h2>
            <fmt:message key="operation.dashboard">
                <fmt:param value="<%= serviceName%>"/>
                <fmt:param value="<%= opName%>"/>
            </fmt:message>
        </h2>

        <div id="workArea">
            <table width="100%">
                <tr>
                    <td width="50%">
                        <table class="styledLeft" id="opDetailsTable" width="100%" style="margin-left:0px;">
                            <thead>
                            <tr>
                                <th colspan="2" align="left"><fmt:message key="operation.details"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td width="30%"><fmt:message key="operation.name"/></td>
                                <td><%= data.getName()%>
                                </td>
                            </tr>
                            <tr>
                                <td width="30%"><fmt:message key="service"/></td>
                                <td><%= serviceName%>
                                </td>
                            </tr>
                            <tr>
                                <td width="30%"><fmt:message key="mtom"/></td>
                                <td><%= data.getEnableMTOM()%>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <p>&nbsp;</p>
                        <table class="styledLeft" id="opConfigurationTable" width="100%" style="margin-left:0px;">
                            <thead>
                            <tr>
                                <th colspan="2" align="left"><fmt:message key="quality.of.service.configuration"/></th>
                            </tr>
                            </thead>
                            <tr>
                                <td width="50%">
                                    <%
                                        if(CarbonUIUtil.isContextRegistered(config, "/caching/")){
                                    %>
                                    <a href="" onclick="submitHiddenForm('../caching/index.jsp?serviceName=<%=serviceName%>&opName=<%=opName%>');return false;">
                                        <img src="images/caching.gif"
                                             alt="Manage response caching"/>
                                        <fmt:message key="response.caching"/>
                                    </a>
                                    <% } %>
                                </td>
                                <td width="50%">
                                    <%
                                        if(CarbonUIUtil.isContextRegistered(config, "/viewflows/")){
                                    %>
                                    <a href="../viewflows/index.jsp?serviceName=<%=serviceName %>&opName=<%= opName%>&toppage=false"
                                       id="handler_view_link" title="View Axis2 handler chain">
                                        <img src="images/flows.gif" alt="View Axis2 handler chain"/>
                                        <fmt:message key="msg.flows"/>
                                    </a>
                                    <% } %>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <%
                                        if(CarbonUIUtil.isContextRegistered(config, "/throttling/")){
                                    %>
                                    <a href="" onclick="submitHiddenForm('../throttling/index.jsp?serviceName=<%=serviceName%>&opName=<%=opName%>');return false;">
                                        <img src="images/throttling.gif"
                                             alt="Manage access throttling"/>
                                        <fmt:message key="access.throttling"/>
                                    </a>
                                    <% } %>
                                </td>
                                <td width="50%">
                                    <%
                                        if(CarbonUIUtil.isContextRegistered(config, "/modulemgt/")){
                                    %>
                                    <a title="View engaged modules"
                                       href="../modulemgt/operation_modules.jsp?serviceName=<%=serviceName%>&operationName=<%=opName%>"
                                       id="module_view_link">
                                        <img src="images/modules.gif"
                                             alt="Manage module engagements"/>
                                        <fmt:message key="modules"/>
                                    </a>
                                    <% } %>
                                </td>
                            </tr>
                            <tr>
                                <td width="50%">
                                    <img src="images/service.gif" alt="MTOM state"/>&nbsp;MTOM&nbsp;&nbsp;&nbsp;
                                    <select id="mtomSelector" onchange="changeMtomState('<%= opName%>', 'mtomSelector')">
                                        <%
                                            if (data.getEnableMTOM().equalsIgnoreCase("true")) {
                                        %>
                                        <option value="true" selected="true"><fmt:message key="true"/></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="true"><fmt:message key="true"/></option>
                                        <% } %>

                                        <%
                                            if (data.getEnableMTOM().equalsIgnoreCase("false")) {
                                        %>
                                        <option value="false" selected="true"><fmt:message key="false"/></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="false"><fmt:message key="false"/></option>
                                        <% } %>

                                        <%
                                            if (data.getEnableMTOM().equalsIgnoreCase("optional")) {
                                        %>
                                        <option value="optional" selected="true"><fmt:message key="optional"/></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="optional"><fmt:message key="optional"/></option>
                                        <% } %>
                                    </select>
                                    <div id="mtomOutput"></div>
                                    <script type="text/javascript">
                                        jQuery.noConflict();
                                        function changeMtomState(opName, selectId) {
                                            var state = document.getElementById(selectId).value;
                                            var url = 'mtom_ajaxprocessor.jsp?serviceName=<%= serviceName%>&opName=' + opName + '&mtomState=' + state;
                                            jQuery("#mtomOutput").load(url, null, function (
                                                    responseText, status, XMLHttpRequest) {
                                                if (status != "success") {
                                                    CARBON.showErrorDialog('<fmt:message key="could.not.change.mtom.state"/>');
                                                } else {
                                                    CARBON.showInfoDialog('<fmt:message key="changed.mtom.state"/>');
                                                }
                                            });
                                        }
                                    </script>
                                </td>
                                <td width="50%">
                                    <a title="Edit operation specific parameters"
                                       href="parameters.jsp?serviceName=<%=serviceName%>&opName=<%=opName%>">
                                        <img src="images/parameters.gif"
                                             alt="Manage operation parameters"/>
                                        <fmt:message key="parameters"/>
                                    </a>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td width="10px">&nbsp;</td>
                    <td>
                        <%
                            if (statisticsComponentFound) {
                        %>
                        <div id="result"></div>
                        <script type="text/javascript">
                            jQuery.noConflict();
                            var refresh;
                            function refreshStats() {
                                var url = "../statistics/operation_stats_ajaxprocessor.jsp?serviceName=<%= serviceName%>&opName=<%= opName%>";
                                jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                                    if (status != "success") {
                                        stopRefreshStats();
                                        document.getElementById('result').innerHTML = responseText;
                                    }
                                });
                            }
                            function stopRefreshStats() {
                                if (refresh) {
                                    clearInterval(refresh);
                                }
                            }
                            jQuery(document).ready(function() {
                                refreshStats();
                                refresh = setInterval("refreshStats()", 6000);
                            });
                        </script>
                        <%
                            }
                        %>
                    </td>
                </tr>
            </table>
        </div>
    </div>

    <form name="dataForm" method="post" action="">
        <input name="backURL" type="hidden" id="hiddenField" value="">
    </form>

    <script type="text/javascript">
        alternateTableRows('opDetailsTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('opConfigurationTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
