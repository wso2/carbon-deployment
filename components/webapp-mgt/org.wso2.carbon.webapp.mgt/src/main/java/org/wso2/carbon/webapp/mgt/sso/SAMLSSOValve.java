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

package org.wso2.carbon.webapp.mgt.sso;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentSessionBean;
import org.wso2.carbon.identity.sso.agent.oauth2.SAML2GrantAccessTokenRequestor;
import org.wso2.carbon.identity.sso.agent.saml.SAML2SSOManager;
import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConstants;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.mgt.DataHolder;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

public class SAMLSSOValve extends CarbonTomcatValve {

    private static Log log = LogFactory.getLog(SAMLSSOValve.class);

    private Map<String, SAML2SSOManager> samlManagerMap = new HashMap<String, SAML2SSOManager>();

    public SAMLSSOValve() {
    }

    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {

        //Is enable SSO Valve defined in context param?
        if (!Boolean.parseBoolean(request.getContext().findParameter(WebappSSOConstants.ENABLE_SAML2_SSO))) {
            getNext().invoke(request, response, compositeValve);
            return;
        }

        SAML2SSOManager samlSSOManager = null;
        SSOAgentConfigs ssoAgentConfigs = null;

        if (samlManagerMap.containsKey(request.getContextPath())) {
            samlSSOManager = samlManagerMap.get(request.getContextPath());
            ssoAgentConfigs = samlSSOManager.getConfigs();
        } else {
            try {
                /*//create a new SAML2SSOManager using the configs defined in the properties file,
				//refered to by the "saml2.config.file.path" defined in the web.xml
				Properties properties = new Properties();
	        	properties.load(new FileInputStream(request.getContext().findParameter("saml2.config.file.path")));
	        	ssoConfigs = new SSOAgentConfigs(properties);*/

                Properties ssoSPConfigs = DataHolder.getSsoSPConfig();
                ssoAgentConfigs = new SSOAgentConfigs();
                ssoAgentConfigs.initConfig(ssoSPConfigs);
                ssoAgentConfigs.setIssuerId(SSOUtils.generateIssuerID(request.getContextPath()));
                ssoAgentConfigs.setConsumerUrl(SSOUtils.generateConsumerUrl(request.getContextPath()));
                ssoAgentConfigs.initCheck();
                samlSSOManager = new SAML2SSOManager(ssoAgentConfigs);
                samlManagerMap.put(request.getContextPath(), samlSSOManager);
            } catch (Exception e) {
                log.error("Error on initializing SAML2SSOManager", e);
                return;
            }
        }

        // This should be SLO SAML Request from IdP
        String samlRequest = request.getParameter(SSOAgentConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ);

        // This could be either SAML Response for a SSO SAML Request by the client application or
        // a SAML Response for a SLO SAML Request from a SP
        String samlResponse = request.getParameter(SSOAgentConstants.HTTP_POST_PARAM_SAML2_RESP);

        String openid_mode = request.getParameter(SSOAgentConstants.OPENID_MODE);

        String claimed_id = request.getParameter(ssoAgentConfigs.getClaimedIdParameterName());

        try {
            if (ssoAgentConfigs.isSAMLSSOLoginEnabled() && samlRequest != null) {
                samlSSOManager.doSLO(request);
            } else if (ssoAgentConfigs.isSAMLSSOLoginEnabled() && samlResponse != null) {
                try {
                    samlSSOManager.processResponse(request);

                    if (((SSOAgentSessionBean) request.getSession().getAttribute(
                            ssoAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean().getSubjectId() != null) {

                        String relayState = URLDecoder.decode(request.getParameter("RelayState"), "UTF-8");
                        if (relayState != null) {
                            response.sendRedirect(relayState);
                        } else {
                            response.sendRedirect(request.getContext().getPath());
                        }
                        return;
                    }

                } catch (SSOAgentException e) {
                    if (request.getSession(false) != null) {
                        request.getSession(false).removeAttribute(ssoAgentConfigs.getSessionBeanName());
                    }
                    throw e;
                }
            } else if (ssoAgentConfigs.isSAMLSSOLoginEnabled() && ssoAgentConfigs.isSLOEnabled() &&
                    request.getRequestURI().endsWith(ssoAgentConfigs.getLogoutUrl())) {
                if (request.getSession(false) != null) {
                    response.sendRedirect(samlSSOManager.buildRequest(request, true, false));
                    return;
                }
            } else if (ssoAgentConfigs.isSAMLSSOLoginEnabled() &&
                    request.getRequestURI().endsWith(ssoAgentConfigs.getSAMLSSOUrl())) {
                response.sendRedirect(samlSSOManager.buildRequest(request, false, false));
                return;
            } else if ((ssoAgentConfigs.isSAMLSSOLoginEnabled() || ssoAgentConfigs.isOpenIDLoginEnabled()) &&
                    !request.getRequestURI().endsWith(ssoAgentConfigs.getLoginUrl()) &&
                    (request.getSession(false) == null ||
                            request.getSession(false).getAttribute(ssoAgentConfigs.getSessionBeanName()) == null)) {
//                response.sendRedirect(SSOUtils.getSAMLSSOURLforApp(request.getRequestURI()));
                response.sendRedirect(samlSSOManager.buildRequest(request, false, false));
                return;
            } else if (ssoAgentConfigs.isSAMLSSOLoginEnabled() && ssoAgentConfigs.isSAML2GrantEnabled() &&
                    request.getRequestURI().endsWith(ssoAgentConfigs.getSAML2GrantUrl()) &&
                    request.getSession(false) != null &&
                    request.getSession(false).getAttribute(ssoAgentConfigs.getSessionBeanName()) != null &&
                    ((SSOAgentSessionBean) request.getSession().getAttribute(
                            ssoAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean() != null &&
                    ((SSOAgentSessionBean) request.getSession(false).getAttribute(
                            ssoAgentConfigs.getSessionBeanName())).getSAMLSSOSessionBean().getSAMLAssertion() != null) {
                SAML2GrantAccessTokenRequestor.getAccessToken(request, ssoAgentConfigs);
            }
        } catch (IOException e) {
            log.error("Error while redirecting the response path", e);
        } catch (SSOAgentException e) {
            log.error(e.getMessage(), e);
        }
       /* // This should be SLO SAML Request from IdP
        String samlRequest = request.getParameter(SSOAgentConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ);

        // This could be either SAML Response for a SSO SAML Request by the client application or
        // a SAML Response for a SLO SAML Request from a SP
        String samlResponse = request.getParameter(SSOAgentConstants.HTTP_POST_PARAM_SAML2_RESP);

        try {
            if (samlRequest != null) {
                samlSSOManager.doSLO(request);
            } else if (samlResponse != null) {
                samlSSOManager.processResponse(request);

                if (((SSOAgentSessionBean)request.getSession().getAttribute(ssoConfigs.getSessionBeanName())).getSAMLSSOSessionBean().getSubjectId()) {


                    String relayState = URLDecoder.decode(request.getParameter("RelayState"), "UTF-8");

                    if (relayState != null && !relayState.equals(request.getContextPath() + "/" + ssoConfigs.getLoginAction())) {
                        response.sendRedirect(relayState);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/" + ssoConfigs.getHomePage());
                    }
                    return;
                } else {
                    //send to logout page
                    response.sendRedirect(request.getContextPath() + "/" + ssoConfigs.getLogoutPage());
                    return;
                }
            } else if (request.getRequestURI().endsWith(ssoConfigs.getLogoutAction())) {
                if (request.getSession(false) != null) {
                    response.sendRedirect(samlSSOManager.buildRequest(request, true, false));
                    return;
                }
            } else if (request.getSession().getAttribute(ssoConfigs.getSubjectIdSessionAttributeName()) == null
                    && !request.getRequestURI().endsWith(ssoConfigs.getLogoutPage())) {
                response.sendRedirect(samlSSOManager.buildRequest(request, false, false));
                return;
            }

            String principalName = (String) request.getSession().getAttribute(ssoConfigs.getSubjectIdSessionAttributeName());

            if (principalName != null) {

                List<String> rolesList = null;
                Map<String, String> attrMap = (HashMap) request.getSession(false).getAttribute(ssoConfigs.getSamlSSOAttributesMapName());

                if (attrMap != null && !attrMap.isEmpty()) {
                    String roles = attrMap.get("http://wso2.org/claims/role");

                    if (roles != null && !roles.isEmpty()) {
                        String[] rolesArr = roles.split(",");
                        rolesList = Arrays.asList(rolesArr);
                    }
                }

                request.setUserPrincipal(new GenericPrincipal(principalName, null, rolesList));
            }
        } catch (SSOAgentException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }*/

        getNext().invoke(request, response, compositeValve);
    }
}
