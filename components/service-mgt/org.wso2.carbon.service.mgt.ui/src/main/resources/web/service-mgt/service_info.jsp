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
            <tr>
                <td><fmt:message key="service.deployed.time"/></td>
                <td><%=service.getServiceDeployedTime()%>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="service.up.time"/></td>
                <td><%=service.getServiceUpTime()%>
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
<%
    String colspan = "";
    if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/modify/service")){
        colspan = " colspan=\"3\" ";
%>
<td>
<table class="styledLeft" id="serviceOperationsTable" style="margin-left: 0px;" width="100%">
<thead>
    <tr>
        <th colspan="2" align="left"><fmt:message key="quality.of.service.configuration"/></th>
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

<tr>
    <td>
        <%
            if(CarbonUIUtil.isContextRegistered(config, "/securityconfig/")){
        %>
        <nobr>
        <a href="../securityconfig/index.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/security.gif);">
            <fmt:message key="security"/>&nbsp;&nbsp;&nbsp;
        <%= service.getSecurityScenarioId() != null ?
                "<img src='images/secured.gif' title='Secured using "+  service.getSecurityScenarioId()  +"'/>":
                "<img src='images/unsecured.gif' title='Unsecured'/>"%>
         </a>
        <%
            }
        %>
        </nobr>
    </td>
    <td>
        <a href="policy_editor_proxy.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/policies.gif);">
            <fmt:message key="policies"/>
        </a>
    </td>
</tr>
<tr>
    <td>
        <%
            if(CarbonUIUtil.isContextRegistered(config, "/rm/")){
        %>
        <a href="" onclick="submitHiddenForm('../rm/index.jsp?serviceName=<%=serviceName%>');return false;"
           class="icon-link-nofloat"
           style="background-image:url(images/rm.gif);">
            <fmt:message key="reliable.messaging"/>
        </a>
         <%
            }
        %>
    </td>
    <td>
         <%
            if(CarbonUIUtil.isContextRegistered(config, "/transport-mgt/")){
        %>
        <a href="../transport-mgt/service_transport.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/transports.gif);">
            <fmt:message key="transports"/>
        </a>
         <%
            }
        %>
    </td>

</tr>
<tr>
    <td>
        <%
            if(CarbonUIUtil.isContextRegistered(config, "/caching/")){
        %>
        <a href="" onclick="submitHiddenForm('../caching/index.jsp?serviceName=<%=serviceName%>');return false;"
           class="icon-link-nofloat"
           style="background-image:url(images/caching.gif);">
            <fmt:message key="response.caching"/>
        </a>
        <%
            }
        %>
    </td>
    <td>
         <%
            if(CarbonUIUtil.isContextRegistered(config, "/modulemgt/")){
        %>
        <a href="../modulemgt/service_modules.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/modules.gif);">
            <fmt:message key="modules"/>
        </a>
        <%
            }
        %>
    </td>
</tr>
<tr>
    <td>
         <%
            if(CarbonUIUtil.isContextRegistered(config, "/throttling/")){
        %>
        <a href="" onclick="submitHiddenForm('../throttling/index.jsp?serviceName=<%=serviceName%>');return false;"
           class="icon-link-nofloat"
           style="background-image:url(images/throttling.gif);">
            <fmt:message key="access.throttling"/>
        </a>
        <%
            }
        %>
    </td>
    <td>
         <%
            if(CarbonUIUtil.isContextRegistered(config, "/operation/")){
        %>
        <a href="../operation/index.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/operations.gif);">
            <fmt:message key="operations"/>
        </a>
        <%
            }
        %>
    </td>
</tr>
<tr>
    <td>
        <span class="icon-text" style="background-image:url(images/service.gif);">MTOM&nbsp;&nbsp;</span>
        <select id="mtomSelector" onchange="changeMtomState()" style="margin-top:2px !important;">
            <%
                if (service.getMtomStatus().equalsIgnoreCase("true")) {
            %>
            <option value="true" selected="true">True</option>
            <%
            } else {
            %>
            <option value="true"><fmt:message key="true"/></option>
            <% } %>

            <%
                if (service.getMtomStatus().equalsIgnoreCase("false")) {
            %>
            <option value="false" selected="true"><fmt:message key="false"/></option>
            <%
            } else {
            %>
            <option value="false"><fmt:message key="false"/></option>
            <% } %>

            <%
                if (service.getMtomStatus().equalsIgnoreCase("optional")) {
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
            function changeMtomState() {
                var state = document.getElementById('mtomSelector').value;
                var url = 'mtom_ajaxprocessor.jsp?serviceName=<%= request.getAttribute("serviceName")%>&mtomState=' + state;
                jQuery("#mtomOutput").load(url, null, function (responseText, status, XMLHttpRequest) {
                    if (status != "success") {
                        CARBON.showErrorDialog('<fmt:message key="could.not.change.mtom.state.for.service"/>');
                    } else {
                        CARBON.showInfoDialog('<fmt:message key="changed.mtom.state.for.service"/>');
                    }
                });
            }
        </script>
    </td>
    <td>
        <a href="edit_service_params.jsp?serviceName=<%=serviceName%>"
           class="icon-link-nofloat"
           style="background-image:url(images/edit2.gif);">
            <fmt:message key="parameters"/>
        </a>
    </td>
</tr>

                        <tr>
<%
     // Add UI Extensions that may have  been provided by other UI components
     BundleContext bundleContext =  CarbonUIUtil.getBundleContext();
     if (bundleContext != null) {
         ServiceTracker tracker = new ServiceTracker(bundleContext,
                                                     ServiceManagementUIExtender.class.getName(),
                                                     null);
         tracker.open();
         ServiceManagementUIExtender extender = (ServiceManagementUIExtender) tracker.getService();

         if(extender != null){
             List<String> items = extender.getItems();
             for (String item: items) {
%>
    `           <tr>
                  <td colspan="2" align="left"><%= item%></td>
                </tr>
<%
             }
             tracker.close();
         }
     }

    String serviceType = service.getServiceType();
    String serviceTypePath = "/" + serviceType + "/";
    String extraConfig = ".." + serviceTypePath + "extra_config.jsp?serviceName="+serviceName;
    Set resourcePaths = config.getServletContext().getResourcePaths(serviceTypePath);
    if (resourcePaths != null && resourcePaths.contains(serviceTypePath + "extra_config.jsp")) { //TODO: How to handle this
%>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td colspan="2" align="left"><strong><fmt:message key="specific.configuration"/></strong></td>
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
