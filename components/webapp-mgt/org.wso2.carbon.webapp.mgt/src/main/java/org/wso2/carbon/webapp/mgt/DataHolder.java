/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.deployment.DeploymentSynchronizer;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Holds the some of the data required by the webapps component
 */
public class DataHolder {
    private static ConfigurationContext serverConfigContext;
    private static RealmService realmService;
    private static CarbonTomcatService carbonTomcatService;
    private static HotUpdateService hotUpdateService;
    protected static DeploymentSynchronizer deploymentSynchronizerService;

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        DataHolder.realmService = realmService;
    }

    public static void setServerConfigContext(ConfigurationContext serverConfigContext) {
        DataHolder.serverConfigContext = serverConfigContext;
    }

    public static ConfigurationContext getServerConfigContext() {
        CarbonUtils.checkSecurity();
        return serverConfigContext;
    }

    public static void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.carbonTomcatService = carbonTomcatService;
    }

    public static CarbonTomcatService getCarbonTomcatService() {
        return DataHolder.carbonTomcatService;
    }

    public static void setHotUpdateService(HotUpdateService hotUpdateService) {
        DataHolder.hotUpdateService = hotUpdateService;
    }

    public static HotUpdateService getHotUpdateService() {
        return DataHolder.hotUpdateService;
    }

    public static void setDeploymentSynchronizerService(
            DeploymentSynchronizer deploymentSynchronizerService) {
            DataHolder.deploymentSynchronizerService = deploymentSynchronizerService;
    }

    public static DeploymentSynchronizer getDeploymentSynchronizerService() {
        return DataHolder.deploymentSynchronizerService;
    }
}