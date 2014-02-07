<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="js/ejbservices.js"></script>

<script type="text/javascript">

    function validateServiceParameters() {
        document.updateServiceParamForm.submit();
    }

</script>

<%
    String serviceName = request.getParameter("serviceName");
    Map paramMap = null;
    String remoteInterfaceName = "";
    String beanJndiName = "";
    String jndiUser= "";
    String jndiPassword = "";
    String providerUrl = "";
    String serviceType = "";
    String jndiContextClass = "";
    String serviceClass = "";

    try {
        EJBServicesAdminClient serviceAdmin = new EJBServicesAdminClient(config.getServletContext(), session);
        paramMap = serviceAdmin.getServiceParameters(serviceName);
        remoteInterfaceName = (String) paramMap.get("remoteInterfaceName");
        beanJndiName = (String) paramMap.get("beanJndiName");
        jndiUser = (String) paramMap.get("jndiUser");
        jndiPassword = (String) paramMap.get("jndiPassword");
        providerUrl = (String) paramMap.get("providerUrl");
        serviceType = (String) paramMap.get("serviceType");
        jndiContextClass = (String) paramMap.get("jndiContextClass");
        serviceClass = (String) paramMap.get("ServiceClass");

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
 %>
        <script type="text/javascript">
               location.href = "index.jsp";
        </script>
<%
        return;
    }
%>


<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="ejb.service.parameters"
		resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
    <div id="middle">
        <div id="workArea">

            <form action="update_service_param.jsp" name="updateServiceParamForm" method="post">
                <div class="sectionSeperator"><fmt:message key="ejb.service.parameters"/></div>
                <div class=”sectionSub”>
                    <table  class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="service.name"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="serviceName" type="text" value="<%=serviceName%>" disabled="true" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="remote.interface.name"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="remoteInterfaceName" type="text" value="<%=remoteInterfaceName%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">

                                <fmt:message key="bean.jndi.name"/><span class="required">*</span></td>
                            <td class="labelField">
                                <input name="beanJndiName" type="text" value="<%=beanJndiName%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="jndi.user"/></td>
                            <td class="labelField">
                                <input name="jndiUser" type="text" value="<%=jndiUser%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="jndi.password"/></td>
                            <td class="labelField">
                                <input name="jndiPassword" type="text" value="<%=jndiPassword%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="provider.url"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="providerUrl" type="text" value="<%=providerUrl%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="service.type"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="serviceType" type="text" value="<%=serviceType%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="jndi.context.class"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="jndiContextClass" type="text" value="<%=jndiContextClass%>" size="50">
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="service.class"/><span class="required">*</span>
                            </td>
                            <td class="labelField">
                                <input name="serviceClass" type="text" value="<%=serviceClass%>" disabled="true" size="50">
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="buttonRow">
                    <input type="button" class="button" value="<fmt:message key="update"/>"
                           id="updateServiceParameters" onclick="validateServiceParameters()"/>&nbsp;&nbsp;
                    <input type="reset" class="button"  value="<fmt:message key="reset"/>"
                           id="reset" onclick="function resetValues() {
                            document.updateServiceParamForm.remoteInterfaceName.value = <%=remoteInterfaceName%>;
                            document.updateServiceParamForm.beanJndiName.value = <%=beanJndiName%>;
                            document.updateServiceParamForm.jndiUser.value = <%=jndiUser%>;
                            document.updateServiceParamForm.jndiPassword.value = <%=jndiPassword%>;
                            document.updateServiceParamForm.providerUrl.value = <%=providerUrl%>;
                            document.updateServiceParamForm.serviceType.value = <%=serviceType%>;
                            document.updateServiceParamForm.jndiContextClass.value = <%=jndiContextClass%>;
                            }
                            resetValues()"/>&nbsp;&nbsp;
                    <input type="button" class="button" value="<fmt:message key="cancel"/>"
                           id="cancel" onclick="location.href='../ejb_service/index.jsp';"/>&nbsp;&nbsp;
                    <input type="hidden" id="serviceName" value="<%=serviceName%>"
                           name="serviceName"/>
                </div>
            </form>
        </div>

    </div>
</fmt:bundle>