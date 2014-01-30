/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.mgt.multitenancy;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;


public class GhostWebappMetaArtifactsLoader implements GhostMetaArtifactsLoader {
    private static Log log = LogFactory.getLog(GhostWebappMetaArtifactsLoader.class);


    public void loadArtifacts(AxisConfiguration axisConfiguration, String tenantDomain) {
        ConfigurationContext mainConfigCtx = DataHolder.getServerConfigContext();
        if (mainConfigCtx == null) {
            return;
        }
        ConfigurationContext tenantCC = TenantAxisUtils.
                getTenantConfigurationContexts(mainConfigCtx).get(tenantDomain);

        if (tenantCC != null) {
            try {
                GhostWebappDeployerUtils.deployGhostArtifacts(tenantCC);
            } catch (Exception e) {
                log.error("Webapps ghost meta artifact loading failed for tenant : " + tenantDomain);
            }
        }
    }
}
