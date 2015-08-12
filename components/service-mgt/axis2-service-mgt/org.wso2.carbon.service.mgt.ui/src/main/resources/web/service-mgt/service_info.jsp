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
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.osgi.framework.BundleContext" %>
<%@ page import="org.osgi.util.tracker.ServiceTracker" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceManagementUIExtender" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.ArrayList" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<%
    response.setHeader("Cache-Control", "no-cache");

    String serviceName = CharacterEncoder.getSafeText(request.getParameter("serviceName"));
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceAdminClient client;
    ServiceMetaData service;
    try {
        client = new ServiceAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        service = client.getServiceData(serviceName);
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

<fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
<carbon:breadcrumb
		label="service.dashboard"
		resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>"/>

<%
    boolean statisticsComponentFound = CarbonUIUtil.isContextRegistered(config, "/statistics/");
    if (statisticsComponentFound) {
//        int responseTimeGraphWidth = 500;
//    responseTimeGraphWidth = Utils.getValue(session, request, responseTimeGraphWidth, "responseTimeGraphWidth");
%>
        <script type="text/javascript" src="../statistics/js/statistics.js"></script>
        <script type="text/javascript" src="../statistics/js/graphs.js"></script>

        <script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
        <script type="text/javascript" src="../admin/js/excanvas.js"></script>
        <script type="text/javascript" src="global-params.js"></script>
        <script type="text/javascript">
            initResponseTimeGraph('50');
        </script>
<%
    }
%>
<jsp:include page="javascript_include.jsp" />

<script type="text/javascript">
    function submitHiddenForm(action) {
        document.getElementById("hiddenField").value = location.href + "&ordinal=1";
        document.dataForm.action = action;
        document.dataForm.submit();
    }
</script>
<script type="text/javascript">
   function sendToURLMapper(myepr){
            jQuery.ajax({
                            type: "POST",
                            url: "../urlmapper/index.jsp",
                            data: "type=add&carbonEndpoint=" + myepr,
                            success: function(msg){
                               
                            }
                        });
    }   
</script> 
<!--<script type="text/javascript">
 /*function add(myepr){
        CARBON.showInputDialog("Enter Service Specification identifier :\n",function(inputVal){
            jQuery.ajax({
                type: "POST",
                url: "../urlmapper/foo_ajaxprocessor.jsp",
                data: "type=add&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal + "&endpointType=Endpoint_1",
                success: function(msg){
                     CARBON.showInputDialog( msg.trim() );
                }
            });
        });
    }*/
</script>-->

<%
    String endPointStr = "";
    String[] eps = service.getWsdlPorts();
    String[] epsTypes = service.getWsdlPortTypes();

    boolean isSecured = false;
    isSecured = service.getSecurityScenarioId() != null ? true : false;

    for (int i = 0; i < eps.length; i++) {

        if (isSecured) {
            if (epsTypes[i].equalsIgnoreCase("https")) {
                endPointStr += eps[i] + ",";
                continue;
            }
        } else {
            if (epsTypes[i].equalsIgnoreCase("https") || epsTypes[i].equalsIgnoreCase("http"))
                endPointStr += eps[i] + ",";
            continue;
        }
    }
    if(!"".equals(endPointStr)) {
        endPointStr = endPointStr.substring(0, endPointStr.length() - 1);
    }

%>

<div id="middle">
<h2><fmt:message key="service.dashboard"/> (<%= serviceName%>)</h2>

<div id="workArea">

<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
    <td width="50%">
        <table class="styledLeft" id="serviceInfoTable" style="margin-left: 0px;" width="100%">
            <thead>
                <tr>
                    <th colspan="2" align="left"><fmt:message key="service.details"/></th>
                </tr>
            </thead>
            <tr>
                <td width="30%"><fmt:message key="service.name"/></td>
                <td><%=service.getName()%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="service.description"/></td>
                <td><%=service.getDescription()%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="service.group.name"/></td>
                <td><%=service.getServiceGroupName()%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="deployment.scope"/></td>
                <td><%=service.getScope()%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="service.type"/></td>
                <td>
                    <%=service.getServiceType()%>&nbsp;&nbsp;&nbsp;
                    <img src="../<%= service.getServiceType()%>/images/type.gif"
                         title="<%= service.getServiceType()%>"
                         alt="<%= service.getServiceType()%>"/>
                </td>
            </tr>
        </table>
    </td>

    <td width="10px">&nbsp;</td>

    <td>
        <div id="serviceClientDiv" style="display:<%= service.getActive() ? "'';" : "none;" %>">
        <table class="styledLeft" id="serviceClientTable" style="margin-left: 0px;" width="100%">
            <thead>
                <tr>
                    <th colspan="2" align="left"><fmt:message key="client.operations"/></th>
                </tr>
            </thead>
            <%
                if(!service.getDisableTryit()){
            %>
            <tr>
                <td colspan="2">
                    <a href="<%=service.getTryitURL()%>" class="icon-link" style="background-image:url(images/tryit.gif);" target="_blank">
                        <fmt:message key="try.this.service"/>
                    </a>
                </td>
            </tr>
            <%
                }
            %>
            <%
                    if(CarbonUIUtil.isContextRegistered(config, "/wsdl2code/")){
            %>
            <tr>
                <td colspan="2">
                    <a href="../wsdl2code/index.jsp?generateClient=<%=service.getWsdlURLs()[0]%>&toppage=false&endpoints=<%=endPointStr%>" class="icon-link"
                       style="background-image:url(images/genclient.gif);">
                        <fmt:message key="generate.client"/>
                    </a>
                </td>
            </tr>
            <%
                }
            %>
            <tr>
                <td width="50%">
                    <a href="<%=service.getWsdlURLs()[0]%>" class="icon-link"
                       style="background-image:url(images/wsdl.gif);" target="_blank">
                        WSDL1.1
                    </a>
                </td>
                <td width="50%">
                    <a href="<%=service.getWsdlURLs()[1]%>" class="icon-link"
                       style="background-image:url(images/wsdl.gif);" target="_blank">
                        WSDL2.0
                    </a>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2" align="left">
                    <strong><fmt:message key="endpoints"/></strong>
                </td>
            </tr>
            <%
                String[] eprs = service.getEprs();
                String carbonEndpoint = "";
                if (eprs != null) {
                    for (String epr : eprs) {
                        if (epr != null) {
                        	if (epr.contains("http")||epr.contains("https")) {
                        		carbonEndpoint =epr;
                        	}
                            %>
                <tr>
                    <td colspan="2"><%=epr%>
                    </td>
                 
                </tr>
                   
                            <!--<tr>
                                <td  width="50%"><%--=epr--%>
                                </td>
                               <td width="50%">
                                    <a class="icon-link"
                                       style="background-image:url(images/add.gif);" onclick="add('<%--=epr--%>');" title="Add Service Specific Url">
                                        Add
                                    </a>
                                </td>
                            </tr> -->
                            <%
                        } else {
                                %>
                                 <tr>
                                    <td colspan="2">
                                        <font color="red"><fmt:message key="transport.configuration.error"/></font>
                                    </td>
                                </tr>
                                <%
                        }
                    }%>
                    <tr>
                    <%
                  
                        if(CarbonUIUtil.isContextRegistered(config, "/urlmapper/")){ %>
                         
                              <td colspan="2"><nobr>
                            <a class="icon-link" style="background-image: url(images/url-rewrite.png);"
    href="../urlmapper/index.jsp?carbonEndpoint=<%=carbonEndpoint%>&apptype=service"
                            >
                                URL Mappings
                            </a></nobr>
                        </td>
                           
                               <% 
                      
                       			 }
               				   %>
                </tr> <% 
                } else {
            %>
            <tr>
                <td colspan="2">
                    <font color="red"><fmt:message key="transport.configuration.error2"/></font>
                </td>
            </tr>
            <%
                }
            %>
           
        </table>
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">&nbsp;</td>
</tr>
<tr>
    <td width="50%">
            <table class="styledLeft" id="serviceInfoTable" style="margin-left: 0px;" width="100%">
                <thead>
                    <tr>
                        <th colspan="2" align="left"><fmt:message key="operations"/></th>
                    </tr>
                </thead>
                <tr>
                    <td colspan="2">
                        <nobr>
                            <%
                                request.setAttribute("serviceName", serviceName);
                                request.setAttribute("isActive", String.valueOf(service.getActive()));
                                                request.setAttribute("serviceURL", service.getTryitURL().substring(0, service.getTryitURL().indexOf("?tryit")));
                            %>
                            <div id="serviceStateDiv">
                                <%@ include file="service_state_include.jsp" %>
                            </div>
                        </nobr>
                        <script type="text/javascript">
                            jQuery.noConflict();
                            function changeServiceState(active) {
                                var url = 'change_service_state_ajaxprocessor.jsp?serviceName=<%= request.getAttribute("serviceName")%>&isActive=' + active;
                                jQuery("#serviceStateDiv").load(url, null, function (responseText, status, XMLHttpRequest) {
                                    if (status != "success") {
                                        CARBON.showErrorDialog('<fmt:message key="could.not.change.service.state"/>');
                                    } else {
                                        if(active){
                                            document.getElementById('serviceClientDiv').style.display = '';
                                            document.getElementById('statsDiv').style.display = '';
                                            refresh = setInterval("refreshStats()", 6000);
                                        } else {
                                            document.getElementById('serviceClientDiv').style.display = 'none';
                                            stopRefreshStats();
                                            document.getElementById('statsDiv').style.display = 'none';
                                        }
                                    }
                                });
                            }
                        </script>
                    </td>
                </tr>
            </table>
        </td>

        <td width="10px">&nbsp;</td>

        <td></td>
</tr>
<tr>
    <td colspan="3">&nbsp;</td>
</tr>
<tr>
<%
    List<String> items = null;
    boolean hasUIExtension = false;
    boolean hasExtraConfig = false;
    String serviceTypePath = null;
    BundleContext bundleContext = CarbonUIUtil.getBundleContext();
    hasUIExtension = false;
    if (bundleContext != null) {
        ServiceTracker tracker = new ServiceTracker(bundleContext,
                                                    ServiceManagementUIExtender.class.getName(),
                                                    null);
        tracker.open();
        ServiceManagementUIExtender extender = (ServiceManagementUIExtender) tracker.getService();

        if (extender != null && extender.getItems().size() > 0) {
            hasUIExtension = true;
            items = extender.getItems();
        }
        tracker.close();
    }

    hasExtraConfig = false;
    String serviceType = service.getServiceType();
    serviceTypePath = "/" + serviceType + "/";
    Set resourcePaths = config.getServletContext().getResourcePaths(serviceTypePath);

    if (resourcePaths != null && resourcePaths.contains(serviceTypePath + "extra_config.jsp")) {
        hasExtraConfig = true;
    }

    String colspan = "";
    if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/modify/service") &&
       (hasExtraConfig || hasUIExtension)){
        colspan = " colspan=\"3\" ";
%>
<td>
<table class="styledLeft" id="serviceOperationsTable" style="margin-left: 0px;" width="100%">
<thead>
    <tr>
        <th colspan="2" align="left"><fmt:message key="quality.of.service.configuration"/></th>
    </tr>
</thead>
    <%
        if (hasUIExtension) {
            for (String item : items) {
    %>
    `
    <tr>
        <td colspan="2" align="left"><%= item%>
        </td>
    </tr>
    <%
            }
        }

        if (hasExtraConfig) { //TODO: How to handle this
            String extraConfig = ".." + serviceTypePath + "extra_config.jsp?serviceName=" + serviceName;
    %>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="2" align="left"><strong><fmt:message key="specific.configuration"/></strong>
        </td>
    </tr>
    <jsp:include page="<%= extraConfig%>"/>
    <%
        }
    %>
</table>
</td>
<td>&nbsp;</td>
<%
    }
%>
<td <%=colspan%> >
    <div id="statsDiv" style="display:<%= service.getActive() ? "'';" : "none;" %>">
    <%
        if(statisticsComponentFound && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor")){
    %>
    <div id="result"></div>
    <script type="text/javascript">
        jQuery.noConflict();
        var refresh;
        function refreshStats() {
            var url = "../statistics/service_stats_ajaxprocessor.jsp?serviceName=<%= serviceName%>";
            try {
            jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                if (status != "success") {
                    stopRefreshStats();
                    document.getElementById('result').innerHTML = responseText;
                }
            });
            } catch (e){} // ignored
        }
        function stopRefreshStats() {
            if (refresh) {
                clearInterval(refresh);
            }
        }
        try {
        jQuery(document).ready(function() {
            refreshStats();
            if (document.getElementById('statsDiv').style.display == ''){
                refresh = setInterval("refreshStats()", 6000);
            }
        });
        } catch (e){} // ignored
    </script>
    <%
        }
    %>
    </div>
</td>
</tr>
</table>

<form name="dataForm" method="post" action="">
    <input name="backURL" type="hidden" id="hiddenField" value="">
</form>

<script type="text/javascript">
    alternateTableRows('serviceInfoTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('serviceClientTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('serviceOperationsTable', 'tableEvenRow', 'tableOddRow');
</script>
</div>
</div>
</fmt:bundle>
