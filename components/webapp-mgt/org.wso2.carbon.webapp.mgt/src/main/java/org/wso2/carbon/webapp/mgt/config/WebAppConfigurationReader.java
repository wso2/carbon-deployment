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

package org.wso2.carbon.webapp.mgt.config;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.sso.WebappSSOConstants;
import org.wso2.carbon.webapp.mgt.utils.WebAppConfigurationUtils;
import org.wso2.carbon.webapp.mgt.utils.XMLUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is responsible for reading configuration files.
 * If both wso2as-web.xml and webapp-classloading.xml are present, priority is given
 * to wso2as-web.xml. If only one of them is present that will be used.
 * If neither of them is present, default configuration is used.
 */
public class WebAppConfigurationReader {
    private static final Log log = LogFactory.getLog(WebAppConfigurationProcessingListener.class);

    public static WebAppConfigurationData retrieveWebConfigData(String webappFilePath) {
        return retrieveWebConfigData(webappFilePath, null);
    }

    public static WebAppConfigurationData retrieveWebConfigData(StandardContext context) {
        return retrieveWebConfigData(null, context);
    }

    /**
     * Given the path to a webapp this method returns a WebAppConfigData object which
     * contains the configuration information of the web app
     *
     * @param webAppFilePath Path to the web app
     * @return WebAppConfigData object
     */
    private static WebAppConfigurationData retrieveWebConfigData(String webAppFilePath, StandardContext context) {
        WebAppConfigurationData configData = null;
        /*
        This object contains default configuration information inside wso2as-web.xml
        inside AS
        */
        WebAppConfigurationData defaultConfigData = readConfigFile(WebAppConfigurationConstants.DEFAULT_WSO2AS_WEB_XML);
        try {
            if (webAppFilePath == null) {
                webAppFilePath = WebAppConfigurationUtils.getWebAppFilePath(context);
            }
            URL configFileURL = getConfigFileURL(webAppFilePath, WebAppConfigurationConstants.WSO2AS_WEB_XML);
            if (configFileURL != null) {
                try(InputStream inputStream = configFileURL.openStream()) {
                    configData = JAXBUnmarshaller.unmarshall(inputStream);
                }
            } else {
                configFileURL = getConfigFileURL(webAppFilePath, WebAppConfigurationConstants.APP_CL_CONFIG_FILE);
                if (configFileURL != null) {
                    configData = readOldConfigurationFiles(configFileURL, context);
                }
            }
            //Either way merge configData and defaultConfigData giving priority
            //to configData which contains configuration information specified by the
            //webapp developer
            configData = merge(configData, defaultConfigData);

            //Adding the system environment
            List<String> environments = configData.getEnvironments();
            String[] env = environments.toArray(new String[environments.size()]);
            env = addSystemEnvironment(env);
            configData.setEnvironments(Arrays.asList(env));

        } catch (JAXBException | ParserConfigurationException | SAXException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return configData;
    }

    /**
     * Returns the URL of the configuration file for the passed config file name
     *
     * @param webappFilePath   Path to web app
     * @param configFilePrefix the config file name
     * @return The URL of the configuration file
     * @throws IOException
     */
    private static URL getConfigFileURL(String webappFilePath, String configFilePrefix) throws IOException {
        Path path = Paths.get(webappFilePath);
        if (Files.isDirectory(path)) {
            Path configFilePath = Paths.get(webappFilePath, configFilePrefix);
            if (Files.exists(configFilePath)) {
                return configFilePath.toUri().toURL();
            }

        } else {
            JarEntry contextXmlFileEntry;
            try (JarFile webappJarFile = new JarFile(webappFilePath)) {
                contextXmlFileEntry = webappJarFile.getJarEntry(configFilePrefix);
                if (contextXmlFileEntry != null) {
                    return new URL("jar:file:" + URLEncoder.encode(webappFilePath, "UTF-8").replace("+", "%20") + "!/" +
                            configFilePrefix);
                }
            }

        }
        return null;
    }

    /**
     * Reads the passed config file and returns a WebAppConfigData object
     *
     * @param webappFilePath Path to the webapp
     * @return WebAppConfigData object
     */
    private static WebAppConfigurationData readConfigFile(String webappFilePath) {
        WebAppConfigurationData configData = null;
        try (FileInputStream inputStream = new FileInputStream(webappFilePath)) {
            configData = JAXBUnmarshaller.unmarshall(inputStream);
        } catch (IOException e) {
            log.error("Error while reading" + webappFilePath + ". " + e.getMessage(), e);
        } catch (JAXBException e) {
            log.error("Error while unmarshalling " + WebAppConfigurationConstants.WEBAPP_DESCRIPTOR_NAME + ". " + e
                    .getMessage(), e);
        }
        return configData;
    }

    /**
     * Merges two WebAppConfigData objects giving priority to priority1obj
     *
     * @param priority1obj contains web app specific config values
     * @param priority2obj contains default config values
     * @return Returns the merged object
     */
    public static WebAppConfigurationData merge(WebAppConfigurationData priority1obj,
            WebAppConfigurationData priority2obj) {
        if (priority1obj != null && priority2obj != null) {
            if (priority1obj.getClassloading() == null) {
                priority1obj.setClassloading(priority2obj.getClassloading());
            } else if (priority1obj.getEnvironments() == null) {
                //if classloading is not null but environments are null that means
                //parentFirst must have been set.
                priority1obj.setEnvironments(priority2obj.getEnvironments());
            }
            if (priority1obj.getRestWebServices() == null) {
                priority1obj.setRestWebServices(priority2obj.getRestWebServices());
            }
            if (priority1obj.getStatisticsPublisher() == null) {
                priority1obj.setStatisticsPublisher(priority2obj.getStatisticsPublisher());
            }
            return priority1obj;
        } else if (priority1obj == null) {
            return priority2obj;
        } else {
            return priority1obj;
        }
    }

    /**
     * Used when wso2-web.xml is not inside a webapp but old configuration file is there
     * This is just to make sure old web apps can be deployed in the new version of AS
     *
     * @param appCLConfigFileURL URL to webapp-classloading.xml of the web app
     * @return WebAppConfigData object
     * @throws ParserConfigurationException, SAXException, IOException
     */

    private static WebAppConfigurationData readOldConfigurationFiles(URL appCLConfigFileURL, Context context)
            throws ParserConfigurationException, SAXException, IOException {

        WebAppConfigurationData webAppConfigurationData = new WebAppConfigurationData();

        addClassloadingConfiguration(appCLConfigFileURL, webAppConfigurationData);
        if (context != null) {
            addSingleSignOnConfiguration(webAppConfigurationData, context);
        }

        return webAppConfigurationData;
    }

    /**
     * Sets single sign on configuration
     *
     * @param webAppConfigurationData WebAppConfigData object in which the sso configuration needs to be set
     * @param context                 Web app context
     * @return returns the WebAppConfigData object
     */
    private static WebAppConfigurationData addSingleSignOnConfiguration(WebAppConfigurationData webAppConfigurationData,
            Context context) {
        String ssoEnabled = context.findParameter(WebappSSOConstants.ENABLE_SAML2_SSO);
        if (ssoEnabled != null) {
            webAppConfigurationData.setSingleSignOnEnabled(Boolean.parseBoolean(ssoEnabled));
        }
        return webAppConfigurationData;
    }

    /**
     * This sets classloading configuration in the passed WebAppConfigData object
     * using webapp-classloading.xml
     *
     * @param appCLConfigFileURL      URL to webapp-classloading.xml of the web app
     * @param webAppConfigurationData WebAppConfigData object in which Classloading configuration should be set
     * @throws IOException, SAXException, ParserConfigurationException
     */
    private static void addClassloadingConfiguration(URL appCLConfigFileURL,
            WebAppConfigurationData webAppConfigurationData)
            throws IOException, SAXException, ParserConfigurationException {

        try (InputStream inputStream = appCLConfigFileURL.openStream()) {
            Document doc = XMLUtils.buildDocumentFromInputStream(inputStream);

            //Processing ParentFirst element.
            Node parentFirstNode = doc.getElementsByTagName(WebappsConstants.ELE_PARENT_FIRST).item(0);
            if (parentFirstNode != null) {
                boolean parentFirst = Boolean.parseBoolean(parentFirstNode.getTextContent());
                webAppConfigurationData.setParentFirst(parentFirst);
            }

            //Processing Environment element.
            String[] environments;
            Node envNode = doc.getElementsByTagName(WebappsConstants.ELE_ENVIRONMENTS).item(0);
            if (envNode != null) {
                String envList = envNode.getTextContent();
                environments = splitStrings(envList, WebappsConstants.ENVIRONMENTS_SPILIT_CHAR);
                webAppConfigurationData.setEnvironments(Arrays.asList(environments));
            }
        }
    }

    /**
     * Adds the system envorimnet to the passed array
     *
     * @param existingClassLoadingEnvironments The array of environments to which the system environemnt needs to be added
     * @return array with system environment
     */
    private static String[] addSystemEnvironment(String[] existingClassLoadingEnvironments) {
        List<String> envList = new ArrayList<>(existingClassLoadingEnvironments.length);
        Collections.addAll(envList, existingClassLoadingEnvironments);

        /**
         * Add 'Tomcat' also as an environments if
         *
         * 1. specified environments list does not contains 'Tomcat'
         * 2. specified environments list does not contains 'Carbon', Carbon includes Tomcat
         *
         */
        boolean found = false;
        for (String env : envList) {
            if (WebAppConfigurationConstants.TOMCAT_ENV.equals(env) || WebAppConfigurationConstants.SYSTEM_ENV
                    .equals(env) || WebAppConfigurationConstants.JAVAEE_ENV.equals(env)) {
                found = true;
                break;
            }
        }

        if (!found) {
            envList.add(WebAppConfigurationConstants.TOMCAT_ENV);
        }

        return envList.toArray(new String[envList.size()]);
    }

    /**
     * Splits the passed string using the passed separator
     *
     * @param str       The string to be split
     * @param separator The specified separator
     * @return The array containing the parts of the split string
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

}

