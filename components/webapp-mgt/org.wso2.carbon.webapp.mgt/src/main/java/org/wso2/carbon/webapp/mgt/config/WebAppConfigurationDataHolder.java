/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the web application configuration information for each web app.
 */
public class WebAppConfigurationDataHolder implements WebAppConfigurationService {
    private static final Log log = LogFactory.getLog(WebAppConfigurationDataHolder.class.getName());
    /**
     * Holds configuration information for all existing web applications
     * The key is the path to web app folder
     */
    private Map<String, WebAppConfigurationData> configDataMap;

    public WebAppConfigurationDataHolder() {
        configDataMap = new HashMap<>();
    }

    public void addConfiguration(String ID, WebAppConfigurationData configData) {
        if (configData != null) {
            ID = dropWarExtension(ID);
            configDataMap.put(ID, configData);
        }
    }

    public void removeConfiguration(String ID) {
        ID = dropWarExtension(ID);
        if (configDataMap.containsKey(ID)) {
            configDataMap.remove(ID);
            log.info("Configuration data for " + ID + " was removed");
        } else {
            log.warn("There is no configuration data available for " + ID);
        }
    }

    public WebAppConfigurationData getConfiguration(String ID) {
        ID = dropWarExtension(ID);
        if (configDataMap.containsKey(ID)) {
            return configDataMap.get(ID);
        } else {
            return null;
        }
    }

    private String dropWarExtension(String ID) {
        if (ID.endsWith(".war")) {
            ID = ID.split(".war")[0];
        }
        return ID;
    }
}
