/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ejbservices.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ejbservices.service.EJBServicesAdmin;
import org.wso2.carbon.ejbservices.component.xml.EJBAppServerConfig;
import org.wso2.carbon.ejbservices.util.EJBConstants;
import org.wso2.carbon.utils.component.xml.Component;
import org.wso2.carbon.utils.component.xml.ComponentConfigFactory;

import java.net.URL;
import java.io.InputStream;

public class Activator implements BundleActivator {

    public void start(BundleContext bundleContext) throws Exception {
        ServiceReference registryServiceReference =
                bundleContext.getServiceReference(RegistryService.class.getName());
        if (registryServiceReference != null) {
            RegistryService registryService =
                    (RegistryService) bundleContext.getService(registryServiceReference);
            EJBServicesAdmin.setRegistry(registryService.getConfigSystemRegistry());

            URL url = bundleContext.getBundle().getEntry("META-INF/component.xml");
            if (url == null) {
                return;
            }

            InputStream inputStream = url.openStream();
            Component component = ComponentConfigFactory.build(inputStream);
            EJBAppServerConfig[] appServerConfigs = (EJBAppServerConfig[])
                    component.getComponentConfig(EJBConstants.ComponentConfig.EJB_APP_SERVERS);

            if(appServerConfigs != null) {
                EJBServicesAdmin.setEJBAppServerConfig(appServerConfigs);}

        } else {
            throw new Exception("Registry service is not found");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
