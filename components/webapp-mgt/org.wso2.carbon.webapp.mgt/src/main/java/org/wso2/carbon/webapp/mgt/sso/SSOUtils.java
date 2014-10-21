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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.DataHolder;

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

    public static String generateConsumerUrl(String contextPath) {
        return getSsoSPConfig().getProperty(WebappSSOConstants.APP_SERVER_URL) + contextPath +
                getSsoSPConfig().getProperty(WebappSSOConstants.CONSUMER_URL_POSTFIX);
    }

    private static Properties getSsoSPConfig() {
        return DataHolder.getSsoSPConfig();
    }

    public static String getSAMLSSOURLforApp(String requestURI) {
        return getSsoSPConfig().getProperty(WebappSSOConstants.APP_SERVER_URL) + requestURI +
                getSsoSPConfig().getProperty(WebappSSOConstants.SAMLSSOURL);
    }
}
