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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Handles lazily loading tenant artifacts if a tenant webapp request comes in
 */
public class TenantLazyLoaderValve extends CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(TenantLazyLoaderValve.class);

    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        String requestURI = request.getRequestURI();
        String requestedHostName = request.getServerName();

        //getting actual uri when accessing a virtual host through url mapping from the Map
        String uriOfVirtualHost = URLMappingHolder.getInstance().getApplicationFromUrlMapping(requestedHostName);
        //getting the host name of first request from registry if & only if the request contains url-mapper suffix
        if(TomcatUtil.isVirtualHostRequest(requestedHostName) && uriOfVirtualHost == null) {
            uriOfVirtualHost = DataHolder.getHotUpdateService().
                    getApplicationContextForHost(requestedHostName);
        }

        if(uriOfVirtualHost != null) {
            requestURI = uriOfVirtualHost;
        }

        String domain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
        if (domain == null || domain.trim().length() == 0) {
            getNext().invoke(request, response, compositeValve);
            return;
        }
        if (!(requestURI.contains("/" + WebappsConstants.WEBAPP_PREFIX + "/") ||
                requestURI.contains("/" + WebappsConstants.JAGGERY_APPS_PREFIX + "/") ||
                requestURI.contains("/" + WebappsConstants.JAX_WEBAPPS_PREFIX + "/"))) {
            getNext().invoke(request, response, compositeValve);
            return;
        }
        //check whether the tenant exists. If not, return. This will end up
        //by showing a 404 in the browser
        try {
            TenantManager tenantManager = DataHolder.getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(domain);
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                if (log.isDebugEnabled()) {
                    log.debug("Tenant does not exist: " + domain);
                }
                getNext().invoke(request, response, compositeValve);
                return;
            }
        } catch (Exception e) {
            log.error("Error occurred while checking tenant existence", e);
            getNext().invoke(request, response, compositeValve);
            return;
        }


        ConfigurationContext serverConfigCtx = DataHolder.getServerConfigContext();
        if (TenantAxisUtils.getLastAccessed(domain, serverConfigCtx) == -1) { // First time access
            try {
                setTenantAccessed(domain, serverConfigCtx);
                if (requestURI.contains("/" + WebappsConstants.WEBAPP_PREFIX + "/") ||
                        requestURI.contains("/" + WebappsConstants.JAX_WEBAPPS_PREFIX + "/") ||
                        requestURI.contains("/" + WebappsConstants.JAGGERY_APPS_PREFIX + "/")) {
                    TomcatUtil.remapRequest(request);
                } else {
                    request.getRequestDispatcher(requestURI).forward(request, response);
                }
            } catch (Exception e) {
                String msg = "Cannot redirect tenant request to " + requestURI +
                        " for tenant " + domain;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        setTenantAccessed(domain, serverConfigCtx);
        getNext().invoke(request, response, compositeValve);
    }


    private void setTenantAccessed(String domain, ConfigurationContext serverConfigCtx) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(TenantLazyLoaderValve.
                    class.getClassLoader());
            TenantAxisUtils.setTenantAccessed(domain, serverConfigCtx);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
}
