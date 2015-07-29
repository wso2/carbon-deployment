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

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
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

        String envConfigPath = carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "tomcat" + File.separator + WebAppConfigurationConstants.ENV_CONFIG_FILE;

        //Loading specified environment from the environment config file.
        File envConfigFile = new File(envConfigPath);
        if (!envConfigFile.exists()) {
            throw new FileNotFoundException(envConfigPath);
        }
        populateEnvironments(classloadingConfig, envConfigFile);

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
            webAppConfigurationData = webAppConfigurationService.getConfig(webappFilePath);
        } else {
            webAppConfigurationData = WebAppConfigurationReader.retrieveWebConfigData(webappFilePath);
        }

        webAppClassloadingContext.setParentFirst(webAppConfigurationData.isParentFirst());

        List<String> environments = webAppConfigurationData.getEnvironments();
        //webAppClassloadingContext.setEnvironments(environments.toArray(new String[environments.size()]));

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

    //TODO Validate the schema.. works for the best case.
    private static void populateEnvironments(ClassloadingConfiguration classloadingConfig, File envConfigFile)
            throws Exception {
        Document doc;
        try {
            doc = XMLUtils.buildDocumentFromFile(envConfigFile);
            populateDelegatedEnvironments(classloadingConfig, doc);
            populateExclusiveEnvironments(classloadingConfig, doc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("Failed to populate the ClassloadingConfiguration");
        }
    }

    private static void populateExclusiveEnvironments(ClassloadingConfiguration classloadingConfig, Document doc) {
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

    private static String[] generateClasspath(String classpathStr) {
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

                File directory = new File(token);
                if (!directory.isDirectory()) {
                    continue;
                }

                List<String> fileList = new ArrayList<>();
                getFileList(directory, fileList);
                if (!fileList.isEmpty()) {
                    entryList.addAll(fileList);
                }

            } else {
                // single file or directory
                File file = new File(token);
                if (!file.exists()) {
                    continue;
                }
                entryList.add(file.toURI().toString());
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

    private static void getFileList(File directory, List fileList) {
        if (directory.exists()) {
            for (String fileName : directory.list()) {
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
