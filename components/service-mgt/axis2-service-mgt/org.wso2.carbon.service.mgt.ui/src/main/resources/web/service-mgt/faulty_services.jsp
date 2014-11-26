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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.FaultyServicesWrapper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%

    String pageNumber = CharacterEncoder.getSafeText(request.getParameter("pageNumber"));
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceAdminClient client;
    FaultyServicesWrapper faultyServicesWrapper;
    try {
        client = new ServiceAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        faultyServicesWrapper = client.getAllFaultyServices(pageNumberInt);
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

<div id="middle">
    <div id="workArea">
        <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
        <carbon:breadcrumb
                label="faulty.service.groups"
                resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                topPage="false"
                request="<%=request%>"/>
        <h2><fmt:message key="faulty.services"/></h2>
        </fmt:bundle>
        <%
            FaultyService[] faultyServices;
            int numberOfPages;
            if (faultyServicesWrapper == null) {
        %>
                <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
                <p><fmt:message key="no.faulty.service.groups.found"/></p>
                </fmt:bundle>
        <%
                return;
            }

            faultyServices = faultyServicesWrapper.getFaultyServices();
            numberOfPages = faultyServicesWrapper.getNumberOfPages();
        %>

        <% if (faultyServices.length == 0) { %>
                <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
                <p><fmt:message key="no.faulty.service.groups.found"/></p>
                </fmt:bundle>    
        <%
                return;
            }
        %>
        <fmt:bundle basename="org.wso2.carbon.service.mgt.ui.i18n.Resources">
        <script type="text/javascript">
            var allServiceGroupsSelected = false;
            function showError(divId) {
                if (document.getElementById(divId).style.visibility == 'visible') {
                    document.getElementById(divId).style.visibility = 'hidden';
                } else {
                    document.getElementById(divId).style.visibility = 'visible';
                }
            }

            function deleteServiceGroups() {
                var selected = false;
                if (document.faultyServiceForm.serviceGroups[0] != null) { // there is more than 1 sg
                    for (var j = 0; j < document.faultyServiceForm.serviceGroups.length; j++) {
                        selected = document.faultyServiceForm.serviceGroups[j].checked;
                        if (selected) break;
                    }
                } else if (document.faultyServiceForm.serviceGroups != null) { // only 1 sg
                    selected = document.faultyServiceForm.serviceGroups.checked;
                }
                if (!selected) {
                    CARBON.showInfoDialog('<fmt:message key="select.service.groups.to.be.deleted"/>');
                    return;
                }
                if (allServiceGroupsSelected) {
                    CARBON.showConfirmationDialog("<fmt:message key="delete.selected.faulty.service.groups.prompt"><fmt:param value="<%= faultyServicesWrapper.getNumberOfFaultyServiceGroups() %>"/></fmt:message>",
                            function() {
                                location.href = 'delete_faulty_service_groups.jsp?deleteAllServiceGroups=true';
                    });
                } else {
                    CARBON.showConfirmationDialog("<fmt:message key="delete.all.faulty.service.groups.prompt"/>", function() {
                        document.faultyServiceForm.submit();
                    });
                }
            }

            function selectAllInThisPage(isSelected) {
                allServiceGroupsSelected = false;
                if (document.faultyServiceForm.serviceGroups[0] != null) { // there is more than 1 sg
                    if (isSelected) {
                        for (var j = 0; j < document.faultyServiceForm.serviceGroups.length; j++) {
                            document.faultyServiceForm.serviceGroups[j].checked = true;
                        }
                    } else {
                        for (j = 0; j < document.faultyServiceForm.serviceGroups.length; j++) {
                            document.faultyServiceForm.serviceGroups[j].checked = false;
                        }
                    }
                } else if (document.faultyServiceForm.serviceGroups != null) { // only 1 sg
                    document.faultyServiceForm.serviceGroups.checked = isSelected;
                }
            }

            function selectAllInAllPages() {
                selectAllInThisPage(true);
                allServiceGroupsSelected = true;
            }

            function resetVars() {
                allServiceGroupsSelected = false;

                var isSelected = false;
                if (document.faultyServiceForm.serviceGroups[0] != null) { // there is more than 1 sg
                    for (var j = 0; j < document.faultyServiceForm.serviceGroups.length; j++) {
                        if (document.faultyServiceForm.serviceGroups[j].checked) {
                            isSelected = true;
                        }
                    }
                } else if (document.faultyServiceForm.serviceGroups != null) { // only 1 sg
                    if (document.faultyServiceForm.serviceGroups.checked) {
                        isSelected = true;
                    }
                }
            }
        </script>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="deleteServiceGroups()"
                                  addRemoveButtonId="delete1"
                                  addRemoveKey="delete"/>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>
        <p>&nbsp;</p>

        <form action="delete_faulty_service_groups.jsp" name="faultyServiceForm">
            <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th width="300px"><fmt:message key="faulty.service"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <tbody>
                <%

                    int count = 0;
                    for (FaultyService service : faultyServices) {
                        if (service != null) {
                            count++;
                            String faultyServiceName = service.getServiceName();
                %>
                <tr>
                    <td rowspan="2">
                        <input type="checkbox" name="serviceGroups"
                               value="<%=service.getServiceName()%>"
                               onclick="resetVars()" class="chkBox"/>
                    </td>
                    <td width="300px">
                        <%=faultyServiceName%>
                        <img src="../<%= service.getServiceType()%>/images/type.gif"
                             title="<%= service.getServiceType()%>"
                             alt="<%= service.getServiceType()%>"/>
                        <br/>
                        <% if (service.getArtifact() != null) { %>
                        (<%= service.getArtifact() %>)
                        <% } %>
                    </td>
                    <td>
                        <nobr>
                            <%
                                String serviceType = service.getServiceType();
                                String serviceTypePath = "/" + serviceType + "/";
                                String faultyServiceEdit = ".." + serviceTypePath + "edit_faulty_service.jsp";
                                if (config.getServletContext().getResourcePaths(serviceTypePath).contains(serviceTypePath+"edit_faulty_service.jsp")) {
                            %>
                            <a href="<%= faultyServiceEdit + "?serviceName="+faultyServiceName%>"
                               id="edit_link" title="<fmt:message key="edit.faulty.service"/>">
                                <fmt:message key="edit.faulty.service"/>
                            </a>
                            <%
                                }
                            %>
                        </nobr>

                    </td>
                </tr>
                <tr id="errorMsg<%=count %>">
                    <td>&nbsp;</td>
                    <td colspan="2">
                        <%=service.getFault()%>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </form>
        <p>&nbsp;</p>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="faulty_services.jsp" pageNumberParameterName="pageNumber"/>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="deleteServiceGroups()"
                                  addRemoveButtonId="delete2"
                                  addRemoveKey="delete"/>
        </fmt:bundle>
    </div>
</div>
