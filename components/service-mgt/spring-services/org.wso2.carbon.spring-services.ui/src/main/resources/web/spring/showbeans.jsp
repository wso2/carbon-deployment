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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.springservices.ui.SpringServiceMaker" %>
<%@ page import="org.wso2.carbon.springservices.ui.SpringBeansData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>



<%
    String springContextUUID = request.getParameter("springContextUUID");
    String springBeansUUID = request.getParameter("springBeansUUID");

    /**
     * Get all the beans to show
     */

    //Obtaining the client-side ConfigurationContext instance.
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    ClassLoader loader = (ClassLoader) config.getServletContext()
            .getAttribute(CarbonConstants.BUNDLE_CLASS_LOADER);

    SpringBeansData data = null;
    int totalBeans = 0;
    try {
        SpringServiceMaker maker = new SpringServiceMaker(configContext, request.getLocale());
        data = maker.getSpringBeanNames(springContextUUID, springBeansUUID, loader);
        totalBeans = data.getBeans().length;
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

<fmt:bundle basename="org.wso2.carbon.springservices.ui.i18n.Resources">
<carbon:breadcrumb 
		label="select.beans.to.deploy"
		resourceBundle="org.wso2.carbon.springservices.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />
    <script type="text/javascript">

        function generate() {
            validateBeans();
            var classes = document.getElementsByName("chkBeans");
            var arrayString = '';
            var isClassSelected = false;
            for (var i = 0; i < classes.length; i++) {
                if (classes[i].checked) {
                    isClassSelected = true;
                    arrayString += classes[i].value + ',';
                }
            }
            if (!isClassSelected) {
                CARBON.showErrorDialog("<fmt:message key="please.select.bean"/>");
                return;
            }
            document.selectionForm.action = "generatebeans.jsp?springContextUUID=<%=springContextUUID%>&springBeansUUID=<%=springBeansUUID%>&beanString=" + arrayString.substring(0, arrayString.length - 1);
            document.selectionForm.submit();
        }

        function selectAll(checkboxes, select) {
            for (var i = 0; i < checkboxes.length; i++) {
                checkboxes[i].checked = select;
            }
        }

        function init() {
            validateBeans();
        }

        function validateBeans() {
            var beans = <%=totalBeans%>;
            if (beans == 0) {
                CARBON.showWarningDialog('<fmt:message key="no.beans.found"/>..');
            }
        }

    </script>

    <script type="text/javascript">
        init();
    </script>


    <div id="middle">

        <h2><fmt:message key="select.beans.to.deploy"/></h2>

        <div id="workArea">
            <form name="selectionForm" method="post" action="">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="select.spring.beans.exposed"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="buttonRow">
                            <div class="buttonrowTop">
                                <input type="button" class="button"
                                       value=" <fmt:message key="generate"/> "
                                       onclick="generate();"
                                       tabindex="1"/>
                                &#160;&#160;&#160;&#160;&#160;
                                <input type="button" class="button"
                                       onclick="selectAll(document.getElementsByName('chkBeans'), true);"
                                       value=" <fmt:message key="select.all"/> " tabindex="2"/>&#160;&#160;&#160;&#160;&#160;
                                <input type="button" class="button"
                                       onclick="selectAll(document.getElementsByName('chkBeans'), false);"
                                       value="<fmt:message key="select.none"/>" tabindex="3"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <table class="innerTable">
                                <thead>
                                <tr>
                                    <th><fmt:message key="bean.name"/></th>
                                    <th><fmt:message key="include"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <%
                                    for (String beanName : data.getBeans()) {
                                %>
                                <tr>
                                    <td>
                                        <%=beanName%>
                                    </td>
                                    <td>
                                        <input type="checkbox" name="chkBeans" value="<%=beanName%>"
                                               tabindex="position() + 3"/>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <div class="buttonrowBottom">
                                <input type="button" class="button"
                                       value=" <fmt:message key="generate"/> "
                                       onclick="generate();"
                                       tabindex="1">
                                &#160;&#160;&#160;&#160;&#160;
                                <input type="button" class="button"
                                       onclick="selectAll(document.getElementsByName('chkBeans'), true);"
                                       value=" <fmt:message key="select.all"/> " tabindex="2"/>&#160;&#160;&#160;&#160;&#160;
                                <input type="button" class="button"
                                       onclick="selectAll(document.getElementsByName('chkBeans'), false);"
                                       value=" <fmt:message key="select.none"/> " tabindex="3"/>
                            </div>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>