/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.webapp.mgt.config;

import org.apache.catalina.startup.ContextConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * According to Servlet3 specification web containers should scan and should support for {@link javax.servlet.ServletContainerInitializer}.
 * {@link javax.servlet.ServletContainerInitializer} are provided to web containers through Java Service provider architecture. Tomcat's
 * {@link org.apache.catalina.startup.ContextConfig} class is responsible for this task but it can't load
 * {@link javax.servlet.ServletContainerInitializer} implementations from OSGi bundles due to OSGi constrains.
 *
 * Apache Aries SPI Fly component is used to get rid of this limitation where this component can scan OSGi bundles for
 * mentioned Java SPIs and register them as OSGi services. CarbonContextConfig class retrieve {@link javax.servlet.ServletContainerInitializer}
 * SPIs registered  by  SPI Fly component and register them with Tomcat container.
 *
 * Bundles with {@link javax.servlet.ServletContainerInitializer} implementations should contains following two OGSi headers.
 *
 *      Require-Capability: osgi.extender; filter:="(osgi.extender=osgi.serviceloader.registrar)"
 *      Provide-Capability: osgi.serviceloader; osgi.serviceloader=javax.servlet.ServletContainerInitializer
 *
 *
 * For Apache Felix bundle plugin use following configuration in POM files.
 *
 *      <Require-Capability>
 *          osgi.extender;filter:="(osgi.extender=osgi.serviceloader.registrar)"
 *      </Require-Capability>
 *
 *      <Provide-Capability>
 *          osgi.serviceloader;osgi.serviceloader=javax.servlet.ServletContainerInitializer
 *      </Provide-Capability>
 *
 *
 *  @since  4.2.3
 *  @see javax.servlet.ServletContainerInitializer
 *
 */
public class CarbonContextConfig extends ContextConfig {

    private static Log log = LogFactory.getLog(CarbonContextConfig.class);

    @Override
    protected void processServletContainerInitializers(ServletContext servletContext) {
        //Let parent to process first
        super.processServletContainerInitializers(servletContext);

        for (ServletContainerInitializer sci : loadScisFromBundles()) {
            initializerClassMap.put(sci, new HashSet<Class<?>>());
            processHandlesTypesAnnotation(sci);
        }
    }

    /*
     Refer {@link org.apache.catalina.startup.ContextConfig#processServletContainerInitializers(ServletContext)} method.

     */
    private void processHandlesTypesAnnotation(ServletContainerInitializer sci) {
        HandlesTypes ht;
        try {
            ht = sci.getClass().getAnnotation(HandlesTypes.class);

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.info(sm.getString(sci.getClass().getName()), e);
            } else {
                log.info(sm.getString(sci.getClass().getName()));
            }
            return;
        }
        if (ht == null) {
            return;
        }
        Class<?>[] types = ht.value();
        if (types == null) {
            return;
        }

        for (Class<?> type : types) {
            if (type.isAnnotation()) {
                handlesTypesAnnotations = true;
            } else {
                handlesTypesNonAnnotations = true;
            }
            Set<ServletContainerInitializer> scis =
                    typeInitializerMap.get(type);
            if (scis == null) {
                scis = new HashSet<ServletContainerInitializer>();
                typeInitializerMap.put(type, scis);
            }
            scis.add(sci);
        }


    }

    private List<ServletContainerInitializer> loadScisFromBundles() {
        List<ServletContainerInitializer> services = new ArrayList<ServletContainerInitializer>();
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, ServletContainerInitializer.class, null);
        try {
            serviceTracker.open();
            for (Object obj : serviceTracker.getServices()) {
                services.add((ServletContainerInitializer) obj);

            }
        } finally {
            serviceTracker.close();
        }
        return services;
    }


}
