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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient"%>
<%@ page import="org.wso2.carbon.module.mgt.stub.types.ModuleMetaData"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<script type="text/javascript" src="js/modulemgt.js"></script>
<fmt:bundle basename="org.wso2.carbon.module.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="module.information"
                       resourceBundle="org.wso2.carbon.module.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
<%
            String moduleName = request.getParameter("moduleName");
            String moduleVersion = request.getParameter("moduleVersion");

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
             (ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ModuleMetaData module = null;
            String globallyEngaged = "NO";

            try {
                ModuleManagementClient client = new ModuleManagementClient(configContext, serverURL,
                        cookie, false);
                module = client.getModuleInfo(moduleName,moduleVersion);
                globallyEngaged = module.getEngagedGlobalLevel() ? "YES" : "NO";
            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
            <script type="text/javascript">
                   location.href = "../admin/error.jsp";
            </script>
<%
                return;
            } 

%>
<div id="middle">
    <h2> <%=moduleName%> <fmt:message key="module"/> <%=moduleVersion%></h2>
    <div id="workArea">
    <table>
        <tr>
       <td>
        <table class="styledLeft" id="moduleInfo" width="100%">
	        <thead>
	            <tr>
	                <th colspan="2"><fmt:message key="module.information"/></th>
	            </tr>
	        </thead>
	        <tbody>
	            <tr>
	                <td><fmt:message key="module.name"/></td>
	                <td><%= module.getModulename()%></td>
	            </tr>
	            <tr>
	                <td><fmt:message key="module.version"/></td>
                    <td><%= module.getModuleVersion() == null ? "" : module.getModuleVersion()%></td>
                </tr>
	            <tr>
	                <td><fmt:message key="module.description"/></td>
                    <% if(module.getDescription() != null) { %>
                        <td><%= module.getDescription()%></td>
                    <% } else { %>
                        <td>No description found</td>
                    <% } %>	                
	            </tr>  
	            <tr>
	                <td><fmt:message key="globally.status"/></td>
	                <td><%=globallyEngaged%></td>
	            </tr>          
	        </tbody>
	    </table>
	   </td>
	</tr>
    </table>
</div>
<script type="text/javascript">
    alternateTableRows('moduleInfo', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('moduleConfig', 'tableEvenRow', 'tableOddRow');
</script>    
</fmt:bundle>
