/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.service.mgt;

import junit.framework.TestCase;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;

import java.io.File;
import java.io.InputStream;
import java.lang.Throwable;

public class BaseTestCase extends TestCase {

    protected static InMemoryEmbeddedRegistryService embeddedRegistryService = null;
    protected static Registry configRegistry = null;
    protected static Registry governanceRegistry = null;

    public void setUp() throws Exception{

        if (embeddedRegistryService != null) {
            return;
        }

        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        // The line below is responsible for initializing the cache.
        CarbonContext.getCurrentContext();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        try {
            InputStream regConfigStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("registry.xml");
            embeddedRegistryService = new InMemoryEmbeddedRegistryService(regConfigStream);

            RegistryCoreServiceComponent component = new RegistryCoreServiceComponent() {
                {
                    setRealmService(embeddedRegistryService.getRealmService());
                }
            };
            component.registerBuiltInHandlers(embeddedRegistryService);

            configRegistry = embeddedRegistryService.getConfigSystemRegistry();
            governanceRegistry = embeddedRegistryService.getGovernanceSystemRegistry();
        } catch (RegistryException e) {
            throw e;
            //fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }
}
