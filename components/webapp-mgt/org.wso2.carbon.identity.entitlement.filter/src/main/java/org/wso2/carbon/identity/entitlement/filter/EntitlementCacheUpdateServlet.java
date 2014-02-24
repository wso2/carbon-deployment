/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.filter;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementCacheUpdateServletException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EntitlementCacheUpdateServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(EntitlementCacheUpdateServlet.class);

    private String httpsPort;
    private ConfigurationContext configCtx;
    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceUrl;
    private String authCookie;
    private ServletConfig servletConfig;
    private String authentication;
    private String authenticationPage;
    private String authenticationPageURL;

    public void init(ServletConfig config) throws EntitlementCacheUpdateServletException {

        servletConfig = config;
        try {
            configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        } catch (AxisFault e) {
            log.error("Error while initializing Configuration Context", e);
            throw new EntitlementCacheUpdateServletException("Error while initializing Configuration Context", e);

        }
        httpsPort = config.getInitParameter(EntitlementConstants.HTTPS_PORT);
        authentication = config.getInitParameter(EntitlementConstants.AUTHENTICATION);
        remoteServiceUrl = config.getServletContext().getInitParameter(EntitlementConstants.REMOTE_SERVICE_URL);
        remoteServiceUserName = config.getServletContext().getInitParameter(EntitlementConstants.USERNAME);
        remoteServicePassword = config.getServletContext().getInitParameter(EntitlementConstants.PASSWORD);
        authenticationPage = config.getInitParameter(EntitlementConstants.AUTHENTICATION_PAGE);
        authenticationPageURL = config.getInitParameter(EntitlementConstants.AUTHENTICATION_PAGE_URL);

    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws EntitlementCacheUpdateServletException {
        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws EntitlementCacheUpdateServletException {

        if (!req.isSecure()) {
            redirectToHTTPS(req, resp);
        } else if (req.getParameter("username") != null && req.getParameter("password") != null
                   && !req.getParameter("username").equals("null") && !req.getParameter("password").equals("null")) {
            doAuthentication(req,resp);
        } else {
            if(req.getParameter("username") == null){
                log.info("\'username\' parameter not available in request. Redirecting to " + authenticationPageURL);
            }
            if(req.getParameter("password") == null){
                log.info("\'password\' parameter not available in request. Redirecting to " + authenticationPageURL);
            }
            if(req.getParameter("username") != null && req.getParameter("username").equals("null")){
                log.info("\'username\' is empty in request. Redirecting to " + authenticationPageURL);
            }
            if(req.getParameter("password") != null && req.getParameter("password").equals("null")){
                log.info("\'password\' is empty in request. Redirecting to " + authenticationPageURL);
            }
            showAuthPage(req, resp);
        }
    }

    private boolean authenticate(String userName, String password, String remoteIp)
            throws EntitlementCacheUpdateServletException {

        boolean isAuthenticated = false;

        if (authentication.equals(EntitlementConstants.WSO2_IS)) {

            AuthenticationAdminStub authStub;
            String authenticationAdminServiceURL = remoteServiceUrl +"AuthenticationAdmin";
            try {
                authStub = new AuthenticationAdminStub(configCtx, authenticationAdminServiceURL);
                ServiceClient client = authStub._getServiceClient();
                Options options = client.getOptions();
                options.setManageSession(true);
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
                isAuthenticated = authStub.login(userName, password, remoteIp);
                authCookie = (String) authStub._getServiceClient().getServiceContext()
                        .getProperty(HTTPConstants.COOKIE_STRING);
            } catch (LoginAuthenticationExceptionException e) {
                log.info(userName + " not authenticated to perform entitlement query to perform cache update");
            } catch (Exception e) {
                throw new EntitlementCacheUpdateServletException("Error while trying to authenticate" +
                                                                 " with AuthenticationAdmin", e);
            }

        } else if (authentication.equals(EntitlementConstants.WEB_APP)) {

            if (userName.equals(remoteServiceUserName) && password.equals(remoteServicePassword)) {
                isAuthenticated = true;
            }

        } else {

            throw new EntitlementCacheUpdateServletException(authentication + " is an invalid"
                  + " configuration for authentication parameter in web.xml. Valid configurations are"
                  + " \'" + EntitlementConstants.WEB_APP + "\' and \'" + EntitlementConstants.WSO2_IS + "\'");

        }
        return isAuthenticated;
    }

    private String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private void redirectToHTTPS (HttpServletRequest req, HttpServletResponse resp) throws EntitlementCacheUpdateServletException{
            String serverName = req.getServerName();
            String contextPath = req.getContextPath();
            String servletPath = req.getServletPath();
            String redirectURL = "https://" + serverName + ":" + httpsPort + contextPath
                                 + servletPath;
            try{
                resp.sendRedirect(redirectURL);
            }catch (IOException e){
                log.error("Error while redirecting request to come over HTTPS", e);
                throw new EntitlementCacheUpdateServletException("Error while redirecting request to come over HTTPS", e);
            }
    }

    private void doAuthentication(HttpServletRequest req, HttpServletResponse resp) throws EntitlementCacheUpdateServletException{
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String remoteIp = req.getServerName();

        if (authenticate(username, password, remoteIp)) {

            RequestDispatcher requestDispatcher = req.getRequestDispatcher("/updateCacheAuth.do");
            String subjectScope = servletConfig.getServletContext().getInitParameter("subjectScope");
            String subjectAttributeName = servletConfig.getServletContext().getInitParameter("subjectAttributeName");

            if (subjectScope.equals(EntitlementConstants.REQUEST_PARAM)) {

                requestDispatcher = req.getRequestDispatcher("/updateCacheAuth.do?" + subjectAttributeName + "=" + username);

            } else if (subjectScope.equals(EntitlementConstants.REQUEST_ATTIBUTE)) {

                req.setAttribute(subjectAttributeName, username);

            } else if (subjectScope.equals(EntitlementConstants.SESSION)) {

                req.getSession().setAttribute(subjectAttributeName, username);

            } else {

                resp.setHeader("Authorization", Base64Utils.encode((username + ":" + password).getBytes()));
            }

            try{
                requestDispatcher.forward(req, resp);
            }catch (Exception e){
                log.error("Error occurred while dispatching request to /updateCacheAuth.do", e);
                throw new EntitlementCacheUpdateServletException("Error occurred while dispatching request to /updateCacheAuth.do", e);
            }

        } else {
            showAuthPage(req,resp);
        }
    }

    private void showAuthPage (HttpServletRequest req, HttpServletResponse resp) throws EntitlementCacheUpdateServletException{
        if (authenticationPage.equals("default")) {

                InputStream is = getClass().getResourceAsStream("/updateCache.html");
                String updateCache = convertStreamToString(is);
                try{
                    resp.getWriter().print(updateCache);
                }catch (IOException e){
                    log.error("Error occurred while writing /updateCache.html page to OutputStream");
                    throw new EntitlementCacheUpdateServletException("Error occurred while writing"
                                                       + " /updateCache.html page to OutputStream"+e);
                }
            } else if (authenticationPage.equals("custom")) {

                try {
                    req.getRequestDispatcher(authenticationPageURL).forward(req, resp);
                } catch (Exception e) {
                    log.error("Error occurred while dispatching request to "+ authenticationPageURL, e);
                    throw new EntitlementCacheUpdateServletException("Error occurred while dispatching"
                                                        + " request to "+ authenticationPageURL, e);
                }

            } else {

                throw new EntitlementCacheUpdateServletException(authenticationPage + " is an invalid"
                                                                 + " configuration for authenticationPage parameter in web.xml. Valid"
                                                                 + " configurations are 'default' and 'custom'");

            }
    }

}
