<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.WrappedAllConfigurations" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBProviderData" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBAppServerData" %>
<%@ page import="java.util.Random" %>

<script type="text/javascript" src="js/ejbservices.js"></script>

<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="select.ejb.remote.interface"
		resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <%
        String archiveId = request.getParameter("archiveId");
        String jnpProviderUrl = (String) session.getAttribute("providerUrl");
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EJBServicesAdminClient ejbAdminClient;

        Random random = new Random();
        String[] classNames;
        try {
            ejbAdminClient = new EJBServicesAdminClient(configContext, backendServerURL, cookie);
            classNames = ejbAdminClient.getClassNames(request.getParameter("archiveId"));

        } catch (Exception e) {
    %>
    <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
    <%
            return;
        }
    %>

    <script type="text/javascript">

        function validateEJBParams() {
            var remoteInterfaceClass = '';
            var beanJNDIName = document.getElementById('beanJNDIName').value;

            var remoteInterfaceName = document.getElementById("remoteInterface").value;
            /*var remoteInterfaces = document.getElementsByName("chkRemoteInterface");
            for (var a = 0; a < remoteInterfaces.length; a++) {
                if (remoteInterfaces[a].checked) {
                    remoteInterfaceClass = remoteInterfaces[a].value;
                }
            }*/

            if (remoteInterfaceName == "null") {
                CARBON.showErrorDialog('<fmt:message key="please.enter.remote.interface.class"/>.');
                return false;
            }
            if (beanJNDIName == null || wso2.wsf.Util.trim(beanJNDIName) == "") {
                CARBON.showErrorDialog('<fmt:message key="please.enter.jndi.name.of.ejb"/>.');
                return false;
            }

            document.addEJBDeployServerForm.submit();
        }
        function cancel() {
            location.href = 'index.jsp';
        }

        function handleBackButton(){
            location.href = 'ejb_provider_wizard_step1.jsp?ordinal=1&providerUrl= + <%=jnpProviderUrl%>';
        }

        function setServiceName(){
            var remoteInterfaceName = document.getElementById("remoteInterface").value;
            document.getElementById("serviceName").value =
                remoteInterfaceName.substring(remoteInterfaceName.lastIndexOf(".") + 1) +
                Math.floor((Math.random()*100)+1);
        }
    </script>

    <div id="middle">

        <h2><fmt:message key="create.new.ejb.service.step2"/></h2>

        <div id="workArea">
            <%--<div class="sectionHelp">All the fields marked with * are mandatory</div>--%>
            <div class="sectionSeperator"><fmt:message key="ejb.service.parameters"/></div>
            <form name="addEJBDeployServerForm" method="post"
                  action="deploy_ejb_service.jsp">
                <div class=”sectionSub”>
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med  labelField">
                                <fmt:message key="remote.interface"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <select onchange="setServiceName()" name="remoteInterface"
                                        id="remoteInterface" tabindex="1">
                                    <%
                                        if (classNames == null || classNames[0].equals("")) {
                                    %>
                                            <option value="null"><fmt:message key="no.classes.are.available.in.the.uploaded.jar"/></option>
                                    <%
                                    } else {
                                        for (String className : classNames) {
                                    %>
                                            <option value=<%=className%>><%=className%></option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="bean.jndi.name"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input maxlength="100" size="60" name="beanJNDIName" type="text"
                                       id="beanJNDIName" tabindex="2"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med  labelField">
                                <fmt:message key="service.name"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="serviceName" type="text" id="serviceName" tabindex="3"/>
                            </td>
                        </tr>
                    </table>
            </div>
            <div class="buttonRow">
                <input name="back_1" type="button" tabindex="4"
                       value=" &lt; <fmt:message key="back"/>"
                       onclick="handleBackButton();return false;" class="button"/>
                <input type="button" tabindex="5"
                       value="<fmt:message key="finish"/>"
                       onclick="validateEJBParams()" class="button"/>
                <input type="button" onClick="cancel()" tabindex="6"
                       value="<fmt:message key="cancel"/>" class="button"/>
                <input type="hidden" id="archiveId" value="<%=archiveId%>"
                       name="archiveId"/>
            </div>
            </form>
        </div>
    </div>
</fmt:bundle>