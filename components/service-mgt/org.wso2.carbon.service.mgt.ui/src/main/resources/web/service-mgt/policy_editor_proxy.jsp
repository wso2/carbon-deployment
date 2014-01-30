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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="policy.editor.select.a.policy.to.edit"
		resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

<script type="text/javascript">

    function createCookie(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            var expires = "; expires=" + date.toGMTString();
        }
        else var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    }

    function readCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }

    function cleanBreadCrumb(){
        // Read the existing breadcrumb value
        var breadCrumb = readCookie("current-breadcrumb");

        // Set the new value after removing policy pages
        var newBreadCrumb = breadCrumb.split("*")[0];
        createCookie("current-breadcrumb", newBreadCrumb);
    }

</script>

<%
    String serviceName = CharacterEncoder.getSafeText(request.getParameter("serviceName"));
    String moduleName = CharacterEncoder.getSafeText(request.getParameter("moduleName"));
    String moduleVersion = CharacterEncoder.getSafeText(request.getParameter("moduleVersion"));
    String action = CharacterEncoder.getSafeText(request.getParameter("action"));
    String operationName = CharacterEncoder.getSafeText(request.getParameter("operationName"));
    String bindingName = CharacterEncoder.getSafeText(request.getParameter("bindingName"));
    String messageType = CharacterEncoder.getSafeText(request.getParameter("messageType"));
    String policyType = CharacterEncoder.getSafeText(request.getParameter("policyType"));

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceAdminClient client;
    String servicePolicy =
            "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"></wsp:Policy>";
    try {
        client = new ServiceAdminClient(cookie, backendServerURL, configContext,
                                        request.getLocale());

        if ((action != null) && (action.equals("save"))) {
            if (policyType.equals("ServicePolicy")) {
                client.setServicePolicy(serviceName, request.getParameter("policy"));
            } else if (policyType.equals("ServiceOperationPolicy")) {
                client.setServiceOperationPolicy(serviceName, operationName,
                                                 request.getParameter("policy"));
            } else if (policyType.equals("ServiceOperationMessagePolicy")) {
                client.setServiceOperationMessagePolicy(serviceName, operationName, messageType,
                                                        request.getParameter("policy"));
            } else if (policyType.equals("BindingPolicy")) {
                client.setBindingPolicy(serviceName, bindingName, request.getParameter("policy"));
            } else if (policyType.equals("BindingOperationPolicy")) {
                client.setBindingOperationPolicy(serviceName, bindingName, operationName,
                                                 request.getParameter("policy"));
            } else if (policyType.equals("BindingOperationMessagePolicy")) {
                client.setBindingOperationMessagePolicy(serviceName, bindingName, operationName,
                                                        messageType,
                                                        request.getParameter("policy"));
            } else if (policyType.equals("ModulePolicy")) {
                client.setModulePolicy(moduleName, moduleVersion, request.getParameter("policy"));
            } else {
                // Fallback to the legacy method, which applies policies to bindings
                client.setPolicy(serviceName, request.getParameter("policy"));
            }

            if (policyType.equals("ModulePolicy")) {
%>
<script type="text/javascript">
    cleanBreadCrumb();
    
    location.href =
    '../modulemgt/module_info.jsp?moduleName=<%=moduleName%>&moduleVersion=<%=moduleVersion%>'
</script>
<%
} else {
%>
<script type="text/javascript">
    cleanBreadCrumb();
    
    location.href = 'service_info.jsp?serviceName=<%=serviceName%>'
</script>
<%
    }        
} else if ((action != null) && (action.equals("getpolicy"))) {

    if (policyType.equals("ServicePolicy")) {
        servicePolicy = client.getPolicy(serviceName).toString();
    } else if (policyType.equals("ServiceOperationPolicy")) {
        servicePolicy = client.getOperationPolicy(serviceName, operationName).toString();
    } else if (policyType.equals("ServiceOperationMessagePolicy")) {
        servicePolicy = client.getOperationMessagePolicy(serviceName, operationName, messageType)
                .toString();
    } else if (policyType.equals("BindingPolicy")) {
        servicePolicy = client.getBindingPolicy(serviceName, bindingName).toString();
    } else if (policyType.equals("BindingOperationPolicy")) {
        servicePolicy = client.getBindingOperationPolicy(serviceName, bindingName, operationName)
                .toString();
    } else if (policyType.equals("BindingOperationMessagePolicy")) {
        servicePolicy = client.getBindingOperationMessagePolicy(serviceName, bindingName,
                                                                operationName, messageType)
                .toString();
    } else if (policyType.equals("ModulePolicy")) {
        servicePolicy = client.getModulePolicy(moduleName, moduleVersion).toString();
    } else {
        // Fallback to the legacy method, which gets policies from the bindings
        servicePolicy = client.getPolicy(serviceName).toString();
    }
%>
<div style="display: none;">
    <form name="frmPolicyData" action="../policyeditor/index.jsp" method="post">
        <input type="hidden" name="policy" id="policy">
        <input type="hidden" name="visited" id="visited">
        <textarea id="txtPolicy" rows="50" cols="50"><%=servicePolicy%>
        </textarea>
        <input type="hidden" name="callbackURL"
               value="../service-mgt/policy_editor_proxy.jsp?serviceName=<%=serviceName%>&moduleName=<%=moduleName%>&moduleVersion=<%=moduleVersion%>&action=save&operationName=<%=operationName%>&bindingName=<%=bindingName%>&messageType=<%=messageType%>&policyType=<%=policyType%>"/>
    </form>
</div>                      
<script type="text/javascript">
    // Handling the browser back button for Firefox. The IE back button is handled form the policy editor index.jsp page
    if (document.frmPolicyData.visited.value == "")
    {
        // This is a fresh page load
        document.frmPolicyData.visited.value = "1";

        function submitForm() {
            document.getElementById("policy").value = document.getElementById("txtPolicy").value
            document.frmPolicyData.submit();
        }
        submitForm();
    }
    else
    {
        location.href = '<%=request.getHeader("Referer")%>';
    }
</script>
<%
} else {
    // Retrieving service meta-data
    ServiceMetaData serviceData = client.getServiceData(serviceName);
%>
<div id="middle">
<h2><fmt:message key="policy.editor.select.a.policy.to.edit"/></h2>

<div id="workArea">
<table id="service-hierarchy-table" class="styledLeft">
<thead>
    <tr>
        <th colspan="3"><fmt:message key="service.hierarchy"/></th>
    </tr>
</thead>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
<tr>
    <td class="sub-header" colspan="2">
        
            <input type="hidden" name="action" value="getpolicy"/>
            <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
            <input type="hidden" name="policyType" value="ServicePolicy"/>

            <fmt:message key="service"/> <%=serviceName%>
      </td>
<td class="sub-header">      
            <input class="button" type="submit" value="<fmt:message key="edit.policy"/>"/>
        
    </td>
</tr>
</form>
<%
    String[] operations = serviceData.getOperations();
    if (operations.length > 0) {
%>
 <form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
	<input type="hidden" name="action" value="getpolicy"/>
            <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
            <input type="hidden" name="policyType" value="ServiceOperationPolicy"/>
<tr>
    <td colspan="2">
              <fmt:message key="operation"/>
                <select name="operationName">
                    <%
                        for (int x = 0; x < operations.length; x++) {
                    %>
                    <option value="<%=operations[x]%>"><%=operations[x]%>
                    </option>
                    <%
                        }
                    %>
                </select>
            </td>
            <td><input class="button" type="submit"
                                             value="<fmt:message key="edit.policy"/>"/>
        
    </td>
</tr>
</form>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
            <input type="hidden" name="action" value="getpolicy"/>
            <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
            <input type="hidden" name="policyType" value="ServiceOperationMessagePolicy"/>
<tr>
    <td width="30%">
	    <fmt:message key="operation"/>
                <select name="operationName">
                    <%
                        for (int x = 0; x < operations.length; x++) {
                    %>
                    <option value="<%=operations[x]%>"><%=operations[x]%>
                    </option>
                    <%
                        }
                    %>
                </select>
            </td>
            <td width="30%">
                <nobr>
                    <input type="radio" name="messageType" id="in.msg" value="In" checked>
                    <label for="in.msg">
                        <fmt:message key="in.message"/>
                    </label>
                    <input type="radio" name="messageType" id="out.msg" value="Out">
                    <label for="out.msg">
                        <fmt:message key="out.message"/>
                    </label>
                </nobr>
            </td>
            <td><input class="button" type="submit"
                                             value="<fmt:message key="edit.policy"/>"/>
        
    </td>
</tr>
</form>
<%
} else {
%>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
            <input type="hidden" name="action" value="getpolicy"/>
            <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
            <input type="hidden" name="policyType" value="ServicePolicy"/>
<tr class="sub-header">
    <td colspan="2"><fmt:message key="service"/> <%=serviceName%>
            
            <td><input class="button" type="submit"
                                             value="<fmt:message key="edit.policy"/>"/>
        
    </td>
</tr>
</form>
<%
    }
%>
<%
    String[] bindings = client.getServiceBindings(serviceName);
    if (bindings.length > 0) {
%>
</table>
<br>
<br>
<table id="binding-hierarchy-table" class="styledLeft">
    <thead>
        <tr>
            <th colspan="3"><fmt:message key="binding.hierarchy"/></th>
        </tr>
    </thead>
            <%


                for (int y = 0; y < bindings.length; y++) {


            %>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
                    <input type="hidden" name="action" value="getpolicy"/>
                    <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
                    <input type="hidden" name="bindingName" value="<%=bindings[y]%>"/>
                    <input type="hidden" name="policyType" value="BindingPolicy"/>
        <tr>
            <td class="sub-header" colspan="2">
                

                    <fmt:message
                            key="binding"/> <%=bindings[y]%>
                    </td>
                    <td class="sub-header"><input class="button"  type="submit"
                                                     value="<fmt:message key="edit.policy"/>"/>
                    
                
            </td>
        </tr>
</form>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
                    <input type="hidden" name="action" value="getpolicy"/>
                    <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
                    <input type="hidden" name="bindingName" value="<%=bindings[y]%>"/>
                    <input type="hidden" name="policyType" value="BindingOperationPolicy"/>
        <tr>
            <td colspan="2">
                

                        <fmt:message key="operation"/>
                        <select name="operationName">
                            <%
                                for (int x = 0; x < operations.length; x++) {
                            %>
                            <option value="<%=operations[x]%>"><%=operations[x]%>
                            </option>
                            <%
                                }
                            %>
                        </select>
                    </td>
                    <td><input class="button" type="submit"
                                                     value="<fmt:message key="edit.policy"/>"/>
                    
                
            </td>
        </tr>
</form>
<form action="policy_editor_proxy.jsp" name="frmPolicyMetaData">
                    <input type="hidden" name="action" value="getpolicy"/>
                    <input type="hidden" name="serviceName" value="<%=serviceName%>"/>
                    <input type="hidden" name="bindingName" value="<%=bindings[y]%>"/>
                    <input type="hidden" name="policyType" value="BindingOperationMessagePolicy"/>
        <tr>
            <td width="30%">
                
                        <fmt:message key="operation"/> <select name="operationName">
                        <%
                            for (int x = 0; x < operations.length; x++) {
                        %>
                        <option value="<%=operations[x]%>"><%=operations[x]%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                    </td>
                    <td width="30%">
                        <nobr>
                            <input type="radio" name="messageType" id="op.in.msg" value="In"
                                   checked>
                            <label for="op.in.msg">
                                <fmt:message key="in.message"/>
                            </label>
                            <input type="radio" name="messageType" id="op.out.msg" value="Out">
                            <label for="op.out.msg">
                                <fmt:message key="out.message"/>
                            </label>
                        </nobr>
                    </td>
                    <td><input class="button" type="submit"
                                                     value="<fmt:message key="edit.policy"/>"/>

                
            </td>
        </tr>
</form>
            <%


                    }
                }


            %>
</table>
</div>
</div>
<script type="text/javascript">
    alternateTableRows('service-hierarchy-table', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('binding-hierarchy-table', 'tableEvenRow', 'tableOddRow');
</script>
<%
    }
} catch (Exception exception) {
        CarbonUIMessage.sendCarbonUIMessage(exception.getMessage(), CarbonUIMessage.ERROR, request, exception);
 %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
        return;
    }
%>
</fmt:bundle>
