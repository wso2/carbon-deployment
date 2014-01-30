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

package org.wso2.carbon.service.mgt.multitenancy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ArtifactUnloader;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.service.mgt.internal.DataHolder;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.deployment.GhostDeployer;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The unloader used in lazy loading for unloading inactive services
 */
public class ServiceUnloader implements ArtifactUnloader {

    private static final Log log = LogFactory.getLog(ServiceUnloader.class);

    // Maximum allowed inactive time period for services. Default is set to 10 mins
    private static final long DEFAULT_MAX_INACTIVE_INTERVAL = 10;


    @Override
    public void unload() {
        ConfigurationContext mainConfigCtx = DataHolder.getServerConfigContext();
        if (mainConfigCtx == null) {
            return;
        }
        try {
            // iterate through all tenant config contexts
            Set<Map.Entry<String, ConfigurationContext>> ccEntries = TenantAxisUtils
                    .getTenantConfigurationContexts(mainConfigCtx).entrySet();
            for (Map.Entry<String, ConfigurationContext> entry : ccEntries) {
                String tenantDomain = entry.getKey();
                unloadInactiveServices(entry.getValue(), tenantDomain);
            }
            // unload from super tenant as well..
            unloadInactiveServices(mainConfigCtx, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (AxisFault axisFault) {
            log.error("Error while unloading inactive services..", axisFault);
        }
    }


    private void unloadInactiveServices(ConfigurationContext configCtx,
                                        String tenantDomain) throws AxisFault {
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        int tenantId = MultitenantUtils.getTenantId(configCtx);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                ctx.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                ctx.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            } else {
                ctx.setTenantId(tenantId);
                ctx.setTenantDomain(tenantDomain);
            }

            if (axisConfig != null) {
                // iterate through all services in the current tenant
                Collection<AxisService> services = axisConfig.getServices().values();
                for (AxisService service : services) {
                    if (isSkippedServiceType(service)) {
                        continue;
                    }
                    // get the last usage parameter from the service
                    Parameter lastUsageParam = service
                            .getParameter(CarbonConstants.SERVICE_LAST_USED_TIME);
                    if (lastUsageParam != null && isInactive((Long) lastUsageParam.getValue())) {
                        // service is inactive. now we have to unload it..
                        GhostDeployer ghostDeployer = GhostDeployerUtils.getGhostDeployer(axisConfig);
                        if (ghostDeployer != null && service.getFileName() != null) {
                            AxisServiceGroup existingSG = (AxisServiceGroup) service.getParent();
                            // remove the existing actual service
                            log.info("Unloading actual Service Group : " + existingSG
                                    .getServiceGroupName() + " and adding a Ghost Service Group. " +
                                    "Tenant Domain: " + tenantDomain);
                            // add this parameter to keep track of this service at ghost dispatcher
                            existingSG.addParameter(CarbonConstants.IS_ARTIFACT_BEING_UNLOADED, "true");
                            GhostDeployerUtils.addServiceGroupToTransitMap(existingSG, axisConfig);
                            // we can't delete the configs in the registry. so keep history..
                            existingSG.addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
                            axisConfig.removeServiceGroup(existingSG.getServiceGroupName());
                            if (log.isDebugEnabled()) {
                                log.debug("Successfully removed actual Service Group : " +
                                        existingSG.getServiceGroupName() + " Tenant Domain: " +
                                        tenantDomain);
                            }
                            // Create the Ghost service group using the file name
                            File ghostFile = GhostDeployerUtils.getGhostFile(service.getFileName()
                                    .getPath(), axisConfig);
                            AxisServiceGroup ghostServiceGroup =
                                    GhostDeployerUtils.createGhostServiceGroup(axisConfig,
                                            ghostFile, service.getFileName());
                            if (ghostServiceGroup != null) {
                                // add the ghost service
                                axisConfig.addServiceGroup(ghostServiceGroup);
                                // remove the service group from transit map
                                GhostDeployerUtils.removeServiceGroupFromTransitMap(ghostServiceGroup,
                                        axisConfig);
                                if (log.isDebugEnabled()) {
                                    log.debug("Successfully added Ghost Service Group : " +
                                            ghostServiceGroup.getServiceGroupName() + " Tenant Domain: " +
                                            tenantDomain);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private boolean isInactive(Long lastUsedTime) {
        long inactiveInterval = System.currentTimeMillis() - lastUsedTime;
        // set the default value
        long maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;
        // check the system property
        String property = System.getProperty(CarbonConstants.SERVICE_IDLE_TIME);
        if (property != null) {
            maxInactiveInterval = Long.parseLong(property);
        }
        return inactiveInterval > maxInactiveInterval * 60 * 1000;
    }

    /**
     * There are some service types that we can't unload. That is because, ghost deployer can't
     * redeploy these types later. Ex : bpel services
     *
     * @param service - AxisService instance
     * @return - true if the type of the service is a skipped one
     */
    private boolean isSkippedServiceType(AxisService service) {
        String serviceType = null;
        Parameter serviceTypeParam;
        serviceTypeParam = service.getParameter(ServerConstants.SERVICE_TYPE);
        if (serviceTypeParam != null) {
            serviceType = (String) serviceTypeParam.getValue();
        }
        // add to this check if there are more types to skip
        return serviceType != null && serviceType.equals("bpel");
    }
}
