/*
 * Copyright  The Apache Software Foundation.
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
package org.wso2.carbon.webapp.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.webapp.mgt.internal.WebappDeploymentServiceComponent", 
         immediate = true)
public class WebappDeploymentServiceComponent {

    private static final Log log = LogFactory.getLog(WebappDeploymentServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.info("Activating Webapp Deployment Service Component");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Webapp Deployment Service Component");
        }
    }

    @Reference(
             name = "carbon.tomcat.service", 
             service = org.wso2.carbon.tomcat.api.CarbonTomcatService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetCarbonTomcatService")
    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(null);
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        DataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }
}

