<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String archiveId = request.getParameter("archiveId");
    String serviceName = request.getParameter("serviceName");
    String beanJNDIName = request.getParameter("beanJNDIName");
    String remoteInterface = request.getParameter("remoteInterface");
    String[] serviceClasses = new String[]{remoteInterface};
    String jnpProviderUrl = (String) session.getAttribute("providerUrl");
    session.removeAttribute("providerUrl");

    String forwardTo;
    String msg;
    CarbonUIMessage carbonMessage = null;
    try {
        EJBServicesAdminClient serviceAdmin = new EJBServicesAdminClient(config.getServletContext(), session);
        serviceAdmin.createAndDeployEJBService(archiveId, serviceName, serviceClasses, jnpProviderUrl, beanJNDIName, remoteInterface);
        msg = "Files have been uploaded "
                    + "successfully. Please refresh this page in a while to see "
                    + "the status of the created EJB service";
        forwardTo = "../service-mgt/index.jsp";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        forwardTo="index.jsp";
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }

%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
