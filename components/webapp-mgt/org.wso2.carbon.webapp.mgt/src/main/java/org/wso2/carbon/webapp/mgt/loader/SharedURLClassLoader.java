/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.webapp.mgt.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * This classloader will be the parent of the CarbonWebappClassLoader class loader.
 * It will loop through the shared environment class loaders to load the resource.
 */
class SharedURLClassLoader extends URLClassLoader {

    private static final Log log = LogFactory.getLog(SharedURLClassLoader.class);
    private WebappClassloadingContext webappCC;

    public SharedURLClassLoader(URL[] array, ClassLoader parent) {

        super(array, parent);
    }

    public void setWebappCC(WebappClassloadingContext classloadingContext) {

        this.webappCC = classloadingContext;
    }


    private URLClassLoader getSharedEnvironmentClassLoader(String environment) {

        return SharedClassLoaderFactory.getInstance().getEnvironmentClassLoader(environment);
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                // 1) Load from the parent if the parent-first is true and if package matches with the
                //    list of delegated packages
                boolean delegatedPkg = webappCC.isDelegatedPackage(name);
                boolean excludedPkg = webappCC.isExcludedPackage(name);

                if (webappCC.isParentFirst() && delegatedPkg && !excludedPkg) {
                    c = super.getParent().loadClass(name);
                    if (c != null) {
                        return c;
                    }
                }

                // 2) Load the class from the shared class loaders.
                c = findClassFromEnvironmentalClassLoaders(name, resolve);
                if (c != null) {
                    return c;
                }


                // 3) Load from the parent if the parent-first is false and if the package matches with the
                //    list of delegated packages.
                if (!webappCC.isParentFirst() && delegatedPkg && !excludedPkg) {
                    c = super.getParent().loadClass(name);
                    if (c != null) {
                        return c;
                    }
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    private Class<?> findClassFromEnvironmentalClassLoaders(String name, boolean resolve) {

        Class<?> clazz = null;
        if (webappCC.getEnvironments() == null) {
            return null;
        }
        for (String environment : webappCC.getEnvironments()) {
            ClassLoader loader = getSharedEnvironmentClassLoader(environment);
            if (log.isDebugEnabled()) {
                log.debug("  Delegating to Environmental classloader " + loader);
            }
            if (loader == null)
                continue;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (log.isDebugEnabled())
                        log.debug("  Loading class from " + loader + " Environmental classloader");
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException|NoClassDefFoundError e) {
//              Ignore
            }
        }
        return clazz;
    }

    @Override
    public URL findResource(String name) {

        if (webappCC.getEnvironments() == null) {
            return null;
        }
        for (String environment : webappCC.getEnvironments()) {
            URLClassLoader exclusiveEnvironmentClassloader = getSharedEnvironmentClassLoader(environment);
            if (exclusiveEnvironmentClassloader != null) {
                return exclusiveEnvironmentClassloader.findResource(name);
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {

        if (webappCC.getEnvironments() == null) {
            return null;
        }
        for (String environment : webappCC.getEnvironments()) {
            URLClassLoader exclusiveEnvironmentClassloader = getSharedEnvironmentClassLoader(environment);
            if (exclusiveEnvironmentClassloader != null) {
                Enumeration<URL> url = exclusiveEnvironmentClassloader.findResources(name);
                if (url != null) {
                    return url;
                }
            }
        }
        return null;
    }
}
