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
<%@ page
        import="org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient" %>
<%@ page import="org.wso2.carbon.module.mgt.ui.client.Parameter" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<jsp:include page="../highlighter/header.jsp"/>

<fmt:bundle basename="org.wso2.carbon.module.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="module.parameters.breadcrumb.text"
                   resourceBundle="org.wso2.carbon.module.mgt.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<%
    String moduleName = request.getParameter("moduleName");
    String moduleVersion = request.getParameter("moduleVersion");
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    List<Parameter> parameters;
    try {
        ModuleManagementClient stub = new ModuleManagementClient(configContext, backendServerURL, cookie, false);
        parameters = stub.getModuleParameters(moduleName, moduleVersion);
    } catch (Exception e) {
%>
<jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<div id="middle">
    <h2>
        <fmt:message key="module.parameters">
            <fmt:param value="<%=moduleName%>"/>
        </fmt:message>
    </h2>

    <div id="workArea">

        <script type="text/javascript">
            function removeParameter(parameter) {
                CARBON.showConfirmationDialog("<fmt:message key="remove.parameters.prompt"/> '" + parameter + "' ?",
                        function() {
                            location.href = "remove_parameters_ajaxprocessor.jsp?moduleName=<%=moduleName%>&moduleVersion=<%=moduleVersion%>&parameterName=" + parameter;
                        },
                        null);
            }

            function addParameter() {
                CARBON.showInputDialog("<fmt:message key="new.parameter.name"/>",
                        function okFunction(paramName) {
                            if (document.getElementById(paramName) != null) {
                                CARBON.showWarningDialog("Module parameter " + paramName + " already exists");
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

                            var oCell = newRow.insertCell(-1);
                            oCell.innerHTML = paramName;
                            oCell.width = "15%";

                            oCell = newRow.insertCell(-1);
                            oCell.innerHTML =
                            "<textarea rows=\"1\" cols=\"50\" name=" + paramName +"__"+">" +
                            "TODO: <fmt:message key="enter.value.here"/>" +
                            "</textarea> <input type=\"hidden\" name=" + paramName + " value='&lt;parameter name=\"" + paramName + "\" locked=\"false\"&gt;&lt;/parameter&gt;' />";

                            oCell = newRow.insertCell(-1);
                            oCell.innerHTML =
                            "<a title='<fmt:message key="remove.parameter"/>'" +
                            "onclick=\"removeParameter('" + paramName + "');return false;\"" +
                            " href='#' class=\"icon-link\" " +
                            "style=\"background-image:url(../admin/images/delete.gif);\" ><fmt:message key="delete"/></a>";

                            alternateTableRows('paramTable', 'tableEvenRow', 'tableOddRow');
                            return true;
                        },
                        function cancelFunction() {
                            return false;
                        },
                        function closeCallbackFunction() {
                            return false;
                        });
            }

            function getModuleInfo(moduleName, moduleVersion) {
                location.href = "./module_info.jsp?moduleName=" + moduleName + "&moduleVersion=" + moduleVersion + '&ordinal=1';
            }
        </script>
        <form action="setParameters.jsp" method="post">
            <input type="hidden" name="moduleName" value="<%=moduleName%>"/>
            <input type="hidden" name="moduleVersion" value="<%=moduleVersion%>"/>
            <table class="styledLeft">
                <tr>
                    <td class="formRow">
                        <div id="paramTableDiv">
                            <%
                                if (parameters.size() > 0) {
                            %>
                            <table class="styledLeft" id="paramTable" style="margin-left: 0px;">
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
                                        <textarea rows="1" cols="50"
                                                  name="<%= param.getName()+"__"%>"><%= param.getParamValue()%>
                                        </textarea>
                                        <input type="hidden" name="<%= param.getName()%>" value='<%= param.getValue()%>'>
                                    </td>
                                    <td>
                                        <a title="<fmt:message key="remove.parameter"/>"
                                           onclick="removeParameter('<%= param.getName()%>');return false;"
                                           href="#" class="icon-link-nofloat"
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
                               value="<fmt:message key="update.parameter"/>" disabled="disabled"/>
                        <%
                        } else {
                        %>
                        <input type="submit" class="button" id="updateBtn"
                               value="<fmt:message key="update.parameter"/>"/>
                        <%
                            }
                        %>
                        &nbsp;&nbsp;
                        <input onclick="addParameter();return false;"
                               value="<fmt:message key="add.new.parameter"/>"
                               type="button" class="button">
                        &nbsp;&nbsp;
                        <input onclick="getModuleInfo('<%=moduleName%>','<%=moduleVersion%>');return false;"
                               value="<fmt:message key="cancel"/>"
                               type="button" class="button">
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>