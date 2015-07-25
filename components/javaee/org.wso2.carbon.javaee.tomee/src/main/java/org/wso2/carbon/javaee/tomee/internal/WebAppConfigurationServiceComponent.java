/*
* Copyright 2015 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.javaee.tomee.internal;

import org.wso2.carbon.javaee.tomee.DataHolder;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService;

/**
 * @scr.component name="org.wso2.carbon.javaee.tomee.internal.WebappConfigServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.webapp.mgt.config.WebAppConfigurationDataHolder"
 * interface="org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService"
 * cardinality="1..1"
 * bind="setWebAppConfigurationService"
 * unbind="unsetWebAppConfigurationService"
 */
public class WebAppConfigurationServiceComponent {

    /**
     * Sets the WebAppConfigDataHolder instance which implements the interface
     * WebappConfigService in the DataHolder
     *
     * @param webAppConfigDataHolder The WebAppConfigDataHolder instance which holds
     *                            configuration information
     */
    protected void setWebAppConfigurationService(WebAppConfigurationService webAppConfigDataHolder) {
        DataHolder.setWebAppConfigurationService(webAppConfigDataHolder);
    }

    /**
     * Removes the WebAppConfigDataHolder instance which implements the interface
     * WebappConfigService from the DataHolder
     *
     * @param webAppConfigDataHolder The WebAppConfigDataHolder instance which holds
     *                            configuration information
     */
    protected void unsetWebAppConfigurationService(WebAppConfigurationService webAppConfigDataHolder) {
        DataHolder.setWebAppConfigurationService(null);
    }
}
