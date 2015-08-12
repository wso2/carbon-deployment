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

import org.apache.catalina.Context;
import org.apache.catalina.deploy.ApplicationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBContextConfig;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extending the TomEE ContextConfig to add WSO2 bits.
 * We need to extend the ContextConfig to add ServletContainerInitializer
 * instances that we will receive via OSGi services.
 *
 */
public class ASOpenEJBContextConfig extends OpenEJBContextConfig {

    private static final String MYFACES_TOMEEM_CONTAINER_INITIALIZER = "org.apache.tomee.myfaces.TomEEMyFacesContainerInitializer";
    private static final String TOMEE_MYFACES_CONTEXT_LISTENER = "org.apache.tomee.myfaces.TomEEMyFacesContextListener";

    private static final Log log = LogFactory.getLog(ASOpenEJBContextConfig.class.getName());

    public ASOpenEJBContextConfig(TomcatWebAppBuilder.StandardContextInfo standardContextInfo) {
        super(standardContextInfo);
    }

    @Override
    protected void processServletContainerInitializers(ServletContext ctx) {
        super.processServletContainerInitializers(ctx);
    }

    @Override
    protected void webConfig() {
        //don't set this
//        TomcatHelper.configureJarScanner(context);

        // read the real config
        super.webConfig();

        if (IgnoredStandardContext.class.isInstance(context)) { // no need of jsf
            return;
        }

        // add myfaces auto-initializer if mojarra is not present
        try {
            context.getLoader().getClassLoader().loadClass("com.sun.faces.context.SessionMap");
            return;
        } catch (final Throwable ignored) {
            // no-op
        }
        try {
            final Class<?> myfacesInitializer = Class.forName(MYFACES_TOMEEM_CONTAINER_INITIALIZER, true, context.getLoader().getClassLoader());
            final ServletContainerInitializer instance = (ServletContainerInitializer) myfacesInitializer.newInstance();
            context.addServletContainerInitializer(instance, getJsfClasses(context));
            context.addApplicationListener(new ApplicationListener(TOMEE_MYFACES_CONTEXT_LISTENER, false)); // cleanup listener
        } catch (final Exception ignored) {
            // no-op
        } catch (final NoClassDefFoundError error) {
            // no-op
        }
    }

    private Set<Class<?>> getJsfClasses(final Context context) {
        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        final ClassLoader cl = context.getLoader().getClassLoader();
        final Map<String, Set<String>> scanned = builder.getJsfClasses().get(cl);

        if (scanned == null || scanned.isEmpty()) {
            return null;
        }

        final Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Set<String> entry : scanned.values()) {
            for (String name : entry) {
                try {
                    classes.add(cl.loadClass(name));
                } catch (ClassNotFoundException ignored) {
                    log.warn("class '" + name + "' was found but can't be loaded as a JSF class");
                }
            }
        }

        return classes;
    }



}
