/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.javaee.tomee;

import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService;

/**
 * This class is to keep the WebappConfigService which provides an interface to
 * org.wso2.carbon.webapp.mgt.config.WebappConfigDataHolder.
 */
public class DataHolder {
    private static WebAppConfigurationService webAppConfigurationService;

    /**
     * This method is to retrieve the WebAppConfigurationService in the DataHolder.
     */
    public static WebAppConfigurationService getWebAppConfigurationService() {
        return webAppConfigurationService;
    }

    /**
     * This method is to set WebAppConfigurationService in the DataHolder.
     *
     * @param webAppConfigurationService WebAppConfigurationService instance to be set
     */
    public static void setWebAppConfigurationService(WebAppConfigurationService webAppConfigurationService) {
        DataHolder.webAppConfigurationService = webAppConfigurationService;
    }

}
