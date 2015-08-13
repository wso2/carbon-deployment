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

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.webapp.mgt.DataHolder;

/**
 * @scr.component name="org.wso2.carbon.webapp.mgt.internal.WebappDeploymentServiceComponent"
 * immediate="true"
 * @scr.reference name="carbon.tomcat.service"
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="0..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService"
 * unbind="unsetRegistryService"*
 */
public class WebappDeploymentServiceComponent {


    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(carbonTomcatService);
    }


    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        DataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
    }


}
