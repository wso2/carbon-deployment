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

package org.wso2.carbon.webapp.mgt.utils;

import org.apache.catalina.tribes.util.StringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.scan.Constants;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CarbonWebappServiceLoader<T> {

    //TODO - Improve this class to support AS CLR architecture.

    private static final String SERVICES = "META-INF/services/";
    private static final Log log = LogFactory.getLog(CarbonWebappServiceLoader.class);
    private static final Set<String> defaultJarsToSkip = new HashSet<String>();
    private static final StringManager sm =
            StringManager.getManager(Constants.Package);
    private static final String CARBON_PLUGINS_DIR_PATH = System.getProperty("carbon.home") + File.separator +
            "repository" + File.separator + "components" + File.separator + "plugins";
    private static final String CARBON_DROPINS_DIR_PATH = System.getProperty("carbon.home") + File.separator +
            "repository" + File.separator + "components" + File.separator + "dropins";

    private static CarbonWebappServiceLoader instance;

    static {
        String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
        if (jarList != null) {
            StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
            while (tokenizer.hasMoreElements()) {
                defaultJarsToSkip.add(tokenizer.nextToken());
            }
        }
    }

    private CarbonWebappServiceLoader() {

    }

    public static CarbonWebappServiceLoader getInstance() {
        if (instance == null) {
            instance = new CarbonWebappServiceLoader();
        }
        return instance;

    }

    /**
     * Load the providers for a service type from OSGi environment.
     *
     * @param serviceType        the type of service to load
     * @param containerSciFilter containerSciFilter
     * @return an unmodifiable collection of service providers
     * @throws java.io.IOException if there was a problem loading any service
     */
    public List<T> load(Class<T> serviceType, String containerSciFilter) throws IOException {

        String configFile = SERVICES + serviceType.getName();
        List<T> providers = new ArrayList<T>();


        File pluginsDir = new File(CARBON_DROPINS_DIR_PATH);
        File[] jarFiles = pluginsDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().endsWith(Constants.JAR_EXT)) {
                    return true;
                }
                return false;
            }
        });

        for (File jarFile : jarFiles) {
            String className = getServiceFileContent(jarFile, configFile);
            if (className != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Loading " + className);
                }
                try {
                    Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
                    providers.add((T) clazz.newInstance());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }


        }
        return providers;
    }


    private String getServiceFileContent(File jarFile, String configFile) throws IOException {
        JarFile jar = new JarFile(jarFile);
        JarEntry jarEntry = (JarEntry) jar.getEntry(configFile);
        if (jarEntry != null) {
            if (log.isDebugEnabled()) {
                log.debug(configFile + "found on " + jarFile.getPath());
            }
            InputStream is = jar.getInputStream(jarEntry);
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1024];
            //TODO - Verify , we only read first line only.
            isr.read(buffer, 0, buffer.length);
            String s = new String(buffer);
            return s.trim();
        }
        return null;

    }

}