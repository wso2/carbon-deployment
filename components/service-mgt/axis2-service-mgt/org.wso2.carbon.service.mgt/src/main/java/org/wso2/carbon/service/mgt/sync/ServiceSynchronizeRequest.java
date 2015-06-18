/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.service.mgt.sync;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.service.mgt.ServiceConstants.ServiceOperationType;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.UUID;

public class ServiceSynchronizeRequest extends ClusteringMessage {
    private transient static final Log log = LogFactory.getLog(ServiceSynchronizeRequest.class);
    private int tenantId;
    private String tenantDomain;
    private UUID messageId;
    private ServiceOperationType operation;
    private String serviceName;

    public ServiceSynchronizeRequest(int tenantId, String tenantDomain, UUID messageId, ServiceOperationType operation, String serviceName) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = messageId;
        this.operation = operation;
        this.serviceName = serviceName;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Received [" + this + "] ");
        }

        // Run only if the tenant is loaded
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID ||
                TenantAxisUtils.getTenantConfigurationContexts(configurationContext).get(tenantDomain) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Going to synchronize Service status for tenant: TID - " + tenantId + " TD - " + tenantDomain);
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(tenantId);
                privilegedCarbonContext.setTenantDomain(tenantDomain);

                ConfigurationContext tenantConfigurationContext;
                if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                    tenantConfigurationContext = configurationContext;
                } else {
                    tenantConfigurationContext = TenantAxisUtils.getTenantConfigurationContexts(configurationContext).get(tenantDomain);
                }

                switch (operation) {
                    case ACTIVATE:
                        activateService(tenantConfigurationContext, serviceName);
                        break;
                    case DEACTIVATE:
                        deactivateService(tenantConfigurationContext, serviceName);
                        break;
                    default:
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Tenant is not loaded. TID - " + tenantId + " TD - " + tenantDomain);
            }
        }
    }

    private void activateService(ConfigurationContext configurationContext, String serviceName) {
        try {
            configurationContext.getAxisConfiguration().startService(serviceName);
            if (log.isDebugEnabled()) {
                log.info("Service activated [" + serviceName + "]");
            }
        } catch (AxisFault axisFault) {
            String msg = "Cannot activate service [" + serviceName + "]";
            log.error(msg, axisFault);
        }
    }

    private void deactivateService(ConfigurationContext configurationContext, String serviceName) {
        try {
            configurationContext.getAxisConfiguration().stopService(serviceName);
            if (log.isDebugEnabled()) {
                log.info("Service deactivated [" + serviceName + "]");
            }
        } catch (AxisFault axisFault) {
            String msg = "Cannot deactivate service [" + serviceName + "]";
            log.error(msg, axisFault);
        }
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public String toString() {
        return "ServiceSynchronizeRequest{" +
                "tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", messageId=" + messageId +
                ", operation=" + operation +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
