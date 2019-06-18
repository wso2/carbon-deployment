/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.discovery.cxf.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.discovery.cxf", 
         immediate = true)
public class CxfDiscoveryServiceComponent {

    private static final Log log = LogFactory.getLog(CxfDiscoveryServiceComponent.class);

    private ServiceRegistration observerServiceRegistration;

    private CxfDiscoveryDataHolder dataHolder = CxfDiscoveryDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.info("Activating CXF WS-Discovery Startup Publisher Component");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating CXF WS-Discovery component");
        }
        if (observerServiceRegistration != null) {
            observerServiceRegistration.unregister();
            observerServiceRegistration = null;
        }
    }

    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(contextService.getServerConfigContext());
        dataHolder.setClientConfigurationContext(contextService.getClientConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        dataHolder.setMainServerConfigContext(null);
        dataHolder.setClientConfigurationContext(null);
    }

    @Reference(
             name = "server.configuration.service", 
             service = org.wso2.carbon.base.api.ServerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfiguration) {
        dataHolder.setServerConfigurationService(serverConfiguration);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfiguration) {
        dataHolder.setServerConfigurationService(null);
    }
}

