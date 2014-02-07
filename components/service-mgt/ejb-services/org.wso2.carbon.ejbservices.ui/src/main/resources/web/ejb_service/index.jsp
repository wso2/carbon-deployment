<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBAppServerData" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBProviderData" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.WrappedAllConfigurations" %>
<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="js/ejbservices.js"></script>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    WrappedAllConfigurations allConfigurations;
    EJBProviderData[] ejbConfigurations;
    EJBAppServerData[] appServers;
    EJBAppServerData[] appServerNameList;
    try {
        EJBServicesAdminClient ejbAdminClient =
                new EJBServicesAdminClient(configContext, backendServerURL, cookie);
        allConfigurations = ejbAdminClient.getAllConfigurations();
        ejbConfigurations = allConfigurations.getEjbProviderData();
        appServers = allConfigurations.getAppServerData();
        appServerNameList = allConfigurations.getAppServerNameList();
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

<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="service.ejb"
                   resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<script type="text/javascript">

    function addApplicationServerElement() {
        var combo = document.getElementById('existingAppServerConfigurations');

        if(!jQuery('#useExistingConfig').is(":checked")) {
            CARBON.showErrorDialog('<fmt:message key="select.application.server.config"/>.');
            return false;
        }

        if (combo.length == 0) {
            CARBON.showErrorDialog('<fmt:message key="add.application.server"/>.');
            return false;
        }

        var obj = document.getElementById('existingAppServerConfigurations');
        var providerUrl = obj[obj.selectedIndex].value;
        //TODO remove providerUrl part since unused?
        location.href = 'ejb_provider_wizard_step1.jsp?providerUrl=' + providerUrl;
    }

    function validateAddEJBApplicationServerSubmit() {
        var serverType = document.getElementById('serverType').value;
        var providerURL = document.getElementById('providerUrl').value;
        var jndiContextClass = document.getElementById('jndiContextClass').value;
        var jndiUserName = document.getElementById('userName').value;
        var password = document.getElementById('password').value;
        var confirmPassword = document.getElementById('confirmPassword').value;

        if (checkForExistingAppServerConfigurations(providerURL)) {
            if (serverType == null || wso2.wsf.Util.trim(serverType) == "") {
                CARBON.showWarningDialog('<fmt:message key="select.application.server.type"/>.');
                return false;
            }

            if (providerURL == null || wso2.wsf.Util.trim(providerURL) == "") {
                CARBON.showWarningDialog('<fmt:message key="enter.valid.provider.url"/>.');
                return false;
            }
            if (jndiContextClass == null || wso2.wsf.Util.trim(jndiContextClass) == "") {
                CARBON.showWarningDialog('<fmt:message key="enter.valid.jndi.context"/>.');
                return false;
            }
            if (password != null && wso2.wsf.Util.trim(password) != "") {
                if (jndiUserName == null || wso2.wsf.Util.trim(jndiUserName) == "") {
                    CARBON.showWarningDialog('<fmt:message key="enter.username"/>.');
                    return false;
                }
                if (password != confirmPassword) {
                    CARBON.showWarningDialog('<fmt:message key="re.entered.password"/>.');
                    return false;
                }
            }
            document.addEJBApplicationServerForm.submit();
        }
        return false;
    }

    function editServiceParameters(serviceName) {
        location.href = "edit_ejb_configuration.jsp?serviceName=" + serviceName;
    }

    function deleteServiceParameters(serviceName) {
        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.ejb.configuration"/>',
                function(){
                    location.href = "delete_ejb_configuration.jsp?serviceName=" + serviceName;
                }, function(){
                    return false;
                },
                null);
    }

    function toggleAddAppServerWindow(){
        if(jQuery('#addNewConfig').is(":checked")) {
            jQuery('#addNewConfigTable').show();
        } else {
            jQuery('#addNewConfigTable').hide();
        }
    }

    function testApplicationServerConnection(){
        var providerURL1 = jQuery('#providerUrl').val();
        var jndiContextClass = jQuery('#jndiContextClass').val();
        var userName = jQuery('#userName').val();
        var password = jQuery('#password').val();

        if (providerURL1 == null || wso2.wsf.Util.trim(providerURL1) == "") {
            CARBON.showWarningDialog('<fmt:message key="enter.valid.provider.url"/>.');
            return false;
        }
        if (jndiContextClass == null || wso2.wsf.Util.trim(jndiContextClass) == "") {
            CARBON.showWarningDialog('<fmt:message key="enter.valid.jndi.context"/>.');
            return false;
        }
        if (password != null && wso2.wsf.Util.trim(password) != "") {
            if (jndiUserName == null || wso2.wsf.Util.trim(jndiUserName) == "") {
                CARBON.showWarningDialog('<fmt:message key="enter.username"/>.');
                return false;
            }
        }

        jQuery.ajax({
                        type:"POST",
                        url:"../ejb_service/test_appserver_configuration-ajaxprocessor.jsp",
                        data:{
                            providerURL1:providerURL1, jndiContextClass:jndiContextClass,
                            userName:userName, password:password
                        },
                        success:function (status) {
                            CARBON.showInfoDialog(
                                    '<fmt:message key="application.server.connection.test.successful"/>.',
                                    null, null);
//                            if (status.status == '200') {
//                            }
                        },
                        error:function (status) {
                            if (status.status == '500') {
                                CARBON.showErrorDialog(
                                        '<fmt:message key="error.connectiong.to.application.server"/>.',
                                        null, null);
                            }
                        }});
    }
</script>
<div id="middle">

    <h2><fmt:message key="service.ejb"/></h2>

    <div id="workArea">

        <div class="sectionSeperator togglebleTitle"><fmt:message
                key="existing.configurations"/></div>
        <div class=”sectionSub”>
            <table class="styledLeft" id="existingEJBConfigurationsTable">
                <thead>
                <tr>
                    <th><fmt:message key="service.name"/></th>
                    <th><fmt:message key="provider.url"/></th>
                    <th><fmt:message key="bean.jndi.name"/></th>
                    <th><fmt:message key="edit"/></th>
                    <th><fmt:message key="delete"/></th>
                </tr>
                </thead>
                <%
                    if (ejbConfigurations == null) {
                %>
                <tr>
                    <td colspan="5">
                        <label style="width: 200px; color: brown;"><fmt:message
                                key="no.existing.ejb.configurations"/></label>
                    </td>
                </tr>
                <%
                } else {
                    for (EJBProviderData ejbProviderData : ejbConfigurations) {
                %>
                <tr>
                    <td><%=ejbProviderData.getServiceName()%>
                    </td>
                    <td><%=ejbProviderData.getProviderURL()%>
                    </td>
                    <td><%=ejbProviderData.getBeanJNDIName()%>
                    </td>
                    <td>
                            <%--&nbsp;&nbsp;--%>
                        <a title="Edit EJB Configuration"
                           onclick="editServiceParameters('<%=ejbProviderData.getServiceName()%>'); return false;"
                           style="background-image:url(../admin/images/edit.gif);"
                           href="#" class="icon-link">
                                <%--&nbsp;&nbsp;--%>
                        </a>
                    </td>
                    <td>
                        <a title="Delete EJB Configuration"
                           onclick="deleteServiceParameters('<%=ejbProviderData.getServiceName()%>'); return false;"
                           style="background-image:url(../admin/images/delete.gif);"
                           href="#" class="icon-link">
                            &nbsp;&nbsp;
                        </a>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </table>
        </div>
        <p>&nbsp;</p>

        <div class="sectionSeperator" id="addEJBApplicationServer"><fmt:message
                key="create.new.ejb.service"/></div>
        <div class=”sectionSub”>
            <form name="addEJBApplicationServerForm" method="post" action="ejbServiceProvider.jsp">
                <table class="carbonFormTable sectionSub">
                    <tr>
                        <th colspan="2"><fmt:message key="select.application.server"/></th>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <div class="sectionHelp">
                                Application Server Configuration details where the actual EJB is
                                deployed
                                are needed for configuring a new EJB service.
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="labelField">
                            <input onclick="toggleAddAppServerWindow();" type="radio" checked="true"
                                   value="existingconfig" name="astype"
                                   id="useExistingConfig">
                            <label for="useExistingConfig"><fmt:message key="use.existing"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td class="labelField">
                            <table class="carbonFormTable">
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key="server.configuration"/></td>
                                    <td>
                                        <select name="existingAppServerConfigurations" id="existingAppServerConfigurations">
                                            <%
                                                if (appServers != null) {
                                                    for (EJBAppServerData appServer : appServers) {
                                            %>
                                            <option value="<%=appServer.getProviderURL()%>"><%=appServer.getAppServerType()%> - <%=appServer.getProviderURL()%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="labelField">
                            <input onclick="toggleAddAppServerWindow();" type="radio"
                                   value="newconfig" name="astype" id="addNewConfig">
                            <label for="addNewConfig"><fmt:message key="add.new"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td class="labelField">
                            <table class="carbonFormTable" id="addNewConfigTable">
                                <tr>
                                    <td>
                                        <label><fmt:message key="server.type"/><span class="required">*</span></label>
                                    </td>
                                    <td>
                                        <select onchange="setDefaultServerValues(this,document);return false;"
                                                name="serverType" id="serverType">
                                            <%
                                                if (appServerNameList != null) {
                                                    for (EJBAppServerData appServer : appServerNameList) {
                                            %>
                                            <option value="<%=appServer.getServerId()%>"><%=appServer.getServerName()%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                            <option value="" selected="true">--<fmt:message
                                                    key="application.server"/>--
                                            </option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="providerUrl"><fmt:message key="provider.url"/>
                                            <span class="required">*</span></label>
                                    </td>
                                    <td>
                                        <input maxlength="100" size="60" tabindex="2" id="providerUrl"
                                               name="providerUrl" type="text">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="jndiContextClass"><fmt:message key="jndi.context.class"/>
                                            <span class="required">*</span></label>
                                    </td>
                                    <td>
                                        <input maxlength="100" size="60" tabindex="3" id="jndiContextClass"
                                               name="jndiContextClass" type="text">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="userName"><fmt:message key="user.name"/></label>
                                    </td>
                                    <td>
                                        <input maxlength="20" size="40" tabindex="4" id="userName"
                                               name="userName" type="text">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="password"><fmt:message key="password"/></label>
                                    </td>
                                    <td>
                                        <input maxlength="20" size="40" tabindex="5"
                                               id="password" name="password" type="password">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="password"><fmt:message key="confirm.password"/></label>
                                    </td>
                                    <td>
                                        <input maxlength="20" size="40" tabindex="5"
                                               id="confirmPassword" name="confirmPassword" type="password">
                                    </td>
                                </tr>
                                <tr>
                                    <td class="sub_buttonRow" colspan="2">
                                        <input value="<fmt:message key="add.new.application.server"/>"
                                               name="addApplicationServerButton"
                                               id="addApplicationServerButton"
                                               type="button" class="button"
                                               onclick="validateAddEJBApplicationServerSubmit()"/>
                                        <input value="<fmt:message key="test.connection"/>"
                                               name="testApplicationServerConnectionButton"
                                               id="testApplicationServerConnectionButton"
                                               type="button" class="button"
                                               onclick="testApplicationServerConnection();"/>
                                        <input value="<fmt:message key="reset"/>"
                                               name="resetAddApplicationServerButton"
                                               id="resetAddApplicationServerButton"
                                               type="button" class="button"
                                               onclick="location.href='';"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <div class="buttonRow">
            <input type="button" value="<fmt:message key="next"/>&gt;"
                   onclick="addApplicationServerElement();" id="ejbStep0NextButton">
        </div>
    </div>
</div>

<script type="text/javascript">
//    alternateTableRows('existingEJBConfigurationsTable', 'tableEvenRow', 'tableOddRow');
</script>

<script type="text/javascript">
    initSections("hidden");
    toggleAddAppServerWindow();
</script>

</fmt:bundle>
