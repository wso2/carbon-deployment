/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.ext;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  This Listener will construct and set the ApplicationInfo for applications
 */
@SuppressWarnings("UnusedDeclaration")
public class ASExtLifecycleListener extends CarbonLifecycleListenerBase {
    private static final Log log = LogFactory.getLog(ASExtLifecycleListener.class);

    @Override
    public void lifecycleEvent(StandardContext context, ApplicationInfo applicationInfo) {

        try {
            URL url = context.getServletContext().getResource("/META-INF/application.xml");

            //todo properly read the application.xml and find the property "isManagedAPI=true"
            if(url != null){
                //TODO add more info into ApplicationInfo eg: api version, API name, api contexts + params
                String appVersion = getAppVersion(context.getName());
                String appName = context.getName().substring(0, context.getName().indexOf(appVersion) - 1);
                ApplicationInfo info = new ApplicationInfo(appName, appVersion);

                //todo handle scenario for unversioned apps
                info.setManagedApi(true);
                if(log.isDebugEnabled()) {
                    log.debug("Application : ".concat(context.getName()).concat(" specifies managedAPI=true"));
                }
                setAppInfo(context, info);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param appContext : context of the application
     * @return the application version. If the application doesn't specify a version return empty string.
     */
    private String getAppVersion(String appContext) {
        String versionString = appContext;
        if (versionString.startsWith("/t/")) {
            //remove tenant context
            versionString = versionString.substring(appContext.lastIndexOf("/webapps/") + 9);
        } else if(appContext.startsWith("/")) {
            versionString = versionString.substring(1);
        }
        if (versionString.contains("/")) {
            versionString = versionString.substring(versionString.indexOf("/") + 1);
            return versionString;
        } else {
            return "";
        }
    }

}
