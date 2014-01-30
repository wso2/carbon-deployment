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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.springservices.ui.fileupload.ServiceUploaderClient" %>
<%@ page import="org.wso2.carbon.springservices.ui.SpringServiceMaker" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<fmt:bundle basename="org.wso2.carbon.springservices.ui.i18n.Resources">
    <%
        String redirectionLocation = "";

        String springContextUUID = request.getParameter("springContextUUID");
        String springBeansUUID = request.getParameter("springBeansUUID");

        String beanList = request.getParameter("beanString");
        String[] beans = beanList.split(",");

        String BUNDLE = "org.wso2.carbon.springservices.ui.i18n.Resources";
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        //Obtaining the client-side ConfigurationContext instance.
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serviceHierarchy = (String) session.getAttribute("service.hierarchy");

        ServiceUploaderClient client = new ServiceUploaderClient(configContext, serverURL, cookie);
        SpringServiceMaker maker = new SpringServiceMaker(configContext, request.getLocale());

        try {
            maker.createAndUploadSpringBean(serviceHierarchy,
                    springContextUUID, client, springBeansUUID, beans);
            String msg = bundle.getString("spring.successfull");
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
            redirectionLocation = "../service-mgt/index.jsp";
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
            redirectionLocation = "index.jsp";
        }

    %>

    <script type="text/javascript">
        location.href = "<%=redirectionLocation%>";
    </script>
</fmt:bundle>