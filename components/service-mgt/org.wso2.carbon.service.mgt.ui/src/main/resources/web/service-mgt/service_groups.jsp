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
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceGroupAdminClient" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaData" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaDataWrapper" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->

<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%><jsp:include page="../dialog/display_messages.jsp"/>

<%
    response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceGroupAdminClient client;

    ServiceGroupMetaData[] mdata;
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
    ServiceGroupMetaDataWrapper serviceGroupInfo;

    String serviceTypeFilter = CharacterEncoder.getSafeText(request.getParameter("serviceTypeFilter"));
    if (serviceTypeFilter == null) {
        serviceTypeFilter = "ALL";
    }
    String serviceGroupSearchString = CharacterEncoder.getSafeText(request.getParameter("serviceGroupSearchString"));
    if (serviceGroupSearchString == null) {
        serviceGroupSearchString = "";
    }
    boolean isAuthorizedToManage = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/modify/service");
    try {
        client = new ServiceGroupAdminClient(cookie, backendServerURL, configContext,
                                             request.getLocale());
        serviceGroupInfo = client.getAllServiceGroups(serviceTypeFilter,
                                                      serviceGroupSearchString,
                                                      Integer.parseInt(pageNumber));
        numberOfPages = serviceGroupInfo.getNumberOfPages();
        mdata = serviceGroupInfo.getServiceGroups();
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    int correctServiceGroups = serviceGroupInfo.getNumberOfCorrectServiceGroups();
    int faultyServiceGroups = serviceGroupInfo.getNumberOfFaultyServiceGroups();
    boolean loggedIn = session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null;
%>

<%
    if (mdata == null || correctServiceGroups == 0) {
%>
        <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
            <h2><fmt:message key="deployed.service.groups.title"/></h2>
            <div id="workArea">
                <b><fmt:message key="no.deployed.services.found"/></b>
            </div>
        </fmt:bundle>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="deployed.services"
        resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<jsp:include page="javascript_include.jsp"/>

<%
    if (session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null) {
%>
<script type="text/javascript">
    var allServiceGroupsSelected = false;

    function deleteServiceGroups() {
        var selected = false;
        if (document.serviceGroupForm.serviceGroups[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.serviceGroupForm.serviceGroups.length; j++) {
                selected = document.serviceGroupForm.serviceGroups[j].checked;
                if (selected) break;
            }
        } else if (document.serviceGroupForm.serviceGroups != null) { // only 1 sg
            selected = document.serviceGroupForm.serviceGroups.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.service.groups.to.be.deleted"/>');
            return;
        }
        if (allServiceGroupsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.service.groups.prompt"><fmt:param value="<%= correctServiceGroups%>"/></fmt:message>", function() {
                location.href = 'delete_service_groups.jsp?deleteAllServiceGroups=true';
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.service.groups.on.page.prompt"/>", function() {
                document.serviceGroupForm.submit();
            });
        }
    }

    function selectAllInThisPage(isSelected) {
        allServiceGroupsSelected = false;
        if (document.serviceGroupForm.serviceGroups != null &&
            document.serviceGroupForm.serviceGroups[0] != null) { // there is more than 1 sg
            if (isSelected) {
                for (var j = 0; j < document.serviceGroupForm.serviceGroups.length; j++) {
                    document.serviceGroupForm.serviceGroups[j].checked = true;
                }
            } else {
                for (j = 0; j < document.serviceGroupForm.serviceGroups.length; j++) {
                    document.serviceGroupForm.serviceGroups[j].checked = false;
                }
            }
        } else if (document.serviceGroupForm.serviceGroups != null) { // only 1 sg
            document.serviceGroupForm.serviceGroups.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allServiceGroupsSelected = true;
        return false;
    }

    function resetVars() {
        allServiceGroupsSelected = false;

        var isSelected = false;
        if (document.serviceGroupForm.serviceGroups[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.serviceGroupForm.serviceGroups.length; j++) {
                if (document.serviceGroupForm.serviceGroups[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.serviceGroupForm.serviceGroups != null) { // only 1 sg
            if (document.serviceGroupForm.serviceGroups.checked) {
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
</script>

<div id="middle">
<h2><fmt:message key="deployed.service.groups.title"/></h2>

<div id="workArea">
<form action="service_groups.jsp" name="searchForm">
    <table class="styledLeft">
        <tr>
            <td style="border:0; !important">
                <nobr>
                    <a href="index.jsp">
                    <%= serviceGroupInfo.getNumberOfActiveServices()%> <fmt:message key="active.services"/></a>.&nbsp;
                    <%= correctServiceGroups%> <fmt:message key="deployed.service.groups"/>
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
                                    for (String serviceType : serviceGroupInfo.getServiceTypes()) {
                                        if (serviceTypeFilter.equals(serviceType)) {
                                %>
                                <option value="<%= serviceType%>" selected="selected"><%= serviceType%>
                                </option>
                                <%
                                } else {
                                %>
                                <option value="<%= serviceType%>"><%= serviceType%>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                            &nbsp;&nbsp;&nbsp;
                            <fmt:message key="search.service.group"/>
                            <input type="text" name="serviceGroupSearchString"
                                   value="<%= serviceGroupSearchString != null? serviceGroupSearchString : ""%>"/>&nbsp;
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
    if (mdata != null && correctServiceGroups > 0) {
        String parameters = "serviceTypeFilter=" + serviceTypeFilter +
                "&serviceGroupSearchString=" + serviceGroupSearchString;
%>

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
                          addRemoveFunction="deleteServiceGroups()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="<%=numberOfPages%>"/>
<% } %>
<p>&nbsp;</p>

<form action="delete_service_groups.jsp" name="serviceGroupForm" method="post">
    <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
    <table class="styledLeft" id="sgTable" width="100%">
        <thead>
        <tr>
            <%
                if (loggedIn) {
            %>
            <th colspan="2"><fmt:message key="service.groups"/></th>
            <%
            } else {
            %>
            <th><fmt:message key="service.groups"/></th>
            <% } %>
            <th colspan="5"><fmt:message key="services"/></th>
        </tr>
        </thead>
        <tbody>

        <%
            int position = 0;
            for (ServiceGroupMetaData serviceGroup : mdata) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
        %>

        <tr bgcolor="<%= bgColor%>">
                    <% if (loggedIn) {%>
            <td width="10px" rowspan="<%= serviceGroup.getServices().length %>" style="text-align:center; !important">
                <%
                    String serviceType = serviceGroup.getServices()[0].getServiceType();
                    if (!serviceType.equals("bpel") && !serviceType.equals("bpelmgt") && !serviceGroup.getDisableDeletion()) {
                %>
                <input type="checkbox" name="serviceGroups"
                       value="<%=serviceGroup.getServiceGroupName()%>"
                       onclick="resetVars()" class="chkBox"/>
                <%
                } else {
                %>
                &nbsp;
                <% } %>
            </td>
                    <% } %>
            <td width="200px" rowspan="<%= serviceGroup.getServices().length %>">
                <nobr>
                    <%
                        if (loggedIn) {
                    %>
                    <a href="./list_service_group.jsp?serviceGroupName=<%= serviceGroup.getServiceGroupName() %>">
                        <%= serviceGroup.getServiceGroupName() %>
                    </a>
                    <% } else { %>
                    <%= serviceGroup.getServiceGroupName() %>
                    <% } %>
                </nobr>
            </td>

                    <%
                    int svcIndex = 0;
                    for (ServiceMetaData service : serviceGroup.getServices()) {
                        String serviceName = service.getName();
                        if (svcIndex != 0) {
                 %>
        <tr>
            <%
                }
                svcIndex++;
            %>
            <td width="200px">
                <%
                    if (loggedIn) {
                %>
                <a href="./service_info.jsp?serviceName=<%=serviceName%>"><%=serviceName%>
                </a>
                <% } else { %>
                <%=serviceName%>
                <% } %>
            </td>
            <td width="20px" style="text-align:center;">
                <img src="../<%= service.getServiceType()%>/images/type.gif"
                     title="<%= service.getServiceType()%>"
                     alt="<%= service.getServiceType()%>"/>
            </td>
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
                <% if (!service.getDisableTryit() && service.getActive()) {%>
                <nobr>
                    <a href="<%=service.getTryitURL()%>" class="icon-link"
                       style="background-image:url(images/tryit.gif);" target="_blank">
                        <fmt:message key="try.this.service"/>
                    </a>
                </nobr>
                <% } %>
            </td>
        </tr>
        <%
                } // for services
            } // for serviceGroups
        %>
        </tbody>
    </table>
</form>
<p>&nbsp;</p>
<%
    if (loggedIn && isAuthorizedToManage) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          addRemoveFunction="deleteServiceGroups()"
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
</div>
</div>
</fmt:bundle>
