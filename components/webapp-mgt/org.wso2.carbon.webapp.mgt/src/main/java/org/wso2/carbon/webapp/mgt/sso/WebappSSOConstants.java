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

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class WebappSSOConstants {

    public static final String ENABLE_SAML2_SSO = "enable.saml2.sso";

    public static final String SSO_CONFIG_FILE_NAME = "sso-sp-config.properties";
    public static final String SAMLSSOURL = "SAMLSSOUrl";
    public static final String SSO_AGENT_CONFIG = "SSOAgentConfig";
    public static final String HANDLE_CONSUMER_URL_AFTER_SLO = "handleConsumerURLAfterSLO";
    public static final String REDIRECT_PATH_AFTER_SLO = "redirectPathAfterSLO";
    protected static final String REQUEST_PARAM_MAP = "REQUEST_PARAM_MAP";
    public static String SSO_SP_CONFIG_PATH = CarbonUtils.getCarbonSecurityConfigDirPath() + File.separator + SSO_CONFIG_FILE_NAME;

    //SSO SP config property names
    public static String APP_SERVER_URL = "ApplicationServerURL";

    public static String CONSUMER_URL_POSTFIX = "SAML.ConsumerUrlPostFix";

    /**
     * Tenant name as specified in web.xml of application.
     */
    static final String ENABLE_SAML2_SSO_WITH_TENANT = "enable.saml2.sso.with.tenant";

    /**
     * URLs that need to be skipped from SSO flow, as configured in web.xml of application.
     */
    static final String SKIP_URIS = "sso.skip.uris";

    /**
     * Custom HTTP header that can override the default generated ACS url for a web application.
     */
    static final String CUSTOM_ACS_HEADER = "CustomACSHeader";

    /**
     * Tenant url prefix used by default in application server.
     */
    static final String TENANT_URL_PREFIX = "/t/";

    /**
     * Web application prefix used by default in application server.
     */
    static final String WEBAPP_PREFIX = "/webapps";
}
