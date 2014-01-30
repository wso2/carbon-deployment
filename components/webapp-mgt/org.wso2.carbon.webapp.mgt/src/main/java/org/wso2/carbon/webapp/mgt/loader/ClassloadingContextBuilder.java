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

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.webapp.mgt.utils.XMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for building the ClassloadingConfiguration and WebappClassloadingContext objects based on the
 * webapp-classloading-environments.xml and webapp-classloading.xml files.
 */
public class ClassloadingContextBuilder {
    private static final Log log = LogFactory.getLog(ClassloadingContextBuilder.class);

    public synchronized static ClassloadingConfiguration buildSystemConfig() throws Exception {
        ClassloadingConfiguration classloadingConfig = new ClassloadingConfiguration();

        String carbonHome = System.getProperty("carbon.home");

        String envConfigPath = carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "tomcat" + File.separator + LoaderConstants.ENV_CONFIG_FILE;

        String clConfigPath = carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "tomcat" + File.separator + LoaderConstants.CL_CONFIG_FILE;

        //Loading specified environment from the environment config file.
        File envConfigFile = new File(envConfigPath);
        if (!envConfigFile.exists()) {
            throw new FileNotFoundException(envConfigPath);
        }
        populateEnvironments(classloadingConfig, envConfigFile);

        //Loading classloading policy form the webapp-classloading.xml file.
        File clConfigFile = new File(clConfigPath);
        if (!clConfigFile.exists()) {
            throw new FileNotFoundException(clConfigPath);
        }
        loadClassloadingPolicy(classloadingConfig, clConfigFile);

        return classloadingConfig;
    }

    public static WebappClassloadingContext buildClassloadingContext(String webappFilePath) throws Exception {
        ClassloadingConfiguration classloadingConfig = WebappClassloadingContext.getClassloadingConfig();
        WebappClassloadingContext webappClassloadingContext = new WebappClassloadingContext();

        URL appCLConfigFileURL = getClassloadingConfigFileURL(webappFilePath);

        if (appCLConfigFileURL == null) {

            //Webapp is not specified a custom custom classloading behaviour, hence defualts to the system values.
            webappClassloadingContext.setParentFirst(classloadingConfig.isParentFirst());
            if(classloadingConfig.getEnvironments().length > 0){
                List<String> delegatedPackageList = new ArrayList<String>();
                for(String env : classloadingConfig.getEnvironments()){
                    String[] packs = classloadingConfig.getDelegatedEnvironment(env);
                    if(packs != null && packs.length > 0){
                        for(String pkg : packs){
                            delegatedPackageList.add(pkg);
                        }
                    }
                }

                webappClassloadingContext.setDelegatedPackages(delegatedPackageList.toArray(new String[delegatedPackageList.size()]));
                if(delegatedPackageList.size() == 0){
                    webappClassloadingContext.setDelegatedPackages(
                            classloadingConfig.getDelegatedEnvironment(LoaderConstants.SYSTEM_ENV));
                }


            } else {
                webappClassloadingContext.setDelegatedPackages(
                        classloadingConfig.getDelegatedEnvironment(LoaderConstants.SYSTEM_ENV));
            }

            webappClassloadingContext.setProvidedRepositories(new String[0]);
            return webappClassloadingContext;
        }

        //Webapp contains custom classloading specification.
        Document doc = XMLUtils.buildDocumentFromInputStream(appCLConfigFileURL.openStream());

        //Processing ParentFirst element.
        Node parentFirstNode = doc.getElementsByTagName("ParentFirst").item(0);
        if (parentFirstNode != null) {
            boolean parentFirst = Boolean.parseBoolean(parentFirstNode.getTextContent());
            webappClassloadingContext.setParentFirst(parentFirst);
        } else {
            //Parent-first behaviour is not specified hence using the default values.
            webappClassloadingContext.setParentFirst(classloadingConfig.isParentFirst());
        }

        //Processing Environment element.
        String[] environments;
        Node envNode = doc.getElementsByTagName("Environments").item(0);
        if (envNode != null) {
            String envList = envNode.getTextContent();
            environments = splitStrings(envList, ",");
            environments = addSystemEnvironment(environments);
        } else {
            //Environments are not specified, hence using the default values.
            environments = classloadingConfig.getEnvironments();
        }

        //Populate WebappClassloadingContext data structures using the specified environments.
        List<String> delegatedPkgList = new ArrayList<String>();
        List<String> providedResources = new ArrayList<String>();

        for (String env : environments) {
            if (classloadingConfig.getDelegatedEnvironment(env) != null) {
                Collections.addAll(delegatedPkgList, classloadingConfig.getDelegatedEnvironment(env));

            } else if (classloadingConfig.getExclusiveEnvironment(env) != null) {
                Collections.addAll(providedResources, classloadingConfig.getExclusiveEnvironment(env));

            } else {
                throw new Exception("Undefined environment.");
            }
        }

        // Add default "ext" directory and always load Jars from here.
        String[] defaultExtClasspath= generateClasspath(LoaderConstants.DEFAULT_EXT_DIR + "*.jar" );
        Collections.addAll(providedResources, defaultExtClasspath);

        webappClassloadingContext.setDelegatedPackages(
                delegatedPkgList.toArray(new String[delegatedPkgList.size()]));
        webappClassloadingContext.setProvidedRepositories(
                providedResources.toArray(new String[providedResources.size()]));

        return webappClassloadingContext;
    }

    //TODO Validate the schema.. works for the best case.
    private static void populateEnvironments(ClassloadingConfiguration classloadingConfig,
                                             File envConfigFile) throws Exception {
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

    private static void populateDelegatedEnvironments(ClassloadingConfiguration classloadingConfig, Document doc) {
        NodeList envNodeList = doc.getElementsByTagName("DelegatedEnvironment");
        for (int i = 0; i < envNodeList.getLength(); i++) {
            Node envNode = envNodeList.item(i);
            if (envNode.getNodeType() == Node.ELEMENT_NODE) {
                Element envElement = (Element) envNode;
                String name = envElement.getElementsByTagName("Name").item(0).getTextContent();
                String delegatedPkgs = envElement.getElementsByTagName("DelegatedPackages").item(0).getTextContent();
                String[] delegatedPkgArray = splitStrings(delegatedPkgs, ",");
                classloadingConfig.addDelegatedEnvironment(name, delegatedPkgArray);
            }
        }
    }

    private static void populateExclusiveEnvironments(ClassloadingConfiguration classloadingConfig, Document doc) {
        NodeList envNodeList = doc.getElementsByTagName("ExclusiveEnvironment");
        for (int i = 0; i < envNodeList.getLength(); i++) {
            Node envNode = envNodeList.item(i);
            if (envNode.getNodeType() == Node.ELEMENT_NODE) {
                Element envElement = (Element) envNode;
                String name = envElement.getElementsByTagName("Name").item(0).getTextContent();
                String classpathStr = envElement.getElementsByTagName("Classpath").item(0).getTextContent();
                String[] classpath = generateClasspath(classpathStr);
                classloadingConfig.addExclusiveEnvironment(name, classpath);
            }
        }
    }

    //TODO Validate the schema.. works for the best case. Improve error handling
    private static void loadClassloadingPolicy(ClassloadingConfiguration classloadingConfig,
                                               File clConfigFile) throws Exception {
        Document doc;
        try {
            doc = XMLUtils.buildDocumentFromFile(clConfigFile);

            String parentFirstStr = doc.getElementsByTagName("ParentFirst").item(0).getTextContent();
            boolean parentFirst = Boolean.parseBoolean(parentFirstStr);
            classloadingConfig.setParentFirstBehaviour(parentFirst);

            String envStr = doc.getElementsByTagName("Environments").item(0).getTextContent();
            String[] environments = splitStrings(envStr, ",");
            classloadingConfig.setEnvironments(environments);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("Failed to populate the ClassloadingConfiguration");
        }
    }

    private static String[] splitStrings(String str, String separator) {
        if (str == null || str.trim().length() == 0)
            return new String[0];
        List<String> list = new ArrayList<String>();
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
        List<String> entryList = new ArrayList<String>();

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

                List<String> fileList = new ArrayList<String>();
                getFileList(directory, fileList);
                if (!fileList.isEmpty()){
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

    //TODO improve error handling.
    private static URL getClassloadingConfigFileURL(String webappFilePath) {
        File f = new File(webappFilePath);
        if (f.isDirectory()) {
            File configFile = new File(webappFilePath + File.separator + LoaderConstants.APP_CL_CONFIG_FILE);
            if (configFile.exists()) {
                try {
                    return configFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    //TODO fixme
                }
            }
        } else {
            JarFile webappJarFile = null;
            JarEntry contextXmlFileEntry;
            try {
                webappJarFile = new JarFile(webappFilePath);
                contextXmlFileEntry = webappJarFile.getJarEntry(LoaderConstants.APP_CL_CONFIG_FILE);
                if (contextXmlFileEntry != null) {
                    return new URL("jar:file:" + URLEncoder.encode(webappFilePath, "UTF-8") + "!/" +
                            LoaderConstants.APP_CL_CONFIG_FILE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                //TODO fixme
            } finally {
                if (webappJarFile != null) {
                    try {
                        webappJarFile.close();
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                    }
                }
            }

        }
        return null;
    }

    private static String[] addSystemEnvironment(String[] environments) {
        List<String> envList = new ArrayList<String>(environments.length);
        Collections.addAll(envList, environments);

        /**
         * Add 'Tomcat' also as an environments if
         *
         * 1. specified environments list does not contains 'Tomcat'
         * 2. specified environments list does not contains 'Carbon', Carbon includes Tomcat
         *
         */
        boolean found = false;
        for (String env : envList) {
            if (LoaderConstants.TOMCAT_ENV.equals(env)  || LoaderConstants.SYSTEM_ENV.equals(env)) {
                found = true;
                break;
            }
        }

        if (!found) {
            envList.add(LoaderConstants.TOMCAT_ENV);
        }

        return envList.toArray(new String[envList.size()]);
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
