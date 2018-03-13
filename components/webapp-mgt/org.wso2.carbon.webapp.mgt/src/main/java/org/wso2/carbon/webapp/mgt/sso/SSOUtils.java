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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Properties;

public class SSOUtils {

    private static Log log = LogFactory.getLog(SSOUtils.class);



    public static boolean isSSOSPConfigExists() {
        File configFile = new File(WebappSSOConstants.SSO_SP_CONFIG_PATH);
        return configFile.exists();
    }

    public static String generateIssuerID(String contextPath) {
        String issuerId = contextPath.replaceFirst("/webapps", "").replace("/", "_");
        if (issuerId.startsWith("_")) {
            issuerId = issuerId.substring(1);
        }
        return issuerId;
    }

    public static String getSAMLSSOURLforApp(String requestURI, Properties ssoSPConfigProperties) {
        return ssoSPConfigProperties.getProperty(WebappSSOConstants.APP_SERVER_URL) + requestURI +
                ssoSPConfigProperties.getProperty(WebappSSOConstants.SAMLSSOURL);
    }

    /**
     * By default this method generates the ACS url based on the application server hostname + tenant.suffix. But, in
     * case the request contains a custom HTTP header containing a different ACS url, this method will honor that url.
     * (This kind of a scenario can happen when the end user is hoping to access a web application with a custom
     * domain and URI through a router like Nginx.)
     * @param request request object
     * @param ssoSPConfigProperties SSO properties loaded from server level sso-sp-config.properties file.
     * @return appropriate ACS url
     */
    static String generateConsumerUrl(Request request, Properties ssoSPConfigProperties) {

        String assertionConsumerURL = request.getHeader(ssoSPConfigProperties.getProperty(WebappSSOConstants
                .CUSTOM_ACS_HEADER));

        if (StringUtils.isBlank(assertionConsumerURL)) {
            assertionConsumerURL = ssoSPConfigProperties.getProperty(WebappSSOConstants.APP_SERVER_URL) + request.getContextPath() +
                    ssoSPConfigProperties.getProperty(WebappSSOConstants.CONSUMER_URL_POSTFIX);
        }

        if (log.isDebugEnabled()) {
            log.debug("Setting ACS_URL to : " + assertionConsumerURL);
        }

        return assertionConsumerURL;
    }

    /**
     * In case an end user wishes to access a web application from a custom domain without any tenant awareness by
     * fronting the application server with a router like Nginx, we need to detect such a scenario and remove the
     * tenant component from all redirect URIs during the SSO flow.
     * @param uri original URI path
     * @param customACSUrl a custom HTTP header containing a different ACS url
     * @param tenantName Name of tenant that owns the web application.
     * @return
     */
    static String removeTenantFromURI(String uri, String customACSUrl, String tenantName) {

        String returnURI = uri;

        if (!StringUtils.isBlank(customACSUrl) && !StringUtils.isBlank(tenantName) && !customACSUrl.contains(tenantName)) {
            returnURI = returnURI.replace(WebappSSOConstants.TENANT_URL_PREFIX + tenantName +
                    WebappSSOConstants.WEBAPP_PREFIX,"");
        }

        return returnURI;
    }
}
