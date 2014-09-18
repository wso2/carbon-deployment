/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.as.monitoring.collector.http;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.publisher.http.HttpStatPublisher;
import org.wso2.carbon.as.monitoring.publisher.http.WebappMonitoringEvent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import ua_parser.CachingParser;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the starting class for the web app statistics. This class is initiated through tomcat ValveBase. Class has to be added
 * to <WSO2 Application Server home>/repository/conf/tomcat/catalina-server.xml  as following
 * <Valve className="org.wso2.carbon.monitoring.http.WebAppMonitoringPublisherValve"/>
 * Purpose of this class is to initiate the publishing web application statistics. Web applications statistic will be measured in per server, per tenant
 * and per web application.
 */
public class WebAppMonitoringPublisherValve extends ValveBase {

    public static final String BACKSLASH = "/";
    public static final String WEBAPP = "webapp";
    private static final Log LOG = LogFactory.getLog(WebAppMonitoringPublisherValve.class);
    private volatile HttpStatPublisher publisher;
    private Parser uaParser = null;
    private Pattern pattern;

    public WebAppMonitoringPublisherValve() {

        super(true);
        try {
            uaParser = new CachingParser();
        } catch (IOException e) {
            // We should continue though we have a loss here.
            LOG.error("Internal error in the User-Agent header parser. Some of the fields may not be included in the BAM Data Stream", e);
        }

        try {
            publisher = new HttpStatPublisher();
        } catch (BAMPublisherConfigurationException e) {
            LOG.error("HTTP call monitoring will be disabled. due to the error occurred.", e);
        }
        LOG.debug("The WebAppMonitoringPublisherValve initialized.");
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        // Get the requested url from the request to check what it consist of  and to check weather this web app has enable statistic monitoring
        Long startTime = System.currentTimeMillis();

        /*
        * Invoke the next valve. For our valve being the last configured in catalina-server.xml this will trigger the web application context requested.
        * After the completion of serving the request, response will return to here.
        */
        getNext().invoke(request, response);

        // This start time is to capture the request initiated time to measure the response time.
        long responseTime = System.currentTimeMillis() - startTime;

        // No point of gathering data if publisher is not in a publishable state
        if (publisher == null) {
            LOG.debug("HTTP call monitoring event is not processed due to bad publisher configuration.");
            return;
        }

        if (!publisher.isPublishable()) {
            return;
        }

        String requestURI = request.getRequestURI();
        if (isExcludable(requestURI)) {
            return;
        }


        try {

            //Extracting the data from request and response and setting them to bean class
            WebappMonitoringEvent webappMonitoringEvent = prepareWebappMonitoringEventData(request, response, responseTime);

            //Time stamp of request initiated in the class
            webappMonitoringEvent.setTimestamp(startTime);

            if (LOG.isDebugEnabled()) {
                LOG.debug("publishing the HTTP Stat : " + webappMonitoringEvent);
            }

            publisher.publish(webappMonitoringEvent);
        } catch (Exception e) {
            // A monitoring exception is not a blocker to the main call. We only loose the monitoring
            // event here. So log it and that's it.
            LOG.error("Failed to publish web app stat events to BAM : " + e.getMessage(), e);
        }

    }

    /**
     * The monitoring event will not be generated If a part of the request url matches this regex
     *
     * @param urlExclusionRegex the regex that marks the exclusion pattern
     */
    public void setUrlExclusionRegex(String urlExclusionRegex) {
        pattern = Pattern.compile(urlExclusionRegex);
    }

    private boolean isExcludable(String url) {
        if (pattern != null) {
            Matcher matcher = pattern.matcher(url);
            return matcher.find();
        }

        // No exclusion pattern set.
        // No exclusions so.
        return true;
    }

    /*
     * This method set the statics data to webappMonitoringEvent.
     */
    private WebappMonitoringEvent prepareWebappMonitoringEventData(Request request,
                                                                   Response response,
                                                                   long responseTime) {

        WebappMonitoringEvent webappMonitoringEvent = new WebappMonitoringEvent();

        String requestedURI = request.getRequestURI();

        /*
        * Checks requested url null
        */
        if (requestedURI != null) {

            requestedURI = requestedURI.trim();
            String[] requestedUriParts = requestedURI.split(BACKSLASH);

           /*
            * If url start with /t/, the request comes to a tenant web app
            */
            if (requestedURI.startsWith("/t/")) {
                if (requestedUriParts.length >= 4) {
                    webappMonitoringEvent.setWebappName(requestedUriParts[4]);
                    webappMonitoringEvent.setWebappOwnerTenant(requestedUriParts[2]);
                }
            } else {
                webappMonitoringEvent.setWebappOwnerTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                if (!BACKSLASH.equals(requestedURI)) {
                    webappMonitoringEvent.setWebappName(requestedUriParts[1]);
                } else {
                    webappMonitoringEvent.setWebappName(BACKSLASH);
                }
            }

            String webappServletVersion = request.getContext().getEffectiveMajorVersion() + "." +
                                          request.getContext().getEffectiveMinorVersion();
            webappMonitoringEvent.setWebappVersion(webappServletVersion);

            String consumerName = extractUsername(request);
            webappMonitoringEvent.setUserId(consumerName);

            int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String consumerTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            webappMonitoringEvent.setUserTenant(consumerTenantDomain);
            webappMonitoringEvent.setResourcePath(request.getPathInfo());

            webappMonitoringEvent.setHttpMethod(request.getMethod());
            webappMonitoringEvent.setContentType(request.getContentType());
            webappMonitoringEvent.setResponseContentType(response.getContentType());
            webappMonitoringEvent.setResponseHttpStatusCode(response.getStatus());
            webappMonitoringEvent.setRemoteAddress(getClientIpAddress(request));
            webappMonitoringEvent.setReferrer(request.getHeader(WebappMonitoringPublisherConstants.REFERRER));
            webappMonitoringEvent.setRemoteUser(request.getRemoteUser());
            webappMonitoringEvent.setAuthType(request.getAuthType());
            webappMonitoringEvent.setCountry("-");
            webappMonitoringEvent.setResponseTime(responseTime);
            webappMonitoringEvent.setLanguage(request.getLocale().getLanguage());
            webappMonitoringEvent.setCountry(request.getLocale().getCountry());

            webappMonitoringEvent.setSessionId(extractSessionId(request));

            webappMonitoringEvent.setWebappDisplayName(request.getContext().getDisplayName());
            webappMonitoringEvent.setWebappContext(requestedURI);
            webappMonitoringEvent.setWebappType(WEBAPP);
            webappMonitoringEvent.setServerAddress(request.getServerName());
            webappMonitoringEvent.setServerName(request.getLocalName());
            webappMonitoringEvent.setTenantId(tenantID);
            webappMonitoringEvent.setRequestSizeBytes(request.getContentLength());
            webappMonitoringEvent.setResponseSizeBytes(response.getContentLength());
            parserUserAgent(request, webappMonitoringEvent);

        }
        return webappMonitoringEvent;
    }

    private String extractSessionId(Request request) {
        final HttpSession session = request.getSession(false);

        // CXF web services does not have a session id, because they are stateless
        return (session != null && session.getId() != null) ? session.getId() : "-";
    }

    private String extractUsername(Request request) {
        String consumerName;
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consumerName = principal.getName();
        } else {
            consumerName = WebappMonitoringPublisherConstants.ANONYMOUS_USER;
        }
        return consumerName;
    }

    private void parserUserAgent(Request request, WebappMonitoringEvent webappMonitoringEvent) {
        String userAgent = request.getHeader(WebappMonitoringPublisherConstants.USER_AGENT);

        if (uaParser != null) {

            Client readableUserAgent = uaParser.parse(userAgent);

            webappMonitoringEvent.setUserAgentFamily(readableUserAgent.userAgent.family);
            webappMonitoringEvent.setUserAgentVersion(readableUserAgent.userAgent.major);
            webappMonitoringEvent.setOperatingSystem(readableUserAgent.os.family);
            webappMonitoringEvent.setOperatingSystemVersion(readableUserAgent.os.major);
            webappMonitoringEvent.setDeviceCategory(readableUserAgent.device.family);
        }
    }

    /*
    * Checks the remote address of the request. Server could be hiding behind a proxy or load balancer.
    * if we get only request.getRemoteAddr() will give only the proxy pr load balancer address.
    * For that we are checking the request forwarded address in the header of the request.
    */
    private String getClientIpAddress(Request request) {
        String ip = request.getHeader(WebappMonitoringPublisherConstants.X_FORWARDED_FOR);
        ip = tryNextHeaderIfIpNull(request, ip, WebappMonitoringPublisherConstants.PROXY_CLIENT_IP);
        ip = tryNextHeaderIfIpNull(request, ip, WebappMonitoringPublisherConstants.WL_PROXY_CLIENT_IP);
        ip = tryNextHeaderIfIpNull(request, ip, WebappMonitoringPublisherConstants.HTTP_CLIENT_IP);
        ip = tryNextHeaderIfIpNull(request, ip, WebappMonitoringPublisherConstants.HTTP_X_FORWARDED_FOR);

        if (ip == null || ip.length() == 0 || WebappMonitoringPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            // Failed. remoteAddr is the only option
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    // If the input param ip is invalid, it will return the value of the next header
    // as the output
    private String tryNextHeaderIfIpNull(Request request, String ip, String nextHeader) {
        if (ip == null || ip.length() == 0 || WebappMonitoringPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            return request.getHeader(nextHeader);
        }
        return null;
    }
}