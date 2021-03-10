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
import org.apache.tomcat.util.ExceptionUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.webapp.mgt.loader.shared.SharedClassLoaderFactory;
import org.wso2.carbon.webapp.mgt.loader.shared.SharedURLClassLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Customized WebappClassloader for Carbon. This class introduces a new classloading pattern which is based on the
 * webapp-classloading.xml file. The default behaviour is specified in the container level configuration file.
 * But webapps has the ability to override that behaviour by adding the customised  webapp-classloading.xml file into the
 * webapp.
 */
public class CarbonWebappClassLoader extends WebappClassLoader {
    private static final Log log = LogFactory.getLog(CarbonWebappClassLoader.class);

    private WebappClassloadingContext webappCC;
    private static List<String> systemPackages;
    private static final String CLASS_FILE_SUFFIX = ".class";

    public CarbonWebappClassLoader(ClassLoader parent) {
        super(SharedClassLoaderFactory.getSharedClassLoader());
        String launchIniPath = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "etc", "launch.ini").toString();
        readSystemPackagesList(launchIniPath);
    }

    public void setWebappCC(WebappClassloadingContext classloadingContext) {
        this.webappCC = classloadingContext;
        ((SharedURLClassLoader)this.parent).setWebappCC(classloadingContext);
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (log.isDebugEnabled())
            log.debug("loadClass(" + name + ", " + resolve + ")");
        Class<?> clazz;

        // Log access to stopped class loader
        checkStateForClassLoading(name);

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
        //       the webapp from overriding Java SE classes. This implements
        //       SRV.10.7.2
        String resourceName = binaryNameToPath(name, false);

        ClassLoader javaseLoader = getJavaseClassLoader();
        boolean tryLoadingFromJavaseLoader;
        try {
            // Use getResource as it won't trigger an expensive
            // ClassNotFoundException if the resource is not available from
            // the Java SE class loader. However (see
            // https://bz.apache.org/bugzilla/show_bug.cgi?id=58125 for
            // details) when running under a security manager in rare cases
            // this call may trigger a ClassCircularityError.
            // See https://bz.apache.org/bugzilla/show_bug.cgi?id=61424 for
            // details of how this may trigger a StackOverflowError
            // Given these reported errors, catch Throwable to ensure any
            // other edge cases are also caught
            URL url;
            if (securityManager != null) {
                PrivilegedAction<URL> dp = new PrivilegedJavaseGetResource(resourceName);
                url = AccessController.doPrivileged(dp);
            } else {
                url = javaseLoader.getResource(resourceName);
            }
            tryLoadingFromJavaseLoader = (url != null);
        } catch (Throwable t) {
            // Swallow all exceptions apart from those that must be re-thrown
            ExceptionUtils.handleThrowable(t);
            // The getResource() trick won't work for this class. We have to
            // try loading it directly and accept that we might get a
            // ClassNotFoundException.
            tryLoadingFromJavaseLoader = true;
        }

        if (tryLoadingFromJavaseLoader) {
            try {
                clazz = javaseLoader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        // (0.5) Permission to access this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = sm.getString("webappClassLoader.restrictedPackage", name);
                    log.info(error, se);
                    throw new ClassNotFoundException(error, se);
                }
            }
        }



        // 1) Load from the parent if the parent-first is true.
        if (webappCC.isParentFirst()) {
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

        // 4) Load from the parent if the parent-first is false.
        if (!webappCC.isParentFirst()) {
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
            loader = getJavaseClassLoader();
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

        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream stream = super.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            } else if (name.endsWith(CLASS_FILE_SUFFIX) && isSystemPackage(name)) {
                ClassLoader loader = getJavaseClassLoader();
                stream = loader.getResourceAsStream(name);

                if (stream != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("  --> Returning stream from system classloader");
                    }
                    return stream;
                }
            }

            return null;
        }

    private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
        // 1 for leading '/', 6 for ".class"
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        if (withLeadingSlash) {
            path.append('/');
        }
        path.append(binaryName.replace('.', '/'));
        path.append(CLASS_FILE_SUFFIX);
        return path.toString();
    }

    private boolean isSystemPackage(String resourceName) {
        resourceName = resourceName.replace(".class", "").
                replace("/", ".");
        String packageName = resourceName.lastIndexOf(".") == -1 ?
                resourceName : resourceName.substring(0, resourceName.lastIndexOf("."));

        return systemPackages.contains(packageName);
    }

    private void readSystemPackagesList(String launchIniPath) {
        Properties properties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(launchIniPath);
            properties.load(fileInputStream);

            String rawSystemPackages = properties.getProperty("org.osgi.framework.system.packages");

            String[] systemPackagesArray = rawSystemPackages.split("[ ]?,[ ]?");
            this.systemPackages = Arrays.asList(systemPackagesArray);

        } catch (IOException e) {
            log.warn("Error reading system packages list from launch.ini", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration[] tmp = new Enumeration[2];
        /*
        The logic to access BootstrapClassPath is JDK vendor dependent hence
        we can't call it from here. Ensure 'parentCL != null', to find resources
        from BootstrapClassPath.

         */
        if (parent != null && webappCC != null) {
            boolean delegatedRes = webappCC.isDelegatedResource(name);
            boolean excludedRes = webappCC.isExcludedResources(name);
            if (delegatedRes && !excludedRes) {
                tmp[0] = parent.getResources(name);
            }

        }
        tmp[1] = findResources(name);

        return new CompoundEnumeration(tmp);
    }


}
