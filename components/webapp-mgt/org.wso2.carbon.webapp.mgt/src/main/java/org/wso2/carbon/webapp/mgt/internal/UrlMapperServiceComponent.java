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
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.webapp.mgt.DataHolder;

/**
 * UrlMapperServiceComponent is to get HotUpdateService
 * from url-mapper whenever the component is available.
 *
 * @scr.component name="org.wso2.carbon.webapp.mgt.internal.UrlMapperServiceComponent"
 * immediate="true"
  * @scr.reference name="url.mapper.service"
 * interface="org.wso2.carbon.url.mapper.HotUpdateService"
 * cardinality="0..1" policy="dynamic" bind="setHotUpdateService"
 * unbind="unsetHotUpdateService"
 */
public class UrlMapperServiceComponent {

    private static final Log log = LogFactory.getLog(UrlMapperServiceComponent.class);

    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.info("Activating URL Mapped Service Component");
        }
    }

    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating URL Mapped Service Component");
        }
    }

    protected void setHotUpdateService(HotUpdateService hotUpdateService) {
        DataHolder.setHotUpdateService(hotUpdateService);
    }

    protected void unsetHotUpdateService(HotUpdateService hotUpdateService) {
        DataHolder.setHotUpdateService(null);
    }
}
