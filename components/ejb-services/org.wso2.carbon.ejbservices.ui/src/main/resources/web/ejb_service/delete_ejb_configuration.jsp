<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String serviceName = request.getParameter("serviceName");

    String forwardTo = "index.jsp";
    try {
        EJBServicesAdminClient serviceAdmin = new EJBServicesAdminClient(config.getServletContext(), session);
        serviceAdmin.deleteEJBConfiguration(serviceName);

        String msg = "EJB Service Conficutation deleted successfully";
        //todo delete the relevant ejb service as well.
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
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