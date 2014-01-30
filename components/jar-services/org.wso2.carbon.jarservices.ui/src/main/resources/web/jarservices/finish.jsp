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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServiceAdminClient" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.Utils" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.jarservices.stub.types.Service" %>
<%@ page import="org.wso2.carbon.jarservices.ui.fileupload.JarServicesConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    Service[] services = Utils.getServices2(request, "method");

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    try {
        JarServiceAdminClient jarServiceAdminClient =
                new JarServiceAdminClient(configContext, backendServerURL, cookie);
        String path = ((UploadArtifactsResponse) session.getAttribute(JarServicesConstants
                .UPLOAD_ARTIFACTS_RESPONSE)).getResourcesDirPath();
        String serviceHierarchy = (String) session
                .getAttribute(JarServicesConstants.SERVICE_HIERARCHY);
        String serviceGroupName = (String) session
                .getAttribute(JarServicesConstants.SERVICE_GROUP_NAME);
        jarServiceAdminClient.createAndDeployService(path,
                serviceHierarchy, serviceGroupName, services);

        // remove attributes from session
        session.removeAttribute(JarServicesConstants.UPLOAD_ARTIFACTS_RESPONSE);
        session.removeAttribute(JarServicesConstants.SERVICE_GROUP_NAME);
        session.removeAttribute(JarServicesConstants.SERVICE_HIERARCHY);

        String msg = "Service archive successfully created. Please refresh this page in a while to see "
                     + "the status of the created Axis2 services";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
%>
<script type="text/javascript">
    location.href = "../service-mgt/index.jsp";
</script>
<%
        return;
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);    
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>