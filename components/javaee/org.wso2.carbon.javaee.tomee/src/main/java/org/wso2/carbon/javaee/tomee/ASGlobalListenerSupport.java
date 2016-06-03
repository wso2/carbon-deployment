/*
* Copyright 2015 The Apache Software Foundation.
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
package org.wso2.carbon.javaee.tomee;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomee.catalina.ContextListener;
import org.apache.tomee.catalina.GlobalListenerSupport;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationData;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationReader;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService;
import org.wso2.carbon.webapp.mgt.utils.WebAppConfigurationUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ASGlobalListenerSupport extends GlobalListenerSupport {
    public static final String JAVA_EE_CRE = "JavaEE";
    public static final String JAVA_EE_OLD_CRE = "J2EE";
    public static final String IS_JAVA_EE_APP = "IS_JAVA_EE_APP";
    private static final Log log = LogFactory.getLog(ASTomEEServerListener.class.getName());

    public ASGlobalListenerSupport(StandardServer standardServer, ContextListener contextListener) {
        super(standardServer, contextListener);
    }

    public void lifecycleEvent(LifecycleEvent event) {
        try {
            Object source = event.getSource();
            if (source instanceof StandardContext) {
                StandardContext standardContext = (StandardContext) source;

                Boolean isJavaEEApp;
                if ((isJavaEEApp = (Boolean) standardContext.getServletContext().
                        getAttribute(IS_JAVA_EE_APP)) == null) {
                    isJavaEEApp = isJavaEEApp(standardContext);
                    standardContext.getServletContext().setAttribute(IS_JAVA_EE_APP, isJavaEEApp);
                }

                if (isJavaEEApp) {
                    super.lifecycleEvent(event);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("JavaEE CRE was not found for this webapp - " + ((StandardContext) source).getName() +
                                ". Not continuing the OpenEJB container initialization.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not determine the Classloader Runtime Environment. " +
                    "Not continuing the OpenEJB container initialization." + e.getMessage(), e);
        }
    }

    private boolean isJavaEEApp(StandardContext standardContext) throws Exception {
        String webAppFilePath = WebAppConfigurationUtils.getWebAppFilePath(standardContext);
        if (!Files.exists(Paths.get(webAppFilePath))) {
            //This happens when the webapp and its unpacked dir is deleted which triggers
            //undeployment events. The after_stop event do not contain the servlet context attributes we set.
            //Since all we have to do is cleanup, we are simply going let all the webapps go into the tomee stop events.
            return true;
        }

        WebAppConfigurationService webAppConfigurationService = DataHolder.getWebAppConfigurationService();
        WebAppConfigurationData webAppConfigurationData;

        //The service might not be up because the bundle org.wso2.carbon.webapp.mgt
        //may get activated after the lifecycle event
        if (webAppConfigurationService != null) {
            webAppConfigurationData = webAppConfigurationService.getConfiguration(webAppFilePath);
            if (webAppConfigurationData == null) {
                webAppConfigurationData = WebAppConfigurationReader.retrieveWebConfigData(webAppFilePath);
            }
        } else {
            webAppConfigurationData = WebAppConfigurationReader.retrieveWebConfigData(webAppFilePath);
        }
        List<String> webappCREs = null;
        if (webAppConfigurationData != null) {
            webappCREs = webAppConfigurationData.getEnvironments();
        }
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (webappCREs != null) {
            set.addAll(webappCREs);
        }

        return set.contains(JAVA_EE_CRE) || set.contains(JAVA_EE_OLD_CRE);
    }
}
