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
package org.wso2.carbon.webapp.mgt;

import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Used for keeping track of request received by each webapp
 */
public class CarbonServletRequestListener implements ServletRequestListener {
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        // Nothing to do here
    }

    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        setTenantAccessed(servletRequestEvent);
    }

    private void setTenantAccessed(ServletRequestEvent servletRequestEvent) {
        String requestURI =
                ((HttpServletRequest) servletRequestEvent.getServletRequest()).getRequestURI();
        if (requestURI.startsWith("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/")) { // It is a tenant request
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
            TenantAxisUtils.setTenantAccessed(tenantDomain,
                                              DataHolder.getServerConfigContext());
        }
    }
}
