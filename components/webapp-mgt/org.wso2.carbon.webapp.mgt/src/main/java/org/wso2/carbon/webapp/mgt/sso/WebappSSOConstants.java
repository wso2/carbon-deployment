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
    public static String SSO_SP_CONFIG_PATH = CarbonUtils.getCarbonSecurityConfigDirPath() + File.separator + SSO_CONFIG_FILE_NAME;


    //SSO SP config property names
    public static String APP_SERVER_URL = "ApplicationServerURL";

    public static String CONSUMER_URL_POSTFIX = "SAML.ConsumerUrlPostFix";

/*    public static String IDP_URL = "IdPUrl";

//    public static String IDENTITY_SERVER_URL = "IdentityServerURL";

//    public static String USERNAME = "username";
//    public static String PASSWORD = "password";

    public static String ENABLE_SSO = "EnableSLO";

    public static String ENABLE_RESPONSE_SIGNING = "EnableResponseSigning";

    public static String ENABLE_ASSERTION_SIGNING = "EnableAssertionSigning";

    public static String ENABLE_REQUEST_SIGNING = "EnableRequestSigning";

    public static String SSO_AGENT_CREDENTIALS_IMPL_CLASS = "SSOAgentCredentialImplClass";

    public static String KEYSTORE = "KeyStore";

    public static String KEYSTORE_PASSORD = "KeyStorePassword";

    public static String IDP_CERT_ALIAS = "IdPCertAlias";

    public static String PRIVATE_KEY_ALIAS = "PrivateKeyAlias";

    public static String PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";

    public static String SUBJECT_ID_ASSERTION_ATTRIBUTE_NAME = "SubjectIDSessionAttributeName";

    public static String ATTRIBUTES_MAP_NAME = "AttributesMapName";

    public static String LOGIN_ACTION = "LoginAction";

    public static String LOGOUT_ACTION = "LogoutAction";

    public static String HOME_PAGE = "HomePage";

    public static String LOGOUT_PAGE = "LogoutPage";*/
}
