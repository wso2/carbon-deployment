/*
* Copyright 2004,2013 The Apache Software Foundation.
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

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomee.catalina.OpenEJBContextConfig;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.loader.TomcatHelper;

import java.io.File;

public class ASTomcatWebAppBuilder extends TomcatWebAppBuilder {

    /**
     * We are over-riding the default tomee @link GlobalListenerSupport listener
     * to add wso2-specific bits such as webapp CREs.
     */
    private final ASGlobalListenerSupport globalListenerSupport;

    public ASTomcatWebAppBuilder() {
        super();
        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new ASGlobalListenerSupport(standardServer, this);

    }

    /**
     * Start operation.
     */
    @Override
    public void start() {
        globalListenerSupport.start();
    }

    /**
     * Start operation.
     */
    @Override
    public void stop() {
        globalListenerSupport.stop();
    }

    public void init(final StandardContext standardContext) {
        //init will only get called if this is a JavaEE webapp.
        // So, we don't have to re-check the CRE
        standardContext.setIgnoreAnnotations(true);

        //TomEE jar scanner with Carbon bits
//        standardContext.setJarScanner(new ASTomEEJarScanner());

        super.init(standardContext);

//        setContextConfig(standardContext);
    }

    /**
     * TomEE adds OpenEJBContextConfig in TomcatWebAppBuilder#init method.
     * We need to remove that add our own custom TomEE ContextConfig.
     * @param standardContext
     */
    protected void setContextConfig(StandardContext standardContext) {
        final LifecycleListener[] listeners = standardContext.findLifecycleListeners();
        for (final LifecycleListener l : listeners) {
            if (l instanceof ContextConfig) {
                standardContext.removeLifecycleListener(l);
            }
        }
        //set default web.xml and context.xml
        String globalWebXml = new File(System.getProperty("carbon.home")).getAbsolutePath() +
                File.separator + "repository" + File.separator + "conf" + File.separator +
                "tomcat" + File.separator + "web.xml";
        String globalContextXml = new File(System.getProperty("carbon.home")).getAbsolutePath() +
                File.separator + "repository" + File.separator + "conf" + File.separator +
                "tomcat" + File.separator + "context.xml";
        ContextConfig contextConfig = new OpenEJBContextConfig(new StandardContextInfo(standardContext));
        contextConfig.setDefaultWebXml(globalWebXml);
        contextConfig.setDefaultContextXml(globalContextXml);

        standardContext.addLifecycleListener(contextConfig);

    }


}
