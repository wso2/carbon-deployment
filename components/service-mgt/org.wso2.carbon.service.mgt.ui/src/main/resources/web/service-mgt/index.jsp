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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceTypeNaming" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->

<jsp:include page="../dialog/display_messages.jsp"/>
<!--[if IE 7]>
<style>
.paginator-ie7-fix table{
	width:380px;;
}
.paginator-ie7-fix table td{
padding-right:10px;
}
.paginator-ie7-fix td b{
display:inline-block;
padding:0 10px;
}
</style>
<![endif]-->
<%
    response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceAdminClient client;

    ServiceMetaData[] serviceData;
    int numberOfPages;
    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }
    ServiceMetaDataWrapper servicesInfo;
    ServiceTypeNaming stn = new ServiceTypeNaming();

    String serviceTypeFilter = CharacterEncoder.getSafeText(request.getParameter("serviceTypeFilter"));
    if (serviceTypeFilter == null) {
        serviceTypeFilter = "ALL";
    }
    String serviceSearchString = CharacterEncoder.getSafeText(request.getParameter("serviceSearchString"));
    if (serviceSearchString == null) {
        serviceSearchString = "";
    }
    boolean isAuthorizedToManage =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/modify/service");
    boolean hasProxy = false;
    try {
        client = new ServiceAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        servicesInfo = client.getAllServices(serviceTypeFilter,
                                                 serviceSearchString,
                                                 Integer.parseInt(pageNumber));
        numberOfPages = servicesInfo.getNumberOfPages();
        serviceData = servicesInfo.getServices();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    int correctServiceGroups = servicesInfo.getNumberOfCorrectServiceGroups();
    int faultyServiceGroups = servicesInfo.getNumberOfFaultyServiceGroups();
    boolean loggedIn = session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null;
    boolean hasDownloadableServices = false;

    if (serviceData != null && serviceData.length > 0) {
        for (ServiceMetaData service : serviceData) {
            if (service != null && service.getServiceType() != null) {
                if ((service.getServiceType().equalsIgnoreCase("axis2") && !(service.getName().equalsIgnoreCase("xkms"))) ||
                    service.getServiceType().equalsIgnoreCase("jaxws") || service.getServiceType().equalsIgnoreCase("spring") ||
                    service.getServiceType().equalsIgnoreCase("js_service") || service.getServiceType().equalsIgnoreCase("jarservice")) {
                    hasDownloadableServices = true;
                    break;
                }
            }
        }
    }
%>

<carbon:breadcrumb
        label="deployed.services"
        resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<jsp:include page="javascript_include.jsp"/>

<%
    if ((serviceData == null || correctServiceGroups == 0) && faultyServiceGroups == 0) {
%>
        <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
            <div id="middle">
                <h2><fmt:message key="deployed.services"/></h2>
                <div id="workArea">
                    <fmt:message key="no.deployed.services.found"/>
                </div>
            </div>
        </fmt:bundle>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
<%
    if (session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null) {
%>
<script type="text/javascript">
    var allServicesSelected = false;

    function deleteServices() {
        var selected = false;
        if (document.servicesForm.serviceGroups[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                selected = document.servicesForm.serviceGroups[j].checked;
                if (selected) break;
            }
        } else if (document.servicesForm.serviceGroups != null) { // only 1 service
            selected = document.servicesForm.serviceGroups.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.services.to.be.deleted"/>');
            return;
        }
        if (allServicesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.services.prompt"><fmt:param value="<%= servicesInfo.getNumberOfActiveServices()%>"/></fmt:message>", function() {
                location.href = 'delete_service_groups.jsp?deleteAllServiceGroups=true';
            });
        } else {

            var serviceGroupsString = '';
            jQuery('.chkBox').each(function(index) {
                if(this.checked) {
                    serviceGroupsString += this.value + ':';
                }
            });

            jQuery.ajax(
                    {
                        url : "checkForGroupedServices_ajaxprocessor.jsp?serviceGroupsString=" + serviceGroupsString,
                        success : function (data) {
                            if(data.search('foundgroupedservice') > 0){
                                CARBON.showConfirmationDialog("<fmt:message key="delete.service.groups.with.multiples.services.prompt"/>",
                                                              function(){
                                                                  document.servicesForm.submit();
                                                              },
                                                              function(){
                                                                  location.href='';
                                                              });
                            } else {
                                CARBON.showConfirmationDialog("<fmt:message key="delete.services.on.page.prompt"/>", function() {
                                    document.servicesForm.submit();
                                });
                            }
                        }
                    }
            );
        }
    }

    function selectAllInThisPage(isSelected) {
        allServicesSelected = false;
        if (document.servicesForm.serviceGroups != null &&
            document.servicesForm.serviceGroups[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                    document.servicesForm.serviceGroups[j].checked = true;
                }
            } else {
                for (j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                    document.servicesForm.serviceGroups[j].checked = false;
                }
            }
        } else if (document.servicesForm.serviceGroups != null) { // only 1 service
            document.servicesForm.serviceGroups.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allServicesSelected = true;
        return false;
    }

    function resetVars() {
        allServicesSelected = false;

        var isSelected = false;
        if (document.servicesForm.serviceGroups[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                if (document.servicesForm.serviceGroups[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.servicesForm.serviceGroups != null) { // only 1 service
            if (document.servicesForm.serviceGroups.checked) {
                isSelected = true;
            }
        }
        return false;
    }
</script>
<%
    }
%>

<script type="text/javascript">
    function searchServices() {
        document.searchForm.submit();
    }
    function editPS(serviceName) {
        window.location.href='../proxyservices/index.jsp?header=Modify' + '&serviceName='+serviceName+'&startwiz=true';
    }
    function editProxySourceView(serviceName) {
        window.location.href='../proxyservices/index.jsp?header=Modify' + '&serviceName='+serviceName+'&startwiz=false&sourceView=true';
    }
</script>

<div id="middle">
<h2><fmt:message key="deployed.services"/></h2>

<div id="workArea">
<form action="index.jsp" name="searchForm">
    <table class="styledLeft">
        <% if(loggedIn) { %>
        <tr>
            <td style="border:0; !important">
                <nobr>
                    <%= servicesInfo.getNumberOfActiveServices()%> <fmt:message key="active.services"/>.&nbsp;
                    <a href="service_groups.jsp">
                    <%= correctServiceGroups%> <fmt:message key="deployed.service.groups"/>
                    </a>
                    <%
                        if (session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null) {
                            if (faultyServiceGroups > 0) {
                    %>
                    <u>
                        <a href="faulty_services.jsp">
                            <font color="red"><%= faultyServiceGroups%>
                                <fmt:message key="faulty.services"/></font>
                        </a>
                    </u>
                    <%
                            }
                        }
                    %>
                </nobr>
            </td>
        </tr>
        <% } %>
        <tr>
            <td style="border:0; !important">&nbsp;</td>
        </tr>
        <tr>  
            <td>
                <table style="border:0; !important">
                <tbody>
                <tr style="border:0; !important">
                    <td style="border:0; !important">
                        <nobr>
                            <fmt:message key="service.type"/>
                            <select name="serviceTypeFilter">
                                <%
                                    if (serviceTypeFilter.equals("ALL")) {
                                %>
                                <option value="ALL" selected="selected"><fmt:message key="all"/></option>
                                <%
                                } else {
                                %>
                                <option value="ALL"><fmt:message key="all"/></option>
                                <%
                                    }
                                    for (String serviceType : servicesInfo.getServiceTypes()) {
                                        if (serviceTypeFilter.equals(serviceType)) {
                                %>
                                <option value="<%= serviceType%>" selected="selected"><%= stn.convertString(serviceType) %>
                                </option>
                                <%
                                } else {
                                %>                           
                                <option value="<%= serviceType%>"><%= stn.convertString(serviceType) %>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                            &nbsp;&nbsp;&nbsp;
                            <fmt:message key="search.service"/>
                            <input type="text" name="serviceSearchString"
                                   value="<%= serviceSearchString != null? serviceSearchString : ""%>"/>&nbsp;
                        </nobr>
                    </td>
                    <td style="border:0; !important">
                         <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                               onclick="javascript:searchServices(); return false;"
                               alt="<fmt:message key="search"/>"></a>
                    </td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
    </table>
</form>

<p>&nbsp;</p>
<%
    if (serviceData != null && correctServiceGroups > 0) {
        String parameters = "serviceTypeFilter=" + serviceTypeFilter +
                "&serviceSearchString=" + serviceSearchString;
%>
<div class="paginator-ie7-fix">
<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>
<%
	if (loggedIn && isAuthorizedToManage) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          addRemoveFunction="deleteServices()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"/>
<% } %>
</div>	<!-- paginator-ie7-fix -->
<p>&nbsp;</p>

<form action="delete_service_groups.jsp" name="servicesForm" method="post">
    <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
    <table class="styledLeft" id="sgTable" width="100%">
        <thead>
        <tr>
            <%
                if (loggedIn && hasDownloadableServices) {
            %>
            <th colspan="10"><fmt:message key="services"/></th>
            <%
            } else if (loggedIn) {
            %>
            <th colspan="9"><fmt:message key="services"/></th>
            <%
            } else {
            %>
            <th colspan="8"><fmt:message key="services"/></th>
            <% } %>
        </tr>
        </thead>
        <tbody>

        <%
            int position = 0;
            for (ServiceMetaData service : serviceData) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
                if (service == null) {
                    continue;
                }
        %>

        <tr bgcolor="<%= bgColor%>">
                    <% if (loggedIn) {%>
            <td width="10px" style="text-align:center; !important">
                <%
                    String serviceType = service.getServiceType();
                    if (!serviceType.equals("bpel") && !serviceType.equals("bpelmgt") && !service.getDisableDeletion()) {
                %>
                <input type="checkbox" name="serviceGroups"
                       value="<%=service.getServiceGroupName()%>"
                       onclick="resetVars()" class="chkBox"/>
                <%
                } else {
                %>
                &nbsp;
                <% } %>
            </td>
                    <% } %>
            <td width="200px">
                <nobr>
                    <%
                        String serviceName = service.getName();
                        if (loggedIn) {
                    %>
                    <a href="./service_info.jsp?serviceName=<%=serviceName%>"><%=serviceName%>
                    </a>
                    <% } else { %>
                    <%=serviceName%>
                    <% } %>
                </nobr>
            </td>
            <td width="20px" style="text-align:left;">
                <nobr>
                <img src="../<%= service.getServiceType()%>/images/type.gif"
                     title="<%= service.getServiceType()%>"
                     alt="<%= service.getServiceType()%>"/>
                <%= service.getServiceType() %>
                </nobr>
            </td>
            <% if(isAuthorizedToManage) { %>
            <td style="text-align:left;" width="10px">
                <nobr>
                <%= service.getSecurityScenarioId() != null ?
                "<a href='../securityconfig/index.jsp?serviceName=" + serviceName + "'  class='icon-link' style='background-image:url(images/secured.gif);' " +
                "title='Secured using "+ service.getSecurityScenarioId() +"'>Secured</a>":
                "<a href='../securityconfig/index.jsp?serviceName=" + serviceName +  "'  class='icon-link' style='background-image:url(images/unsecured.gif);' " +
                "title='Unsecured'>Unsecured</a>"
                %>
                 </nobr>
            </td>
            <% } %>
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
            <td width="100px">
				<div style="text-align:center">
                <% if (!service.getDisableTryit() && service.getActive()) {%>
                <nobr>
                    <a href="<%=service.getTryitURL()%>" class="icon-link"
                       style="background-image:url(images/tryit.gif);" target="_blank">
                        <fmt:message key="try.this.service"/>
                    </a>
                </nobr>
                <% } %>
				</div>
            </td>
            <% if (loggedIn && hasDownloadableServices) { %>
            <td width="100px">
                <% if ((service.getServiceType().equalsIgnoreCase("axis2") && !(service.getName().equalsIgnoreCase("xkms"))) ||
                        service.getServiceType().equalsIgnoreCase("jaxws") ||service.getServiceType().equalsIgnoreCase("spring") ||
                        service.getServiceType().equalsIgnoreCase("js_service") || service.getServiceType().equalsIgnoreCase("jarservice")) { %>
                <nobr>
                    <a href="download-ajaxprocessor.jsp?serviceName=<%=service.getServiceGroupName()%>&serviceType=<%=service.getServiceType()%>"
                       class="icon-link" style="background-image:url(images/download.gif);"
                       target="_self">
                        <fmt:message key="download"/>
                    </a>
                </nobr>        
                <% } else { %>
                        &nbsp;
                <% } %>
            </td>
            <% } %>
            <% if (service.getServiceType().equalsIgnoreCase("proxy")) { %>
            <% hasProxy = true; %>
            <td>
                <a title="Edit '<%=service.getName()%>' in the design view" href="#" onclick="editPS('<%=service.getName()%>');return false;">
                    <img src="../proxyservices/images/design-view.gif" alt="" border="0"> Design View</a>
            </td>
            <td>
                <a title="Edit '<%=service.getName()%>' in the source view editor" 
                    style="background-image: url(../proxyservices/images/source-view.gif);" 
                    class="icon-link" onclick="editProxySourceView('<%=service.getName()%>')" href="#">Source View</a>
            </td>
            <% } else {%>
            <td colspan="2"></td>
            <% } %>
        </tr>
        <%
            } // for services
        %>
        </tbody>
    </table>
    <script>
    if (<%=hasProxy%> == false) {
        jQuery('#sgTable tr th').attr('colspan', parseInt(jQuery('#sgTable tr th').attr('colspan')) - 2);
        $("#sgTable tr").each(function(){
            $(this).find("td:last").remove();
        });
    }
    </script>
</form>
<p>&nbsp;</p>
<div class="paginator-ie7-fix">
<%
    if (loggedIn && isAuthorizedToManage) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          addRemoveFunction="deleteServices()"
                          addRemoveButtonId="delete2"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"/>
<% } %>
<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%= parameters%>"/>
<%
} else {
%>
<b><fmt:message key="no.deployed.services.found"/></b>
<%
    }
%>
</div><!-- paginator-ie7-fix end -->

</div>
</div>
</fmt:bundle>   
