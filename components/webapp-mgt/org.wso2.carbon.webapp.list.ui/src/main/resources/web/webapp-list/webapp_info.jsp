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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
<%@ page import="org.wso2.carbon.webapp.list.ui.WebappAdminClient" %>
<%@ page import="org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata" %>
<%@ page import="org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappStatistics" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.webapp.list.ui.WebAppDataExtractor" %>
<%@page import="org.wso2.carbon.webapp.mgt.stub.types.carbon.VhostHolder"%>

<fmt:bundle basename="org.wso2.carbon.webapp.list.ui.i18n.Resources">
<carbon:breadcrumb
        label="webapp.dashboard"
        resourceBundle="org.wso2.carbon.webapp.list.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    response.setHeader("Cache-Control", "no-cache");

    String webappFileName = request.getParameter("webappFileName");
    String webappState = request.getParameter("webappState");
    String hostName = request.getParameter("hostName");
    String httpPort = request.getParameter("httpPort");
    String webappType = request.getParameter("webappType");
    String defaultHostName = request.getParameter("defaultHostName");

    WebAppDataExtractor webAppDataExtractor =new WebAppDataExtractor();
    List wsdlURLS=null;
    List wadlURLS=null;
    String serviceListPath = null;
    VhostHolder vhostHolder = null;

    String servletContext = "/";

    String proxyContextPath = CarbonUtils.getProxyContextPath(false);
    String workerProxyContextPath = CarbonUtils.getProxyContextPath(true);
    String resolveProxyPath = "";// resolved proxy  path for worker / manager

    if ("".equals(workerProxyContextPath)) {
        resolveProxyPath = proxyContextPath;
    } else{
        resolveProxyPath = workerProxyContextPath;
    }

    String urlPrefix = "http://" + hostName + ":" + httpPort + resolveProxyPath;
    String defaultPrefix = "http://" + defaultHostName + ":" + httpPort + resolveProxyPath;

    if (webappState == null) {
        webappState = "started";
    }
    if (webappFileName != null && webappFileName.trim().length() > 0) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        WebappAdminClient client;
        WebappMetadata webapp;
        try {
            client = new WebappAdminClient(cookie, backendServerURL, configContext, request.getLocale());
            vhostHolder = client.getVhostHolder();
            if (vhostHolder.getDefaultHostName().equals(hostName)) {
                urlPrefix = defaultPrefix;
            }
            if (webappState.equalsIgnoreCase("all")) {
                webapp = client.getStartedWebapp(webappFileName, hostName);
                if(webapp == null) {
                    webapp = client.getStoppedWebapp(webappFileName, hostName);
                }
                if(webappType.equalsIgnoreCase("JaxWebapp")) {
                    webAppDataExtractor.getServletXML(client.getWarFileInputStream(webapp.getWebappFile(), hostName, webappType));
                    wsdlURLS= webAppDataExtractor.getWSDLs(urlPrefix + webapp.getContext() + servletContext);
                    wadlURLS= webAppDataExtractor.getWADLs(urlPrefix + webapp.getContext() + servletContext);
                    serviceListPath = webAppDataExtractor.getServiceListPath();
                }
            }
            else if (webappState.equalsIgnoreCase("started")) {
                webapp = client.getStartedWebapp(webappFileName,hostName);
                if (webappType.equalsIgnoreCase("JaxWebapp")) {
                    webAppDataExtractor.getServletXML(client.getWarFileInputStream
                            (webapp.getWebappFile(), hostName, webappType));
                    wsdlURLS= webAppDataExtractor.getWSDLs(urlPrefix + webapp.getContext() + servletContext);
                    wadlURLS= webAppDataExtractor.getWADLs(urlPrefix + webapp.getContext() + servletContext);
                    serviceListPath = webAppDataExtractor.getServiceListPath();
                }

            } else {
                webapp = client.getStoppedWebapp(webappFileName,hostName);
            }
            if (webapp == null) {
                String msg = "Webapp is null. webappFileName: " + webappFileName + ", webappState=" + webappState;
                System.err.println(msg);
                throw new ServletException(msg);
            }
            if(webappType == null) {
                webappType = webapp.getWebappType();
            }
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>
<script type="text/javascript">

    function expireSessions() {
        var idleTimeVal = document.getElementById("idleTime").value;
        if( idleTimeVal== "") {
            return;
        } else if(!isNumeric(idleTimeVal)) {
            CARBON.showWarningDialog('<fmt:message key="expiration.session.must.be.real.number"/>');
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="session.expiry.webapp.prompt"/>",
                                          function() {
                                              document.sessionExpiryForm.setAttribute("action", "expire_sessions.jsp");
                                              document.sessionExpiryForm.submit();
                                          }
            );
        }

    }

    function expireAllSessions() {
        CARBON.showConfirmationDialog("<fmt:message key="session.expiry.selected.webapps.prompt"/>",
                                      function() {
                                          location.href = 'expire_sessions.jsp?webappKey=<%= hostName+':'+ URLEncoder.encode(webappFileName, "UTF-8")%>&redirectPage=webapp_info.jsp'
                                          + '&hostName=<%= hostName %>&httpPort=<%= httpPort %>&webappType=<%= webappType %>&defaultHostName=<%= defaultHostName %>';
                                      }
                );
    }

    function reloadWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="reload.selected.webapps.prompt"/>",
                                      function() {
                                          location.href = 'reload_webapps.jsp?webappKey=<%= hostName+':'+ URLEncoder.encode(webappFileName, "UTF-8") %>&redirectPage=webapp_info.jsp'
                                                  +'&hostName=<%= hostName %>&httpPort=<%= httpPort %>&webappType=<%= webappType %>&defaultHostName=<%= defaultHostName %>';
                                      }
                );
    }

    function stopWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="stop.selected.webapps.prompt"/>",
                                      function() {
                                          location.href = 'stop_webapps.jsp?webappKey=<%= hostName+':'+URLEncoder.encode(webappFileName, "UTF-8") %>&redirectPage=webapp_info.jsp'
                                                  +'&hostName=<%= hostName %>&httpPort=<%= httpPort %>&defaultHostName=<%= defaultHostName %>';
                                      }
                );
    }

    function startWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="start.selected.webapps.prompt"/>",
                                      function() {
                                          location.href = 'start_webapps.jsp?webappKey=<%=hostName+':'+ URLEncoder.encode(webappFileName, "UTF-8") %>&redirectPage=webapp_info.jsp'
                                                  +'&hostName=<%= hostName %>&httpPort=<%= httpPort %>&webappType=<%= webappType %>&defaultHostName=<%= defaultHostName %>';
                                      }
                );
    }

    function isNumeric(value) {
        var numericExpression = /^\d*(\.\d*)?$/;
        if (value.match(numericExpression)) {
            return true;
        } else {
            return false;
        }
    }

    //    try it feature for jaxws webapps
    function validateAndSubmitTryit(inputObj) {
        if (inputObj == "") {
            CARBON.showWarningDialog('<fmt:message key="tryit.error.msg"/>');
            return;
        }
        var urlSegments = document.location.href.split("/");
        var resourcePath = urlSegments[3];
        var frontendURL = wso2.wsf.Util.getServerURL() + "/";
        var proxyAddress = getProxyAddress();

        var bodyXml = '<req:generateTryit xmlns:req="http://org.wso2.wsf/tools">\n' +
                '<url><![CDATA[' + inputObj + ']]></url>\n' +
                '<hostName><![CDATA[' + HOST + ']]></hostName>\n' +
                '</req:generateTryit>\n';


        var callURL = wso2.wsf.Util.getBackendServerURL(frontendURL, "<%=CarbonUIUtil.getAdminConsoleURL(request).split("/carbon/")[0]+"/services/"%>") + "ExternalTryitService" ;
        wso2.wsf.Util.cursorWait();
        new wso2.wsf.WSRequest(callURL, "urn:generateTryit", bodyXml, wcserviceClientCallback, [2], undefined, proxyAddress);
    }

    //    call back function for try it feature
    function wcserviceClientCallback() {
        var data = this.req.responseXML;
        var returnElementList = data.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = data.getElementsByTagName("return");
        var responseTextValue = returnElementList[0].firstChild.nodeValue;
        window.open(responseTextValue);
    }



</script>

<script type="text/javascript">
   function add(myepr){
        CARBON.showInputDialog("Enter Service Specification identifier :\n",function(inputVal){
            jQuery.ajax({
                            type: "POST",
                            url: "../urlmapper/contextMapper.jsp",
                            data: "type=add&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal + "&endpointType=Endpoint_1",
                            success: function(msg){
                                 CARBON.showConfirmationDialog( msg.trim() );
                            }
                        });
        });
    }
</script>
<script type="text/javascript">
   function sendToURLMapper(myepr){
            jQuery.ajax({
                            type: "POST",
                            url: "../urlmapper/index.jsp",
                            data: "type=add&carbonEndpoint=" + myepr,
                            success: function(msg){

                            }
                        });
    }
</script>
<div id="middle">
    <h2><fmt:message key="webapp.dashboard"/> (<%= webapp.getContext() %>)</h2>

    <div id="workArea">

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <td width="50%">
                    <table class="styledLeft" id="webappTable" style="margin-left: 0px;"
                           width="100%">
                        <thead>
                        <tr>
                            <th colspan="2" align="left"><fmt:message
                                    key="webapp.details"/></th>
                        </tr>
                        </thead>
                        <tr>
                            <td width="30%">
                                <%
                                    if("JaxWebapp".equalsIgnoreCase(webappType)) {
                                %><fmt:message key="application.services"/><%
                                    } else {
                                %><fmt:message key="webapp.context"/><%
                                    }
                                %>
                            </td>
                            <td>

                                <%
                                    if ("Stopped".equalsIgnoreCase( webapp.getState())) {
                                %>
                                <%=webapp.getContext()%>
                                <%
                                } else {

                                    if (webappType.equalsIgnoreCase("JaxWebapp")) {
                                     servletContext = webapp.getServletContext();
                                %>
                                <a href="<%= urlPrefix + webapp.getContext() + servletContext + serviceListPath%>"
                                   target="_blank">
                                    <%=webapp.getContext() + servletContext +serviceListPath%>
                                </a>
                                <%
                                    }
                                    else {
                                %>
                                <a href="<%= urlPrefix + webapp.getContext()+"/"%>" target="_blank">
                                    <%=webapp.getContext()%>
                                </a>
                                <%
                                    }
                                }
                                %>

                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="webapp.display.name"/></td>
                            <td><%=(webapp.getDisplayName() != null ? webapp.getDisplayName() : "")%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="webapp.file"/></td>
                            <td><%=webapp.getWebappFile()%>
                            </td>

                        </tr>
                        <tr>
                            <td><fmt:message key="webapp.state"/></td>
                            <td><%=webapp.getState()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="last.modified.time"/></td>
                            <td><%= new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss").format(new Date(webapp.getLastModifiedTime()))%>
                            </td>
                        </tr>
                    </table>
                    <br>
                    <table class="styledLeft" id="operationsTable"
                                               style="margin-left: 0px;" width="100%">
                                            <thead>
                                            <tr>
                                                <th><fmt:message key="operations"/></th>
                                            </tr>
                                            </thead>
                                            <% if (webapp.getStarted()) { %>
                                            <tr>
                                                <td>

                                                    <a href="#" onclick="expireSessions()" class="icon-link"
                                                       style='background-image:url(images/expire_timestamp.gif)'>
                                                        <fmt:message key="expire.sessions"/>
                                                    </a>
                                                    <nobr>
                                                        <form name="sessionExpiryForm" onsubmit="expireSessions();return false;" >
                                                            <input type="hidden" name="webappKey"
                                                                   value="<%=  hostName+':'+ URLEncoder.encode(webappFileName, "UTF-8")%>"/>
                                                            <input type="hidden" name="redirectPage"
                                                                   value="webapp_info.jsp"/>
                                                            <input type="hidden" name="hostName"
                                                                   value="<%= hostName %>"/>
                                                            <input type="hidden" name="httpPort"
                                                                   value="<%= httpPort %>"/>
                                                            <input type="hidden" name="webappType"
                                                                   value="<%= webappType %>"/>
                                                            <input type="hidden" name="defaultHostName"
                                                                   value="<%= defaultHostName %>"/>
                                                            <label>
                                                                &nbsp;<fmt:message key="with.idle"/> &ge;
                                                                <input type="text" size="10" name="sessionExpiryTime"
                                                                       id="idleTime"/>
                                                                &nbsp;<fmt:message key="minutes"/>
                                                            </label>
                                                        </form>
                                                    </nobr>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <a href="#" onclick="expireAllSessions()" class="icon-link"
                                                       style='background-image:url(images/expire_session.gif)'>
                                                        <fmt:message key="expire.all.session"/>s
                                                    </a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <a href="#" onclick="reloadWebapp()" class="icon-link"
                                                       style='background-image:url(images/reload.gif)'>
                                                        <fmt:message key="webapps.reload"/>
                                                    </a>
                                                </td>
                                            </tr>
                                            <% } %>
                                            <tr>
                                                <td>
                                                    <% if (webapp.getStarted()) {%>
                                                    <a href="#" onclick="stopWebapp()" class="icon-link"
                                                       style='background-image:url(images/stop.gif)'>
                                                        <fmt:message key="webapps.stop"/>
                                                    </a>
                                                    <% } else { %>
                                                    <a href="#" onclick="startWebapp()" class="icon-link"
                                                       style='background-image:url(images/start.gif)'>
                                                        <fmt:message key="webapps.start"/>
                                                    </a>
                                                    <% } %>
                                                </td>
                                            </tr>
                                            <tr>
                                                <script type="text/javascript">

                                                </script>
                                                <%--<td>--%>
                                                            <% /* String cek = "";
                                                                boolean checked = false;
                                                                String webAppName = webappFileName;
                                                                if(webappFileName.contains(".war")){
                                                                    webAppName = webappFileName.trim().substring(0, webappFileName.length()-4);
                                                                }
                                                                WebappStatPublisherAdminClient adminClient = new WebappStatPublisherAdminClient(
                                                                        cookie, backendServerURL, configContext, request.getLocale());


                                                                try{

                                                                    checked = adminClient.getWebappConfigData(webAppName);

                                                                }catch (Exception e) {
                                                                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                                                                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
                                                                    e.printStackTrace();
                                                                 }

                                                                if(checked){
                                                                    cek = "checked=\"checked\"";
                                                                }
                    */

                                                    %>
                                             <%--       <input type="checkbox" name="bam_stats" value="1" id="bam_statistics" class="bam_statistics" >  Enable BAM Statistics--%>
                                               <%-- </td>--%>
                                            </tr>
                                             <%
                                        if(CarbonUIUtil.isContextRegistered(config, "/urlmapper/")){ %>
                                             <tr>
                                               <td width="50%"><nobr>
                                            <a class="icon-link" style="background-image: url(images/url-rewrite.png);"
                        href="../urlmapper/index.jsp?carbonEndpoint=<%=webapp.getContext()%>&apptype=<%=webappType%>&servletContext=<%=servletContext%>">
                                                URL Mappings
                                            </a></nobr>
                                        </td>
                                            </tr>
                                               <%

                                       			 }
                               				   %>
                                            <tr>
                                        </table>
                                        <br>
                                        <%   if(CarbonUIUtil.isContextRegistered(config, "/bampubsvcstat/") && webapp.getStarted()) { %>
                                        <table class="styledLeft" id="serviceTable"
                                               style="margin-left: 0px;" width="100%">
                                            <thead>
                                            <tr>
                                                <th><fmt:message key="configurations"/></th>
                                            </tr>
                                            </thead>
                                            <tr>
                                                <script type="text/javascript">

                                                </script>
                                                <td>

                                                    <%  String cek = "";
                                                        String enable = "style=\"display: none;\"";
                                                        String disable = "style=\"display: none;\"";
                                                        boolean checked = false;
                                                        String webAppName = webappFileName;
                                                        WebappAdminClient adminClient = new WebappAdminClient(
                                                                cookie, backendServerURL, configContext, request.getLocale());


                                                        try{

                                                            checked = adminClient.getBamConfig(webAppName,hostName);

                                                        }catch (Exception e) {
                                                            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                                                            session.setAttribute(CarbonUIMessage.ID, uiMsg);
                                                            e.printStackTrace();
                                                        }

                                                        if(checked){
                                                            enable = "";
                                                        } else {
                                                            disable = "";
                                                        }


                                                    %>

                                                    <div id="bam_statistic_enabled" <%= enable %> >
                                                        <fmt:message key="bam.statistic.monitoring.enable"/> [<a id="bam_statistcs_enable_url" type=""><font style="color:red;">Deactivate</font></a>]
                                                    </div>
                                                    <div id="bam_statistic_disabled" <%= disable %> >
                                                        <fmt:message key="bam.statistic.monitoring.disable"/> [<a id="bam_statistcs_disable_url" type=""><font style="color:green;">Activate</font></a>]
                                                    </div>


                                                </td>
                                            </tr>
                                        </table>
                                        <% } else { %>
                                        &nbsp;
                                        <% } %>

                    <br>
                    <%   if(CarbonUIUtil.isContextRegistered(config, "/statistics/")){ %>

                                        <div id="result"></div>
                                        <div id="statsDiv" >
                                            <script type="text/javascript" src="../statistics/js/statistics.js"></script>
                                            <script type="text/javascript" src="../statistics/js/graphs.js"></script>

                                            <script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
                                            <script type="text/javascript" src="../admin/js/excanvas.js"></script>
                                            <script type="text/javascript" src="global-params.js"></script>
                                            <script type="text/javascript">
                                                initResponseTimeGraph('50');
                                            </script>
                                            <script type="text/javascript">
                                                jQuery.noConflict();
                                                var refresh;
                                                function refreshStats() {
                                                    var url = "../statistics/webapplication_stats_ajaxprocessor.jsp?webAppNameName=<%= URLEncoder.encode(webappFileName, "UTF-8") %>";
                                                    try {
                                                        jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                                                            if (status != "success") {
                                                                stopRefreshStats();
                                                                document.getElementById('result').innerHTML = responseText;
                                                            }
                                                        });
                                                    } catch (e){} // ignored
                                                }
                                                function stopRefreshStats() {
                                                    if (refresh) {
                                                        clearInterval(refresh);
                                                    }
                                                }
                                                try {
                                                    jQuery(document).ready(function() {
                                                        refreshStats();
                                                        if (document.getElementById('statsDiv').style.display == ''){
                                                            refresh = setInterval("refreshStats()", 6000);
                                                        }
                                                    });
                                                } catch (e){} // ignored
                                            </script>
                                        </div>


                                        <%

                                            }
                                            else {
                                        %>
                                        &nbsp;

                                        <%

                                            }
                                        %>

                </td>

                <td width="10px">&nbsp;</td>


                            <% if (webappType.equalsIgnoreCase("JaxWebapp") && wsdlURLS != null) { %>

                <%--<td width="10px">&nbsp;</td>--%>

                <td width="50%">
                        <table class="styledLeft" id="wsClientTable"
                           style="margin-left: 0px;" width="100%">
                        <thead>
                        <tr>
                            <th colspan="3"><fmt:message key="availableWS"/></th>
                        </tr>
                        </thead>

                        <% for(int i=0;i<wsdlURLS.size();i++){%>
                        <tr>
                            <td colspan="2" align="left">

                              <%
                                String wsdlURL = wsdlURLS.get(i).toString();
                                String serviceName = wsdlURL.substring( wsdlURL.indexOf(servletContext) , wsdlURL.indexOf("?wsdl") );
                              %>
                              <strong>Service:</strong><%= " "+serviceName%>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="#" onclick="validateAndSubmitTryit('<%=wsdlURLS.get(i)%>')" class="icon-link"
                                   style='background-image:url(images/tryit.gif)'>
                                    <fmt:message key="tryit"/>
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <a href="../wsdl2code/index.jsp?generateClient=<%=wsdlURLS.get(i)%>&toppage=false&resultType=cxf&api=jaxws" class="icon-link"
                                   style='background-image:url(images/genclient.gif)'>
                                    <fmt:message key="generate.jaxws.client"/>
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td width="50%">
                                <a href="<%=wsdlURLS.get(i)%>" class="icon-link"
                                   style="background-image:url(images/wsdl.gif);" target="_blank">
                                    WSDL1.1
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="left">
                                <strong><fmt:message key="endpoints"/></strong>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <%
                                    //String wsdlURL = wsdlURLS.get(i).toString();
                                    String jaxWSEpr = wsdlURL.substring( 0, wsdlURL.indexOf("?wsdl") );
                                %>
                                <%=jaxWSEpr%>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="1">&nbsp;</td>
                        </tr>
                        <% }%>
                    </table>


                </td>
                 <% } %>


                        <% if (webappType.equalsIgnoreCase("JaxWebapp") && wadlURLS != null) { %>
                <%--<td width="10px">&nbsp;</td>--%>

                <td width="50%">
                    <table class="styledLeft" id="rsClientTable"
                           style="margin-left: 0px;" width="100%">
                        <thead>
                        <tr>
                            <th colspan="3"><fmt:message key="availableRS"/></th>
                        </tr>
                        </thead>

                         <% for(int i=0;i<wadlURLS.size();i++){%>
                         <tr>
                                                     <td colspan="2" align="left">

                                                       <%
                                                         String wadlURL = wadlURLS.get(i).toString();
                                                         String JAXRSServiceName = wadlURL.substring( wadlURL.indexOf(servletContext) , wadlURL.indexOf("?_wadl") );
                                                       %>
                                                       <strong>Service:</strong><%= " "+JAXRSServiceName%>
                                                     </td>
                         </tr>
                        <tr>
                            <td>
                                <a href="../wsdl2code/index.jsp?generateClient=<%=wadlURLS.get(i)%>&toppage=false&resultType=cxf&api=jaxrs" class="icon-link"
                                   style='background-image:url(images/genclient.gif)'>
                                    <fmt:message key="generate.jaxrs.client"/>
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td width="50%">
                                <a href="<%=wadlURLS.get(i)%>" class="icon-link"
                                   style="background-image:url(images/wsdl.gif);" target="_blank">
                                    WADL
                                </a>
                            </td>
                                <%--<td>--%>
                                <%--<a href="../tryit/rest.jsp?wadlURL=<%=value%>" class="icon-link"--%>
                                <%--style='background-image:url(images/tryit.gif)'>--%>
                                <%--<fmt:message key="tryit"/>--%>
                                <%--</a>--%>
                                <%--</td>--%>
                        </tr>
                        <%--<tr>
                            <td>  <% /*System.out.println(URLEncoder.encode(wadlURLS.get(i).toString().replace("?_wadl", ""), "UTF-8"));
                                System.out.println(wadlURLS.get(i).toString().substring(0,wadlURLS.get(i).toString().indexOf("services")));
                                System.out.println(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + " : "+ request.getContextPath());*/
                            %>
                                <a href="<%=defaultPrefix%>/services?wadltryit&resourceurl=<%=URLEncoder.encode(wadlURLS.get(i).toString().replace("?_wadl", ""), "UTF-8")%>" class="icon-link" style="background-image:url(images/tryit.gif);" target="_blank">  Try this </a>
                            </td>
                        </tr>--%>
                        <tr>
                            <td colspan="2" align="left">
                                <strong><fmt:message key="endpoints"/></strong>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <%
                                    //String wadlURL = wadlURLS.get(i).toString();
                                    String jaxRSEpr = wadlURL.substring( 0, wadlURL.indexOf("?_wadl") );
                                %>
                                <%=jaxRSEpr%>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="1">&nbsp;</td>
                        </tr>
                        <%}%>
                    </table>
                </td>
            <%}%>


                <%
                    if(!"JaxWebapp".equalsIgnoreCase(webappType)) {
                %>
                <td width="50%" id="sessionStatsDiv">

                <table class="styledLeft" id="sessionStatsTable"
                       style="margin-left: 0px;" width="100%">
                    <%
                        WebappStatistics stats = webapp.getStatistics();
                    %>
                    <thead>
                    <tr>
                        <th colspan="2" align="left">
                            <fmt:message key="session.statistics"/>
                        </th>
                    </tr>
                    </thead>
                    <tr>
                        <td><fmt:message key="active.sessions"/></td>
                        <td>
                            <% if (stats.getActiveSessions() > 0) { %>
                            <a href="sessions.jsp?webappFileName=<%= URLEncoder.encode(webapp.getWebappFile(), "UTF-8") %>&hostName=<%=webapp.getHostName()%>">
                                <%= stats.getActiveSessions()%>
                            </a>
                            <% } else { %>
                            <%= stats.getActiveSessions()%>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="expired.sessions"/></td>
                        <td><%= stats.getExpiredSessions()%>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="maximum.active.sessions"/></td>
                        <td><%= stats.getMaxActiveSessions()%>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="rejected.sessions"/></td>
                        <td><%= stats.getRejectedSessions()%>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="average.session.lifetime"/></td>
                        <td><%= stats.getAvgSessionLifetime()%>&nbsp;s</td>
                    </tr>
                    <tr>
                        <td><fmt:message key="maximum.session.lifetime"/></td>
                        <td><%= stats.getMaxSessionLifetime()%>&nbsp;s</td>
                    </tr>
                    <tr>
                        <td><fmt:message key="maximum.session.inactivity.interval"/></td>
                        <td><%= stats.getMaxSessionInactivityInterval()%>&nbsp;s</td>
                    </tr>
                </table>
                </td>
                <%}%>
            </tr>

        </table>


        <script type="text/javascript">
            alternateTableRows('webappTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('sessionStatsTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('operationsTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('wsClientTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('rsClientTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <%
            }
        %>
    </div>
</div>
<script type="text/javascript">

        jQuery("#bam_statistcs_disable_url").live("click", function (){
            var dataVal = "webappFileName="+'<%= URLEncoder.encode(webappFileName, "UTF-8") %>';
            dataVal = dataVal+"&hostName=<%=hostName%>";
            dataVal = dataVal + '&value=1'
            jQuery.ajax({
                type: "POST",
                url: "bam_activator.jsp",
                data: dataVal,
                success: function(msg){
                    //  CARBON.showConfirmationDialog( msg.trim());
                }
            });
            jQuery("#bam_statistic_disabled").hide();
            jQuery("#bam_statistic_enabled").show();
        });

        jQuery("#bam_statistcs_enable_url").live("click", function (){
            var dataVal = "webappFileName="+'<%= URLEncoder.encode(webappFileName, "UTF-8") %>';
            dataVal = dataVal+"&hostName=<%=hostName%>";
            dataVal = dataVal + '&value=0'

            jQuery.ajax({
                type: "POST",
                url: "bam_activator.jsp",
                data: dataVal,
                success: function(msg){
                    //  CARBON.showConfirmationDialog( msg.trim());
                }
            });
            jQuery("#bam_statistic_enabled").hide();
            jQuery("#bam_statistic_disabled").show();
        });



</script>
</fmt:bundle>
