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

import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ContextTransaction;
import org.apache.catalina.startup.ContextConfig;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.common.UserTransactionFactory;
import org.apache.tomee.loader.TomcatHelper;
import org.wso2.carbon.tomcat.ext.scan.CarbonTomcatJarScanner;

import javax.servlet.ServletContext;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ASTomcatWebAppBuilder extends TomcatWebAppBuilder {

    /**
     * We are over-riding the default tomee @link GlobalListenerSupport listener
     * to add wso2-specific bits such as webapp CREs.
     */
    private final ASGlobalListenerSupport globalListenerSupport;

    private Class<?> sessionManagerClass = null;
    private static final boolean FORCE_RELOADABLE = SystemInstance.get().getOptions().get("tomee.force-reloadable", false);
    private static final boolean SKIP_TLD = SystemInstance.get().getOptions().get("tomee.skip-tld", false);
    private static final org.apache.openejb.util.Logger logger = org.apache.openejb.util.Logger.
            getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");
    private String defaultHost = "localhost";
    private static Method startInternal = null; // it just sucks but that's private
    private static Method addMyFacesDefaultParameters = null; // it just sucks but that's private


    static {
        try {
            startInternal = TomcatWebAppBuilder.class.getDeclaredMethod("startInternal", StandardContext.class);
            startInternal.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new OpenEJBRuntimeException("can't find method isIgnored", e);
        }

        try {
            addMyFacesDefaultParameters = TomcatWebAppBuilder.class.
                    getDeclaredMethod("addMyFacesDefaultParameters", ClassLoader.class, ServletContext.class);
            addMyFacesDefaultParameters.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new OpenEJBRuntimeException("can't find method isIgnored", e);
        }


    }

    public ASTomcatWebAppBuilder() {
        super();

        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new ASGlobalListenerSupport(standardServer, this);

        for (final Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = (Engine) service.getContainer();
                this.defaultHost = engine.getDefaultHost();
            }
        }
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
        //super init needs to be called to initialize the OpenEJBContextConfig,
        //NamingContextListeners etc. Why was this not done before?
        super.init(standardContext);

        //init will only get called if this is a JavaEE webapp.
        // So, we don't have to re-check the CRE
        standardContext.setIgnoreAnnotations(true);

        //over-ride super.init - TomEE jar scanner with Carbon bits
        standardContext.setJarScanner(new CarbonTomcatJarScanner());

        //doInit(standardContext);

        //setContextConfig(standardContext);
    }

    /**
     * over-ridden the super method to stop adding the tomee jar scanner,
     */
    @Override
    public void configureStart(final StandardContext standardContext) {
        if (TomcatHelper.isTomcat7()) {
            //don't set this
            //TomcatHelper.configureJarScanner(standardContext);

            final ContextTransaction contextTransaction = new ContextTransaction();
            contextTransaction.setProperty(org.apache.naming.factory.Constants.FACTORY, UserTransactionFactory.class.getName());
            standardContext.getNamingResources().setTransaction(contextTransaction);
            try {
                startInternal.invoke(this, standardContext);
            } catch (InvocationTargetException e) {
                throw new TomEERuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new TomEERuntimeException(e);
            }
        }

        // clear a bit log for default case
        try {
            addMyFacesDefaultParameters.invoke(this,
                    standardContext.getLoader().getClassLoader(), standardContext.getServletContext());
        } catch (InvocationTargetException e) {
            throw new TomEERuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new TomEERuntimeException(e);
        }

        // breaks cdi
        standardContext.setTldValidation(Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.tld.validation", "false")));
        // breaks jstl
        standardContext.setXmlValidation(Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.xml.validation", "false")));
    }

    /**
     * TomEE adds OpenEJBContextConfig in TomcatWebAppBuilder#init method.
     * We need to remove that add our own custom TomEE ContextConfig.
     * @param standardContext ctx
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
        ContextConfig contextConfig = new ASOpenEJBContextConfig(new StandardContextInfo(standardContext));
        contextConfig.setDefaultWebXml(globalWebXml);
        contextConfig.setDefaultContextXml(globalContextXml);

        standardContext.addLifecycleListener(contextConfig);

    }


}
