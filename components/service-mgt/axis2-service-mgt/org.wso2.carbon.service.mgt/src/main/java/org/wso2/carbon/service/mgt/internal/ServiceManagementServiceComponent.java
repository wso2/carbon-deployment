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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.service.mgt.ServiceManagementServiceComponent", 
         immediate = true)
public class ServiceManagementServiceComponent {

    private static Log log = LogFactory.getLog(ServiceManagementServiceComponent.class);

    private ConfigurationContext configCtx;

    private ServiceAdmin serviceAdmin;

    private static ApplicationManagerService applicationManager;

    @Activate
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

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivated ServiceManagementServiceComponent");
        }
    }

    @Reference(
             name = "org.wso2.carbon.configCtx", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContext")
    protected void setConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = configCtx.getServerConfigContext();
        DataHolder.setServerConfigContext(configCtx.getServerConfigContext());
    }

    protected void unsetConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = null;
    }

    @Reference(
             name = "org.wso2.carbon.base.serverConfig", 
             service = org.wso2.carbon.base.api.ServerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetServerConfiguration")
    protected void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        registerMBeans(serverConfigurationService);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigurationServiceg) {
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

    @Reference(
             name = "application.manager", 
             service = org.wso2.carbon.application.deployer.service.ApplicationManagerService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAppManager")
    protected void setAppManager(ApplicationManagerService applicationManager) {
        this.applicationManager = applicationManager;
        DataHolder.setApplicationManager(applicationManager);
    }

    protected void unsetAppManager(ApplicationManagerService applicationManager) {
        this.applicationManager = null;
    }
}

