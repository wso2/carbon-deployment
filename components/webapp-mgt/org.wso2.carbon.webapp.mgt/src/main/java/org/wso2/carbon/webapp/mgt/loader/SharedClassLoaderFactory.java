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

import org.apache.catalina.startup.ClassLoaderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The factory creates class loaders for each run time environment.
 */
public class SharedClassLoaderFactory {

    private static final Log log = LogFactory.getLog(SharedClassLoaderFactory.class);
    private static SharedClassLoaderFactory instance = new SharedClassLoaderFactory();
    private URLClassLoader sharedClassLoader;
    private Map<String, URLClassLoader> environmentClassLoaders = new HashMap<>();

    private SharedClassLoaderFactory() {

    }

    /**
     * Get the instance of the SharedClassLoaderFactory.
     *
     * @return  SharedClassLoaderFactory
     */
    public static SharedClassLoaderFactory getInstance() {

        return instance;
    }

    /**
     * Initialize the shared class loader and environment class loaders for each defined environments
     * in the first webapp deployment.
     *
     * @param classloadingConfig    Class loading configuration.
     */
    public void init(ClassloadingConfiguration classloadingConfig) {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        sharedClassLoader = new SharedURLClassLoader(new URL[0], contextClassLoader);
        classloadingConfig.getExclusiveEnvironments().forEach(
                ((name, clEnvironment) -> createClassloader(name, clEnvironment, contextClassLoader)));
    }

    private void createClassloader(String name, CLEnvironment environment, ClassLoader parentClassLoader) {

        if (log.isDebugEnabled()) {
            log.debug("Creating shared class loader for " + name);
        }
        List<ClassLoaderFactory.Repository> repositories = new ArrayList<>();
        if (environment == null || environment.getDelegatedPackageArray() == null) {
            return;
        }
        for (String repository : environment.getDelegatedPackageArray()) {
            if (environmentClassLoaders.containsKey(name)) {
                continue;
            }
            if (repository.endsWith("*.jar")) {
                repository = repository.substring(0, repository.length() - "*.jar".length());
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.URL));
            } else if (repository.endsWith("/*")) {
                repository = repository.substring(0, repository.length() - 2);
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.DIR));
            } else {
                repositories.add(new ClassLoaderFactory.Repository(repository, ClassLoaderFactory.RepositoryType.DIR));
            }
        }

        try {
            ClassLoader classloader = ClassLoaderFactory.createClassLoader(repositories, parentClassLoader);
            environmentClassLoaders.put(name, (URLClassLoader) classloader);
        } catch (Exception e) {
            log.error("Error while creating class loader for " + name, e);
        }
    }

    /**
     * Get the shared class loader for the complete deployment. This shared class loader will loop through the
     * environment class loaders to load the resource.
     *
     * @return  URLClassLoader
     */
    public URLClassLoader getSharedClassLoader() {

        return sharedClassLoader;
    }

    /**
     * Get the shared class loader loader for a specific environment.
     *
     * @param environmentName  Environment Name.
     * @return      URLClassLoader.
     */
    public URLClassLoader getEnvironmentClassLoader(String environmentName) {

        return environmentClassLoaders.get(environmentName);
    }
}
