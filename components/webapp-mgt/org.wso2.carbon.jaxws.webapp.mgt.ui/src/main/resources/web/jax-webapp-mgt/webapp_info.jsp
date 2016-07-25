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
<%@ page import="org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata" %>
<%@ page import="org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappStatistics" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.jaxws.webapp.mgt.ui.JaxwsWebappAdminClient" %>

<fmt:bundle basename="org.wso2.carbon.jaxws.webapp.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="webapp.dashboard"
        resourceBundle="org.wso2.carbon.jaxws.webapp.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    response.setHeader("Cache-Control", "no-cache");

    String webappFileName = request.getParameter("webappFileName");
    String webappState = request.getParameter("webappState");
    String hostName = request.getParameter("hostName");
    String httpPort = request.getParameter("httpPort");

    String urlPrefix = "http://" + hostName + ":" + httpPort;

    if (webappState == null) {
        webappState = "started";
    }
    if (webappFileName != null && webappFileName.trim().length() > 0) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        JaxwsWebappAdminClient client;
        WebappMetadata webapp;
        try {
            client = new JaxwsWebappAdminClient(cookie, backendServerURL, configContext, request.getLocale());
            if (webappState.equalsIgnoreCase("started")) {
                webapp = client.getStartedWebapp(webappFileName);
            } else {
                webapp = client.getStoppedWebapp(webappFileName);
            }
            if (webapp == null) {
                String msg = "Webapp is null. webappFileName: " + webappFileName + ", webappState=" + webappState;
                System.err.println(msg);
                throw new ServletException(msg);
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
                                              document.sessionExpiryForm.setAttribute("action", "expire_sessions_ajaxprocessor.jsp");
                                              document.sessionExpiryForm.submit();
                                          }
            );
        }

    }

    function expireAllSessions() {
        CARBON.showConfirmationDialog("<fmt:message key="session.expiry.selected.webapps.prompt"/>",
                                      function() {
                                        jQuery.ajax({
                                        	type: "POST",
                                        	url: "expire_sessions_ajaxprocessor.jsp",
                                        	headers: {
                                        		Accept: "text/html"
                                        	},
                                        	data: {
                                        	    "webappFileName": "<%= webappFileName %>",
                                                "redirectPage": "webapp_info.jsp",
                                                "hostName": "<%= hostName %>",
                                                "httpPort": "<%= httpPort %>"
                                        	},
                                        	async: false,
                                        	success: function (responseText, status, XMLHttpRequest) {
                                        		if (status == "success") {
                                        			eval(jQuery(responseText).text());
                                        		}
                                        	}
                                        });
                                      }
                );
    }

    function reloadWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="reload.selected.webapps.prompt"/>",
                                      function() {
                                        jQuery.ajax({
                                        	type: "POST",
                                        	url: "reload_webapps_ajaxprocessor.jsp",
                                        	headers: {
                                        		Accept: "text/html"
                                        	},
                                        	data: {
                                        	    "webappFileName": "<%= webappFileName %>",
                                                "redirectPage": "webapp_info.jsp",
                                                "hostName": "<%= hostName %>",
                                                "httpPort": "<%= httpPort %>"
                                        	},
                                        	async: false,
                                        	success: function (responseText, status, XMLHttpRequest) {
                                        		if (status == "success") {
                                        			eval(jQuery(responseText).text());
                                        		}
                                        	}
                                        });
                                      }
                );
    }

    function stopWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="stop.selected.webapps.prompt"/>",
                                      function() {
                                        jQuery.ajax({
                                        	type: "POST",
                                        	url: "stop_webapps_ajaxprocessor.jsp",
                                        	headers: {
                                        		Accept: "text/html"
                                        	},
                                        	data: {
                                        	    "webappFileName": "<%= webappFileName %>",
                                                "redirectPage": "webapp_info.jsp",
                                                "hostName": "<%= hostName %>",
                                                "httpPort": "<%= httpPort %>"
                                        	},
                                        	async: false,
                                        	success: function (responseText, status, XMLHttpRequest) {
                                        		if (status == "success") {
                                        			eval(jQuery(responseText).text());
                                        		}
                                        	}
                                        });
                                      }
                );
    }

    function startWebapp() {
        CARBON.showConfirmationDialog("<fmt:message key="start.selected.webapps.prompt"/>",
                                      function() {
                                          jQuery.ajax({
                                          	type: "POST",
                                          	url: "start_webapps_ajaxprocessor.jsp",
                                          	headers: {
                                          	    Accept: "text/html"
                                          	},
                                          	data: {
                                          	    "webappFileName": "<%= webappFileName %>",
                                          	    "redirectPage": "webapp_info.jsp",
                                          	    "hostName": "<%= hostName %>",
                                          	    "httpPort": "<%= httpPort %>"
                                          	},
                                          	async: false,
                                          	success: function (responseText, status, XMLHttpRequest) {
                                          		if (status == "success") {
                                          			eval(jQuery(responseText).text());
                                          		}
                                          	}
                                          });
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
                            <td width="30%"><fmt:message key="webapp.context"/></td>
                            <td>
                                <a href="<%= urlPrefix + "/" + webapp.getContext() %>" target="_blank">
                                    <%=webapp.getContext()%>
                                </a>
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
                            <td><fmt:message key="last.modified.time"/></td>
                            <td><%= new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss").format(new Date(webapp.getLastModifiedTime()))%>
                            </td>
                        </tr>
                    </table>
                </td>

                <td width="10px">&nbsp;</td>

                <td>
                    <div id="sessionStatsDiv">
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
                                    <a href="sessions.jsp?webappFileName=<%= webapp.getWebappFile() %>">
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
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td width="50%">
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
                                        <input type="hidden" name="webappFileName"
                                               value="<%= webappFileName%>"/>
                                        <input type="hidden" name="redirectPage"
                                               value="webapp_info.jsp"/>
                                        <input type="hidden" name="hostName"
                                               value="<%= hostName %>"/>
                                        <input type="hidden" name="httpPort"
                                               value="<%= httpPort %>"/>
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
                    </table>
                </td>
                <td width="10px">&nbsp;</td>
                <td>&nbsp;</td>
            </tr>
        </table>

        <script type="text/javascript">
            alternateTableRows('webappTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('sessionStatsTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('operationsTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <%
            }
        %>
    </div>
</div>
</fmt:bundle>
