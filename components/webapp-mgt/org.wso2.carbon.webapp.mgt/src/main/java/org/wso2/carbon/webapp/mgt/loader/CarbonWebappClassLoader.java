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
package org.wso2.carbon.webapp.mgt.loader;

import org.apache.catalina.loader.WebappClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Customized WebappClassloader for Carbon. This class introduces a new classloading pattern which is based on the
 * webapp-classloading.xml file. The default behaviour is specified in the container level configuration file.
 * But webapps has the ability to override that behaviour by adding the customised  webapp-classloading.xml file into the
 * webapp.
 */
public class CarbonWebappClassLoader extends WebappClassLoader {
    private static final Log log = LogFactory.getLog(CarbonWebappClassLoader.class);

    private WebappClassloadingContext webappCC;

    public CarbonWebappClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void setWebappCC(WebappClassloadingContext classloadingContext) {
        this.webappCC = classloadingContext;
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (log.isDebugEnabled())
            log.debug("loadClass(" + name + ", " + resolve + ")");
        Class<?> clazz;

        // Log access to stopped classloader
        if (!started) {
            try {
                throw new IllegalStateException();
            } catch (IllegalStateException e) {
                log.info(sm.getString("webappClassLoader.stopped", name), e);
            }
        }

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        // (0.5) Permission to access this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0, i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                            "Restricted Class: " + name;
                    log.info(error, se);
                    throw new ClassNotFoundException(error, se);
                }
            }
        }


        // 1) Load from the parent if the parent-first is true and if package matches with the
        //    list of delegated packages
        boolean delegatedPkg = webappCC.isDelegatedPackage(name);
        boolean excludedPkg = webappCC.isExcludedPackage(name);

        if (webappCC.isParentFirst() && delegatedPkg && !excludedPkg) {
            clazz = findClassFromParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        // 2) Load the class from the local(webapp) classpath
        clazz = findLocalClass(name, resolve);
        if (clazz != null) {
            return clazz;
        }

        // 3) TODO load from the shared repositories

        // 4) Load from the parent if the parent-first is false and if the package matches with the
        //    list of delegated packages.
        if (!webappCC.isParentFirst() && delegatedPkg && !excludedPkg) {
            clazz = findClassFromParent(name, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        throw new ClassNotFoundException(name);
    }

    protected Class<?> findClassFromParent(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (log.isDebugEnabled())
            log.debug("  Delegating to parent classloader1 " + parent);
        ClassLoader loader = parent;
        if (loader == null)
            loader = system;
        try {
            clazz = Class.forName(name, false, loader);
            if (clazz != null) {
                if (log.isDebugEnabled())
                    log.debug("  Loading class from parent");
                if (resolve)
                    resolveClass(clazz);
            }
        } catch (ClassNotFoundException e) {
//            Ignore
        }
        return (clazz);
    }

    protected Class<?> findLocalClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (log.isDebugEnabled())
            log.debug("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (log.isDebugEnabled())
                    log.debug("  Loading class from local repository");
                if (resolve)
                    resolveClass(clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return (clazz);
    }
}
