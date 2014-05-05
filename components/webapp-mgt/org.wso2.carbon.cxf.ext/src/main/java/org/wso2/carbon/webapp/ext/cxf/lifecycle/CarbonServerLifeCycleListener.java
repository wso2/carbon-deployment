/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.ext.cxf.lifecycle;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerLifeCycleListener;
import org.wso2.carbon.webapp.ext.cxf.Constants;
import org.wso2.carbon.webapp.ext.cxf.interceptor.HostNameSupportedWSDLGetInterceptor;

/**
 * Implementation of ServerLifeCycleListener interface. Custom CXF extensions can be added here. Currently perform following tasks.
 * <p/>
 * 1. Load the configuration from carbon.xml to be used in future.
 * <p/>
 * 2. Scan the registered interceptors and replace WSDLGetInterceptor by HostNameSupportedWSDLGetInterceptor if it available on Classpath.
 */

public class CarbonServerLifeCycleListener implements ServerLifeCycleListener {

    @Override
    public void startServer(Server server) {

        CarbonConfiguration configuration = CarbonConfigurationFactory.getCurrentCarbonConfiguration();
        setCarbonHostName(server, configuration);

    }

    @Override
    public void stopServer(Server server) {

    }


    protected void setCarbonHostName(Server server, CarbonConfiguration configuration) {
        String hostName = (String) configuration.getParameterValue(Constants.CARBON_HOSTNAME_PARAMETER);
        if (hostName != null && !"".equals(hostName)) {
            HostNameSupportedWSDLGetInterceptor getInterceptor = new HostNameSupportedWSDLGetInterceptor(hostName);
            CarbonServerLifeCycleUtil.replaceInInterceptor(server, Constants.CXF_WSDLGET_INTERCEPTOR_NAME, getInterceptor);
        }
    }


}