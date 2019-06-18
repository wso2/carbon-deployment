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
package org.wso2.carbon.webapp.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.ArtifactUnloader;
import org.wso2.carbon.core.deployment.DeploymentSynchronizer;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.deployment.GhostMetaArtifactsLoader;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.GhostWebappDeployerValve;
import org.wso2.carbon.webapp.mgt.TenantLazyLoaderValve;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebContextParameter;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.multitenancy.GhostWebappMetaArtifactsLoader;
import org.wso2.carbon.webapp.mgt.multitenancy.WebappUnloader;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.webapp.mgt.internal.WebappManagementServiceComponent", 
         immediate = true)
public class WebappManagementServiceComponent {

    private static final Log log = LogFactory.getLog(WebappManagementServiceComponent.class);

    private static ApplicationManagerService applicationManager;

    @Activate
    protected void activate(ComponentContext ctx) {
        try {
            // Register the valves with Tomcat
            ArrayList<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new TenantLazyLoaderValve());
            if (GhostDeployerUtils.isGhostOn()) {
                valves.add(new GhostWebappDeployerValve());
                // registering WebappUnloader as an OSGi service
                WebappUnloader webappUnloader = new WebappUnloader();
                ctx.getBundleContext().registerService(ArtifactUnloader.class.getName(), webappUnloader, null);
                GhostWebappMetaArtifactsLoader artifactsLoader = new GhostWebappMetaArtifactsLoader();
                ctx.getBundleContext().registerService(GhostMetaArtifactsLoader.class.getName(), artifactsLoader, null);
            } else {
                setServerURLParam(DataHolder.getServerConfigContext());
            }
            // adding TenantLazyLoaderValve first in the TomcatContainer if Url mapping available
            if (DataHolder.getHotUpdateService() != null) // && TomcatValveContainer.isValveExists(new UrlMapperValve //TODO: Fix this once URLMapper component becomes available
            {
                TomcatValveContainer.addValves(WebappsConstants.VALVE_INDEX, valves);
            } else {
                TomcatValveContainer.addValves(valves);
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating WebappManagementServiceComponent", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
    // TomcatValveContainer.removeValves();
    }

    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(null);
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        // keeping the realm service in the DataHolder class
        DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
    }

    @Reference(
             name = "tenant.registryloader", 
             service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DataHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
    }

    @Reference(
             name = "depsych.service", 
             service = org.wso2.carbon.core.deployment.DeploymentSynchronizer.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetDeploymentSynchronizerService")
    protected void setDeploymentSynchronizerService(DeploymentSynchronizer depSynchService) {
        DataHolder.setDeploymentSynchronizerService(depSynchService);
    }

    protected void unsetDeploymentSynchronizerService(DeploymentSynchronizer depSynchService) {
        DataHolder.setDeploymentSynchronizerService(null);
    }

    private void setServerURLParam(ConfigurationContext configurationContext) {
        // Adding server url as a parameter to webapps servlet context init parameter
        Map<String, WebApplicationsHolder> webApplicationsHolderList = WebAppUtils.getAllWebappHolders(configurationContext);
        WebContextParameter serverUrlParam = new WebContextParameter("webServiceServerURL", CarbonUtils.getServerURL(ServerConfiguration.getInstance(), configurationContext));
        List<WebContextParameter> servletContextParameters = (ArrayList<WebContextParameter>) configurationContext.getProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST);
        if (servletContextParameters != null) {
            servletContextParameters.add(serverUrlParam);
        }
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderList.values()) {
            if (webApplicationsHolder != null) {
                for (WebApplication application : webApplicationsHolder.getStartedWebapps().values()) {
                    application.getContext().getServletContext().setAttribute(serverUrlParam.getName(), serverUrlParam.getValue());
                }
            }
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

    protected void unsetAppManager(ApplicationManagerService appManager) {
        applicationManager = null;
    }
}

