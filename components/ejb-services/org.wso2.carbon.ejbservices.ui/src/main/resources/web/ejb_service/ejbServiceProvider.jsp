<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String serverType = request.getParameter("serverType");
    String providerURL = request.getParameter("providerUrl");
    String jndiContextClass = request.getParameter("jndiContextClass");
    String jndiUserName = request.getParameter("userName");
    String password = request.getParameter("password");
    try {
        EJBServicesAdminClient serviceAdmin = new EJBServicesAdminClient(config.getServletContext(), session);
        serviceAdmin.addApplicationServer(providerURL, jndiContextClass, jndiUserName, password, serverType);        
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "index.jsp";
    }
</script>

<script type="text/javascript">
    forward();
</script>