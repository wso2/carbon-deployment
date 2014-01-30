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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient"%>
<%@ page import="org.wso2.carbon.module.mgt.stub.types.ModuleMetaData"%>
<%@ page
    import="org.wso2.carbon.module.mgt.ui.util.ModuleManagementUtils"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>
<script type="text/javascript" src="js/modulemgt.js"></script>

<fmt:bundle basename="org.wso2.carbon.module.mgt.ui.i18n.Resources">
<carbon:breadcrumb
		label="service.breadcrumbtext"
		resourceBundle="org.wso2.carbon.module.mgt.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />
 <carbon:jsi18n
		resourceBundle="org.wso2.carbon.module.mgt.ui.i18n.JSResources"
		request="<%=request%>" />
<%
            String serviceName = request.getParameter("serviceName");

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
             (ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            ModuleMetaData[] modules;
            try {

                ModuleManagementClient client = new ModuleManagementClient(configContext, serverURL,
                            cookie, false);

                modules = client.listModulesForService(serviceName);
            } catch (ModuleAdminServiceModuleMgtExceptionException e) {
                String url = "service_modules.jsp?serviceName=" + serviceName;
                ModuleManagementUtils.handleModuleMgtErrors(e,request,response,url);
                return;
            }
%>
<script type="text/javascript">
    function engagePerService(serviceName,moduleId) {
        location.href = "./service_eng_ajaxprocessor.jsp?action=engage&moduleId="+moduleId+"&serviceName="+serviceName;
    }

    function disengagePerService(serviceName,moduleId) {
        disengagePerService.serviceName = serviceName;
        disengagePerService.moduleId = moduleId;
        CARBON.showConfirmationDialog(jsi18n["do.you.really.want.to.disengage"] + " " + moduleId + " " + jsi18n["module.from"] + " " + serviceName + " " + jsi18n["service"]+" ?",disengagePerServiceCallback,null);
    }

    function disengagePerServiceCallback() {
        location.href = "./service_eng_ajaxprocessor.jsp?action=disengage&moduleId="+disengagePerService.moduleId+"&serviceName="+disengagePerService.serviceName;
    }

</script>
<style>
    div#workArea table.styledLeft tbody tr td {
        height: auto !important;
        vertical-align: top !important;
    }
</style>
<div id="middle">
  <h2><fmt:message key="service.headertext">
          <fmt:param value="<%=serviceName%>"/>
      </fmt:message>
  </h2>
    <div id="workArea">

        <table width="100%">
            <tr style="padding:0;border:0;!important">
                <td class="formRow" style="padding:0;border:0;!important">
                    <fmt:message key="module"/> &nbsp;&nbsp;&nbsp;
                    <select id="moduleSelector">
                        <%
                            boolean isListEmpty = true;
                            for (ModuleMetaData module : modules) {
                                if ("wso2throttle".equals(module.getModulename()) || "wso2caching".equals(module.getModulename()) ||
                                        module.getEngagedGlobalLevel() || module.getEngagedServiceGroupLevel() || module.getEngagedServiceLevel()) {
                                    continue;
                                }
                                isListEmpty = false;
                        %>
                        <option value="<%=module.getModuleId()%>"><%=module.getModuleId()%>
                        </option>
                        <% } %>
                    </select>
                    <% if (isListEmpty) {%>
                    <input disabled='true' onclick="engagePerService('<%=serviceName%>', document.getElementById('moduleSelector').value);"
                           value=" <fmt:message key="engage"/> " type="button" class="button"/>
                    <% } else { %>
                    <input onclick="engagePerService('<%=serviceName%>', document.getElementById('moduleSelector').value);"
                           value=" <fmt:message key="engage"/> " type="button" class="button"/>
                    <% } %>
                </td>
            </tr>
        </table>
        <p>&nbsp;</p>
        <h4><fmt:message key="currently.engaged"/></h4>
        <table class="styledLeft" width="100%" id="outerTable" cellspacing="0" cellpadding="0">
            <thead>
            <tr>
                <th width="33%"><fmt:message key="level.service"/></th>
                <th width="33%"><fmt:message key="level.service.group"/></th>
                <th width="33%"><fmt:message key="level.global"/></th>
            </tr>
            </thead>
            <%
                String globalModules = null;
                String sgModules = null;
                String serviceModules = null;

                String rowContent;
                String disengageText;
                for (ModuleMetaData module : modules) {
                    disengageText = "<a title=\"Disengage " + module.getModulename() + " module\" " +
                                    "onclick=disengagePerService('" + serviceName + "','" + module.getModuleId() + "');" +
                                    " href=\"#\"> <img src=\"images/disengage.gif\" alt=\"Disengage\"/></a> &nbsp; ";
                    rowContent = "<tr  style='padding:0 !important; border: 0 !important;'><td  style='padding:0 !important; border: 0 !important;'>" + module.getModuleId() + "</td></tr>";

                    if (module.getEngagedGlobalLevel()) {
                        globalModules = (globalModules == null) ? rowContent : globalModules + rowContent;
                    } else if (module.getEngagedServiceGroupLevel()) {
                        sgModules = (sgModules == null) ? rowContent : sgModules + rowContent;
                    } else if (module.getEngagedServiceLevel()) {
                        rowContent = "<tr  style='padding:0 !important; border: 0 !important;'><td style='padding:0 !important; border: 0 !important;'>" + disengageText + module.getModuleId() + "</td></tr>";
                        serviceModules = (serviceModules == null) ? rowContent : serviceModules + rowContent;
                    }
                }
            %>
            <tr>
                <td valign="top">
                    <% if (serviceModules != null) { %>
                    <table id="table1" style="padding:0 !important;border:0 !important;" width="100%">
                        <%=serviceModules%>
                    </table>
                    <% } else { %>
                    <fmt:message key="no.modules"/>
                    <% } %>
                </td>
                <td valign="top">
                    <% if (sgModules != null) { %>
                    <table id="table2" style="padding:0 !important; border:0 !important;" width="100%">
                        <%=sgModules%>
                    </table>
                    <% } else { %>
                    <fmt:message key="no.modules"/>
                    <% } %>
                </td>
                <td valign="top">
                    <% if (globalModules != null) { %>
                    <table id="table3" style="padding:0 !important;border:0 !important;" width="100%">
                        <%=globalModules%>
                    </table>
                    <% } else { %>
                    <fmt:message key="no.modules"/>
                    <% } %>
                </td>
            </tr>
        </table>
    </div>
</div>
<script type="text/javascript">
    alternateTableRows('table1', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('table2', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('table3', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>