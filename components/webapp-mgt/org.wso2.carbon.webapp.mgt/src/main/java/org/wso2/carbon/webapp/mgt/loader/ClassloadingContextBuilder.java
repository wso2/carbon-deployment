/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationConstants;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationData;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationReader;
import org.wso2.carbon.webapp.mgt.config.WebAppConfigurationService;
import org.wso2.carbon.webapp.mgt.utils.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Responsible for building WebappClassloadingContext objects based on the configuration
 * of the relevant web application
 */
public class ClassloadingContextBuilder {
    private static final Log log = LogFactory.getLog(ClassloadingContextBuilder.class);

    /**
     * Uses webapp-classloading-environments.xml and builds a structure containing that information
     *
     * @return ClassloadingConfiguration object
     * @throws Exception
     */
    private static ClassloadingConfiguration buildClassloadingEnvironmentConfigStructure() throws Exception {

        ClassloadingConfiguration classloadingConfig = new ClassloadingConfiguration();

        String carbonHome = System.getProperty(WebappsConstants.CARBON_HOME);

        Path envConfigPath = Paths
                .get(carbonHome, "repository", "conf", "tomcat", WebAppConfigurationConstants.ENV_CONFIG_FILE);

        if (!Files.exists(envConfigPath)) {
            throw new FileNotFoundException(envConfigPath.toString());
        }
        populateEnvironments(classloadingConfig, envConfigPath);

        return classloadingConfig;
    }

    /**
     * Builds WebappClassloadingContext for a webapp
     *
     * @param webappFilePath path of the relevant web app
     * @return WebappClassloadingContext object for the web app
     * @throws Exception
     */
    public static WebAppClassloadingContext buildClassloadingContext(String webappFilePath) throws Exception {
        WebAppClassloadingContext webAppClassloadingContext = new WebAppClassloadingContext();

        WebAppConfigurationService webAppConfigurationService = DataHolder.getWebAppConfigurationService();
        WebAppConfigurationData webAppConfigurationData;
        if (webAppConfigurationService != null) {
            webAppConfigurationData = webAppConfigurationService.getConfiguration(webappFilePath);
        } else {
            webAppConfigurationData = WebAppConfigurationReader.retrieveWebConfigData(webappFilePath);
        }

        webAppClassloadingContext.setParentFirst(webAppConfigurationData.isParentFirst());

        List<String> environments = webAppConfigurationData.getEnvironments();

        ClassloadingConfiguration classloadingConfig = buildClassloadingEnvironmentConfigStructure();

        //Populate WebappClassloadingContext data structures using the specified environments.
        List<String> delegatedPkgList = new ArrayList<>();
        List<String> delegatedResourceList = new ArrayList<>();
        List<String> providedResources = new ArrayList<>();

        for (String env : environments) {
            if (classloadingConfig.getDelegatedEnvironment(env) != null) {
                Collections.addAll(delegatedPkgList,
                        classloadingConfig.getDelegatedEnvironment(env).getDelegatedPackageArray());
                Collections.addAll(delegatedResourceList,
                        classloadingConfig.getDelegatedEnvironment(env).getDelegatedResourcesArray());

            } else if (classloadingConfig.getExclusiveEnvironment(env) != null) {
                Collections.addAll(providedResources,
                        classloadingConfig.getExclusiveEnvironment(env).getDelegatedPackageArray());

            } else {
                throw new Exception("Undefined environment.");
            }
        }

        // Add default "ext" directory and always load Jars from here.
        String[] defaultExtClasspath = generateClasspath(WebAppConfigurationConstants.DEFAULT_EXT_DIR + "*.jar");
        Collections.addAll(providedResources, defaultExtClasspath);

        webAppClassloadingContext.setDelegatedPackages(delegatedPkgList.toArray(new String[delegatedPkgList.size()]));
        webAppClassloadingContext
                .setDelegatedResources(delegatedResourceList.toArray(new String[delegatedResourceList.size()]));
        webAppClassloadingContext
                .setProvidedRepositories(providedResources.toArray(new String[providedResources.size()]));

        return webAppClassloadingContext;
    }

    /**
     * Populates the ClassloadingConfiguration instance with delegated environments
     *
     * @param classloadingConfig The instance of ClassloadingConfiguration to be populated
     * @param doc                the document built from webapp-classloading-environments.xml
     */
    private static void populateDelegatedEnvironments(ClassloadingConfiguration classloadingConfig, Document doc) {
        NodeList envNodeList = doc.getElementsByTagName(WebappsConstants.ELE_DELEGATED_ENVIRONMENTS);
        for (int i = 0; i < envNodeList.getLength(); i++) {
            Node envNode = envNodeList.item(i);
            if (envNode.getNodeType() == Node.ELEMENT_NODE) {
                Element envElement = (Element) envNode;
                String name = envElement.getElementsByTagName(WebappsConstants.ELE_NAME).item(0).getTextContent();

                String delegatedPkgs = envElement.getElementsByTagName(WebappsConstants.ELE_DELEGATED_PACKAGES).item(0)
                        .getTextContent();
                String delegatedResources = envElement.getElementsByTagName(WebappsConstants.ELE_DELEGATED_RESOURCES)
                        .item(0).getTextContent();

                String[] delegatedPkgArray = splitStrings(delegatedPkgs, WebappsConstants.ENVIRONMENTS_SPILIT_CHAR);
                String[] delegatedResArray = splitStrings(delegatedResources,
                        WebappsConstants.ENVIRONMENTS_SPILIT_CHAR);

                CLEnvironment environment = new CLEnvironment(delegatedPkgArray, delegatedResArray);
                classloadingConfig.addDelegatedEnvironment(name, environment);

            }
        }
    }

    /**
     * Populates the ClassloadingConfiguration instance with environments reading webapp-classloading-environments.xml
     *
     * @param classloadingConfig The instance of ClassloadingConfiguration to be populated
     * @param envConfigPath      Path to webapp-classloading-environments.xml
     * @throws Exception
     */
    private static void populateEnvironments(ClassloadingConfiguration classloadingConfig, Path envConfigPath)
            throws Exception {
        Document doc;
        try {
            doc = XMLUtils.buildDocumentFromFile(envConfigPath);
            populateDelegatedEnvironments(classloadingConfig, doc);
            populateExclusiveEnvironments(classloadingConfig, doc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("Failed to populate the ClassloadingConfiguration");
        }
    }

    /**
     * Populates the ClassloadingConfiguration instance with exclusive environments
     *
     * @param classloadingConfig The instance of ClassloadingConfiguration to be populated
     * @param doc                the document built from webapp-classloading-environments.xml
     */
    private static void populateExclusiveEnvironments(ClassloadingConfiguration classloadingConfig, Document doc)
            throws IOException {
        NodeList envNodeList = doc.getElementsByTagName(WebappsConstants.ELE_EXCLUSIVE_ENVIRONMENTS);
        for (int i = 0; i < envNodeList.getLength(); i++) {
            Node envNode = envNodeList.item(i);
            if (envNode.getNodeType() == Node.ELEMENT_NODE) {
                Element envElement = (Element) envNode;
                String name = envElement.getElementsByTagName(WebappsConstants.ELE_NAME).item(0).getTextContent();
                String classpathStr = envElement.getElementsByTagName(WebappsConstants.ELE_CLASSPATH).item(0)
                        .getTextContent();
                String[] classpath = generateClasspath(classpathStr);
                CLEnvironment environment = new CLEnvironment(classpath, null);
                classloadingConfig.addExclusiveEnvironment(name, environment);
            }
        }
    }

    /**
     * Splits a string using the passed separator
     *
     * @param str       The string to be split
     * @param separator The separator to be used when splitting
     * @return The String array of split components
     */
    private static String[] splitStrings(String str, String separator) {
        if (str == null || str.trim().length() == 0)
            return new String[0];
        List<String> list = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(str, separator);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (token.length() != 0)
                list.add(token);
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Adds all the jar files inside the passed directory to the passed List
     *
     * @param directory The directory from where the jars need to be collected
     * @param fileList  The list to which the jars should be added
     */
    private static void getFileList(File directory, List<String> fileList) {
        if (directory.exists()) {
            String[] itemList = directory.list();
            if (itemList != null) {
                for (String fileName : itemList) {
                    File file = new File(directory, fileName);
                    if (file.exists()) {
                        //If file is a single Jar file.
                        if (file.isFile() && fileName.endsWith(".jar")) {
                            fileList.add(file.toURI().toString());

                        } else if (file.isDirectory()) {
                            // If file is a directory.
                            File nastedDir = new File(directory, fileName);
                            getFileList(nastedDir, fileList);
                        }
                    }

                }
            }
        }

    }

    private static String[] generateClasspath(String classpathStr) throws IOException {
        StringTokenizer tkn = new StringTokenizer(classpathStr, ";");
        List<String> entryList = new ArrayList<>();

        while (tkn.hasMoreTokens()) {
            String token = tkn.nextToken().trim();
            if (token.isEmpty()) {
                continue;
            }

            token = Utils.replaceSystemProperty(token);

            if (token.endsWith("*.jar")) {
                token = token.substring(0, token.length() - "*.jar".length());

                Path path = Paths.get(token);
                if (!Files.isDirectory(path)) {
                    continue;
                }

                List<String> fileList = new ArrayList<>();
                getFileList(new File(token), fileList);
                if (!fileList.isEmpty()) {
                    entryList.addAll(fileList);
                }

            } else {
                // single file or directory
                Path path = Paths.get(token);
                if (!Files.exists(path)) {
                    continue;
                }
                entryList.add(path.toUri().toString());
            }
        }

        Collections.sort(entryList);

        if (log.isDebugEnabled()) {
            for (String s : entryList) {
                log.debug("Classpath Entry : " + s);

            }
        }

        return entryList.toArray(new String[entryList.size()]);
    }
}
