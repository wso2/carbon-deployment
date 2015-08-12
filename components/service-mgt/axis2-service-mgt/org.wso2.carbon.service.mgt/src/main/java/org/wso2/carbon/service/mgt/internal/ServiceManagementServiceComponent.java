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
package org.wso2.carbon.service.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ArtifactUnloader;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.multitenancy.ServiceUnloader;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.MBeanRegistrar;

/**
 * @scr.component name="org.wso2.carbon.service.mgt.ServiceManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContext"
 * unbind="unsetConfigurationContext"
 * @scr.reference name="org.wso2.carbon.base.serverConfig"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"
 * bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 * @scr.reference name="application.manager"
 * interface="org.wso2.carbon.application.deployer.service.ApplicationManagerService"
 * cardinality="0..1" policy="dynamic" bind="setAppManager" unbind="unsetAppManager"
 */
public class ServiceManagementServiceComponent {

    private static Log log = LogFactory.getLog(ServiceManagementServiceComponent.class);

    private ConfigurationContext configCtx;
    private ServiceAdmin serviceAdmin;
    private static ApplicationManagerService applicationManager;

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            // registering ServiceUnloader as an OSGi service
            ServiceUnloader serviceUnloader = new ServiceUnloader();
            bundleContext.registerService(ArtifactUnloader.class.getName(), serviceUnloader, null);
            if (serviceAdmin != null) {
                serviceAdmin.setConfigurationContext(configCtx);
            }
            try {
                // Registering ServiceAdmin as an OSGi service
                ServiceAdmin serviceAdmin = new ServiceAdmin(configCtx.getAxisConfiguration());
                bundleContext.registerService(ServiceAdmin.class.getName(), serviceAdmin, null);
            } catch (Exception exception) {
                String msg = "An error occured while initializing ServiceAdmin as an OSGi Service";
                log.error(msg, exception);
            }
        } catch (Throwable e) {
            log.error("Failed to activate the ServiceManagementServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivated ServiceManagementServiceComponent");
        }
    }

    protected void setConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = configCtx.getServerConfigContext();
        DataHolder.setServerConfigContext(configCtx.getServerConfigContext());
    }

    protected void unsetConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = null;
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        registerMBeans(serverConfigurationService);
    }

    protected void unsetServerConfiguration(
            ServerConfigurationService serverConfigurationServiceg) {
    }

    private void registerMBeans(ServerConfigurationService serverConfigurationService) {
        try {
            serviceAdmin = new ServiceAdmin();
            if (configCtx != null) {
                serviceAdmin.setConfigurationContext(configCtx);
            }
            MBeanRegistrar.registerMBean(serviceAdmin);
        } catch (Exception e) {
            log.error("Error initializing ServiceAdmin.");
        }
    }

    protected void setAppManager(ApplicationManagerService applicationManager) {
        this.applicationManager = applicationManager;
        DataHolder.setApplicationManager(applicationManager);
    }

    protected void unsetAppManager(ApplicationManagerService applicationManager) {
        this.applicationManager = null;
    }
}
