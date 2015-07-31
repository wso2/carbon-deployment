/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.javaee.tomee;

import org.apache.catalina.Host;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomee.catalina.ContextListener;
import org.apache.tomee.catalina.GlobalListenerSupport;
import org.wso2.carbon.webapp.mgt.loader.ClassloadingContextBuilder;
import org.wso2.carbon.webapp.mgt.loader.WebappClassloadingContext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class ASGlobalListenerSupport extends GlobalListenerSupport {
    private static final Log log = LogFactory.getLog(ASTomEEServerListener.class.getName());

    public static final String JAVA_EE_CRE = "JavaEE";
    public static final String JAVA_EE_OLD_CRE = "J2EE";
    public static final String IS_JAVA_EE_APP = "IS_JAVA_EE_APP";

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
                                  ". Not continuing the TomEE container initialization.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not determine the Classloader Runtime Environment. " +
                    "Not continuing the TomEE container initialization. " + e.getMessage(), e);
        }
    }

    private boolean isJavaEEApp(StandardContext standardContext) {
        try {
            String webappFilePath = getWebappFilePath(standardContext);
            if (!new File(webappFilePath).exists()) {
                //This happens when the webapp and its unpacked dir is deleted which triggers
                //undeployment events. The after_stop event do not contain the servlet context attributes we set.
                //Since all we have to do is cleanup, we are simply going let all the webapps go into the tomee stop events.
                return true;
            }

            WebappClassloadingContext clContext =
                    ClassloadingContextBuilder.buildClassloadingContext(webappFilePath);
            //check if the classloading environment is JavaEE
            String[] webappCREs = clContext.getEnvironments();
            if (webappCREs != null) {
                Set<String> set=  new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                set.addAll(
                        Arrays.asList(webappCREs));

                return set.contains(JAVA_EE_CRE) || set.contains(JAVA_EE_OLD_CRE);
            } else {
                String[] defaultCREs = WebappClassloadingContext.getClassloadingConfig().getEnvironments();
                Set<String> set=  new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                set.addAll(
                        Arrays.asList(defaultCREs));

                return set.contains(JAVA_EE_CRE) || set.contains(JAVA_EE_OLD_CRE);
            }
        } catch (Exception e) {
            log.error("Error occurred while reading classloader configuration. " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Borrowed from ClassloadingContextBuilder#getWebappFilePath private method
     * @return webapp file path
     * @throws IOException
     */
    private String getWebappFilePath(StandardContext ctx) throws IOException {
        String webappFilePath;

        //Value of the following variable depends on various conditions. Sometimes you get just the webapp directory
        //name. Sometime you get absolute path the webapp directory or war file.
//        Context ctx = (Context) getContainer();
        String docBase = ctx.getDocBase();

        Host host = (Host) ctx.getParent();
        String appBase = host.getAppBase();
        File canonicalAppBase = new File(appBase);
        if (canonicalAppBase.isAbsolute()) {
            canonicalAppBase = canonicalAppBase.getCanonicalFile();
        } else {
            canonicalAppBase =
                    new File(System.getProperty("carbon.home"), appBase)
                            .getCanonicalFile();
        }

        File webappFile = new File(docBase);
        if (webappFile.isAbsolute()) {
            webappFilePath = webappFile.getCanonicalPath();
        } else {
            webappFilePath = (new File(canonicalAppBase, docBase)).getPath();
        }
        return webappFilePath;
    }



}
