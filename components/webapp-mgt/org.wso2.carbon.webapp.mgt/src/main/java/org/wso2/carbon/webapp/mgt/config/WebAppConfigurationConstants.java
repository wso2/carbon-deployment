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

import java.nio.file.Paths;

/**
 * This class keeps the some constants related to web app configuration
 */
public class WebAppConfigurationConstants {
    public static final String WEBAPP_DESCRIPTOR_NAME = "wso2as-web.xml";
    public static final String WSO2AS_WEB_XML = Paths.get("WEB-INF", WEBAPP_DESCRIPTOR_NAME).toString();
    public static final String CARBON_HOME = System.getProperty("carbon.home");
    public static final String DEFAULT_WSO2AS_WEB_XML = Paths
            .get(CARBON_HOME, "repository", "conf", "tomcat", WEBAPP_DESCRIPTOR_NAME).toString();
    public static final String NAMESPACE = "http://wso2.org/2015/08/wso2as-web";
    public static final String WSO2AS_WEB_XML_SCHEMA = Paths
            .get(CARBON_HOME, "repository", "conf", "tomcat", "wso2as-web-schema.xsd").toString();

    public final static String ENV_CONFIG_FILE = "webapp-classloading-environments.xml";
    public final static String CL_CONFIG_FILE = "webapp-classloading.xml";
    public final static String APP_CL_CONFIG_FILE = Paths.get("META-INF", CL_CONFIG_FILE).toString();
    public final static String DEFAULT_EXT_DIR = Paths.get(CARBON_HOME, "lib", "runtimes", "ext").toString();
    public final static String SYSTEM_ENV = "Carbon";
    public final static String TOMCAT_ENV = "Tomcat";
    public final static String CXF_ENV = "CXF";
    public final static String JAVAEE_ENV = "Javaee";
}
