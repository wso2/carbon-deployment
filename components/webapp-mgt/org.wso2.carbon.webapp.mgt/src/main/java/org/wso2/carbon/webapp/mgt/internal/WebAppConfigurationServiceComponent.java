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

package org.wso2.carbon.webapp.mgt.internal;

import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService;

/**
 * @scr.component name="org.wso2.carbon.webapp.mgt.internal.WebAppConfigurationServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.webapp.mgt.config.WebAppConfigurationDataHolder"
 * interface="org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService"
 * cardinality="1..1"
 * bind="setWebappConfigService"
 * unbind="unsetWebappConfigService"
 */
public class WebAppConfigurationServiceComponent {

    /**
     * Sets the WebappConfigDataHolder instance which implements the interface
     * WebappConfigService in the DataHolder
     *
     * @param webConfigDataHolder The WebappConfigDataHolder instance which holds
     *                            configuration information
     */
    protected void setWebappConfigService(WebAppConfigurationService webConfigDataHolder) {
        DataHolder.setWebAppConfigurationService(webConfigDataHolder);
    }

    /**
     * Removes the WebappConfigDataHolder instance which implements the interface
     * WebappConfigService from the DataHolder
     *
     * @param webConfigDataHolder The WebappConfigDataHolder instance which holds
     *                            configuration information
     */
    protected void unsetWebappConfigService(WebAppConfigurationService webConfigDataHolder) {
        DataHolder.setWebAppConfigurationService(null);
    }
}
