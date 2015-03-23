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
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.ArtifactUnloader;
import org.wso2.carbon.core.deployment.DeploymentSynchronizer;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
//import org.wso2.carbon.url.mapper.UrlMapperValve;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.webapp.mgt.internal.WebappManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="depsych.service" interface="org.wso2.carbon.core.deployment.DeploymentSynchronizer"
 * cardinality="0..1" policy="dynamic"  bind="setDeploymentSynchronizerService" unbind="unsetDeploymentSynchronizerService"
 */
public class WebappManagementServiceComponent {
    private static final Log log = LogFactory.getLog(WebappManagementServiceComponent.class);

    protected void activate(ComponentContext ctx) {
        try {
            // Register the valves with Tomcat
            ArrayList<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new TenantLazyLoaderValve());
            if (GhostDeployerUtils.isGhostOn()) {
                valves.add(new GhostWebappDeployerValve());
                // registering WebappUnloader as an OSGi service
                WebappUnloader webappUnloader = new WebappUnloader();
                ctx.getBundleContext().registerService(ArtifactUnloader.class.getName(),
                                                       webappUnloader, null);
                GhostWebappMetaArtifactsLoader artifactsLoader = new GhostWebappMetaArtifactsLoader();
                ctx.getBundleContext().registerService(GhostMetaArtifactsLoader.class.getName(),
                                                       artifactsLoader, null);

            } else {
                setServerURLParam(DataHolder.getServerConfigContext());
            }
            //adding TenantLazyLoaderValve first in the TomcatContainer if Url mapping available
            if (DataHolder.getHotUpdateService() != null
//                && TomcatValveContainer.isValveExists(new UrlMapperValve //TODO: Fix this once URLMapper component becomes available
                    ) {
                TomcatValveContainer.addValves(WebappsConstants.VALVE_INDEX, valves);
            } else {
                TomcatValveContainer.addValves(valves);
            }

        } catch (Throwable e) {
            log.error("Error occurred while activating WebappManagementServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
//         TomcatValveContainer.removeValves();
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.setServerConfigContext(null);
    }


    protected void setRealmService(RealmService realmService) {
        //keeping the realm service in the DataHolder class
        DataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
    }

    protected void setRegistryService(RegistryService registryService) {
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }

    protected void setDeploymentSynchronizerService(DeploymentSynchronizer depSynchService) {
        DataHolder.setDeploymentSynchronizerService(depSynchService);
    }

    protected void unsetDeploymentSynchronizerService(DeploymentSynchronizer depSynchService) {
        DataHolder.setDeploymentSynchronizerService(null);
    }

    private void setServerURLParam(ConfigurationContext configurationContext) {
        // Adding server url as a parameter to webapps servlet context init parameter
        WebApplicationsHolder webApplicationsHolder = (WebApplicationsHolder)
                configurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

        WebContextParameter serverUrlParam =
                new WebContextParameter("webServiceServerURL", CarbonUtils.
                        getServerURL(ServerConfiguration.getInstance(),
                                     configurationContext));

        List<WebContextParameter> servletContextParameters =
                (ArrayList<WebContextParameter>) configurationContext.
                        getProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST);

        if (servletContextParameters != null) {
            servletContextParameters.add(serverUrlParam);
        }

        if (webApplicationsHolder != null) {
            for (WebApplication application :
                    webApplicationsHolder.getStartedWebapps().values()) {
                application.getContext().getServletContext().
                        setInitParameter(serverUrlParam.getName(),
                                         serverUrlParam.getValue());
            }
        }

    }
}
