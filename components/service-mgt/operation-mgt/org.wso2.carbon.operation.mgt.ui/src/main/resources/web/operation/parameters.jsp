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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.operation.mgt.ui.client.OperationAdminClient" %>
<%@ page import="org.wso2.carbon.operation.mgt.ui.client.Parameter" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<script type="text/javascript" src="js/operations.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>


<carbon:breadcrumb label="operation.parameters.breadcrumbtext"
                   resourceBundle="org.wso2.carbon.operation.mgt.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<%
    String serviceName = request.getParameter("serviceName");
    if (serviceName == null) {
        serviceName = (String) session.getAttribute("serviceName");
        session.removeAttribute("serviceName");
    }
    String opName = request.getParameter("opName");
    if (opName == null) {
        opName = (String) session.getAttribute("opName");
        session.removeAttribute("opName");
    }

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    List<Parameter> parameters;
    OperationAdminClient client = new OperationAdminClient(cookie, backendServerURL, configContext);
    try {
        if (request.getParameter("update") != null) {
            Map<String, String[]> map = request.getParameterMap();
            List<String> params = new ArrayList<String>();
            for (String key : map.keySet()) {
                String paramValue = map.get(key)[0].trim();
                if (paramValue.startsWith("<parameter")) {
                    params.add(paramValue);
                }
            }
            client.setOperationParameters(serviceName, opName, params);
            sendParametersUpdatedMessage(request);
        } else if (request.getParameter("delete") != null) {
            String parameter = request.getParameter("parameter");
            client.removeOperationParameter(serviceName, opName, parameter);
        }
        parameters = client.getOperationParameters(serviceName, opName);
    } catch (Exception e) {
        response.setStatus(500);
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
            <fmt:message key="operation.parameters">
                <fmt:param value="<%= serviceName%>"/>
                <fmt:param value="<%= opName%>"/>
            </fmt:message>
        </h2>

        <div id="workArea">
            <script type="text/javascript">

                function removeOperationParameter(parameter) {
                    CARBON.showConfirmationDialog("<fmt:message key="remove.operation.parameters.prompt"/> '" + parameter + "' ?",
                            function() {
                                location.href = "parameters.jsp?delete=true&serviceName=<%=serviceName%>&opName=<%=opName%>&parameter=" + parameter;
                            },
                            null);
                }

                function addOperationParameter() {

                    CARBON.showInputDialog("<fmt:message key="operation.parameter.name"/>",
                            function okFunction(paramName) {
                                if (document.getElementById(paramName) != null) {
                                    CARBON.showWarningDialog("Operation parameter " + paramName + " already exists");
                                    return;
                                } else  if (paramName == null || jQuery.trim(paramName[0]) == '') {
                                    return;
                                }

                                paramName = jQuery.trim(paramName[0]);

                                var paramTable = document.getElementById("paramTable");

                                if (paramTable == null) {
                                    document.getElementById('paramTableDiv').innerHTML =
                                    '<table class="styledLeft" id="paramTable" width="100%">' +
                                    '<thead>' +
                                    '<tr>' +
                                    '<th width="15%"><fmt:message key="name"/></th>' +
                                    '<th colspan="2"><fmt:message key="value"/></th>' +
                                    '</tr>' +
                                    '</thead>' +
                                    '<tbody>' +
                                    '</tbody>' +
                                    '</table>';
                                    document.getElementById('updateBtn').disabled = '';
                                }

                                //add a row to the rows collection and get a reference to the newly added row
                                var newRow = document.getElementById("paramTable").insertRow(-1);

                                //        newRow.className = "even";

                                //add 6 cells (<th>) to the new row and set the innerHTML to contain text boxes
                                var oCell = newRow.insertCell(-1);
                                oCell.innerHTML = paramName;
                                oCell.width = "15%";

                                oCell = newRow.insertCell(-1);
                                oCell.innerHTML =
                                "<textarea rows=\"4\" cols=\"70\" name=" + paramName + ">" +
                                "&lt;parameter name=\"" + paramName + "\" locked=\"false\"&gt;" +
                                "TODO: <fmt:message key="enter.value.here"/>" +
                                "&lt;/parameter&gt;" +
                                "</textarea>";

                                oCell = newRow.insertCell(-1);
                                oCell.innerHTML =
                                "<a title='<fmt:message key="remove.operation.parameter"/>'" +
                                "onclick=\"removeOperationParameter('" + paramName + "');return false;\"" +
                                "href='#' class=\"icon-link\" " +
                                "style=\"background-image:url(../admin/images/delete.gif);\"><fmt:message key="delete"/></a>";

                                alternateTableRows('paramTable', 'tableEvenRow', 'tableOddRow');
                            },
                            function cancelFunction() {
                                return false;
                            },
                            function closeCallbackFunction() {
                                return false;
                            });
                }
            </script>
            <form action="parameters.jsp" method="post">
                <input type="hidden" name="serviceName" value="<%= serviceName%>"/>
                <input type="hidden" name="opName" value="<%= opName%>"/>
                <input type="hidden" name="update" value="true"/>
                <table class="styledLeft" width="100%">
                    <tr>
                        <td class="formRow">
                            <div id="paramTableDiv">
                                <%
                                    if (parameters.size() > 0) {
                                %>
                                <table class="styledLeft" id="paramTable" width="100%">
                                    <thead>
                                    <tr>
                                        <th width="15%"><fmt:message key="name"/></th>
                                        <th colspan="2"><fmt:message key="value"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        for (Parameter param : parameters) {
                                    %>
                                    <tr style="clear: both;">
                                        <td width="15%">
                                            <%= param.getName()%>
                                        </td>
                                        <%
                                            if (param.isLocked()) {
                                        %>
                                        <td>
                                            <pre name="code"
                                                 class="xml:nogutter:nocontrols"
                                                 style="padding-top:0 !important;"><%= param.getValue()%></pre>
                                        </td>
                                        <td>&nbsp;</td>
                                        <%
                                        } else {
                                        %>
                                        <td>
                                            <textarea rows="4" cols="70"
                                                      name="<%= param.getName()%>"><%= param.getValue()%>
                                            </textarea>
                                        </td>
                                        <td>
                                            <a title="<fmt:message key="remove.operation.parameter"/>"
                                               onclick="removeOperationParameter('<%= param.getName()%>');return false;"
                                               href="#" class="icon-link"
                                               style="background-image:url(../admin/images/delete.gif);">
                                                <fmt:message key="delete"/>
                                            </a>
                                        </td>
                                        <% } %>
                                    </tr>
                                    <%
                                        }
                                    %>
                                    </tbody>
                                </table>
                                <script type="text/javascript">
                                    dp.SyntaxHighlighter.HighlightAll('code');
                                </script>
                                <% } else { %>
                                <p><fmt:message key="no.parameters.found"/></p>
                                <% } %>
                            </div>
                            <% if (parameters.size() > 0) { %>
                            <script type="text/javascript">
                                alternateTableRows('paramTable', 'tableEvenRow', 'tableOddRow');
                            </script>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <%
                                if (parameters.size() == 0) {
                            %>
                            <input type="submit" class="button" id="updateBtn"
                                   value="<fmt:message key="update"/>" disabled="disabled"/>
                            <%
                            } else {
                            %>
                            <input type="submit" class="button" id="updateBtn"
                                   value="<fmt:message key="update"/>"/>
                            <%
                                }
                            %>
                            &nbsp;&nbsp;
                            <input onclick="addOperationParameter();return false;"
                                   value="<fmt:message key="add.new"/>"
                                   type="button" class="button">
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle><%!
    private void sendParametersUpdatedMessage(HttpServletRequest request) {
        ResourceBundle bundle =
                    ResourceBundle.getBundle("org.wso2.carbon.operation.mgt.ui.i18n.Resources",
                                             request.getLocale());
        CarbonUIMessage.sendCarbonUIMessage(bundle.getString("updated.parameters"),
                                            CarbonUIMessage.INFO, request);
    }
%>