/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.webapp.mgt;

import java.io.File;

/**
 * Web Application Constants
 */
public final class WebappsConstants {
    public static final String WEBAPP_PREFIX = "webapps";
    public static final String WEBAPP_DEPLOYMENT_FOLDER = "webapps";
    public static final String WEBAPP_EXTENSION = "war";
    public static final String WEBAPP_INFO_JSP_PAGE = "/webapp-list/webapp_info.jsp";
    public static final int MAX_DEPTH = 10;
    public static final String ALL_WEBAPP_FILTER_PROP = "all";
    public static final String WEBAPP_FILTER_PROP = "webapp";
    public static final String JAX_WEBAPP_FILTER_PROP = "jaxWebapp";
    public static final String JAGGERY_WEBAPP_FILTER_PROP = "jaggeryWebapp";
//    public static final String JAX_WEBAPP_REPO = "jaxwebapps";
    public static final String JAGGERY_WEBAPP_REPO = "jaggeryapps";
    public static final int VALVE_INDEX = 0;
    public static final String JAGGERY_APPS_PREFIX = "jaggeryapps";
    public static final String JAX_WEBAPPS_PREFIX = "jaxwebapps";  //todo - remove usages
    public static final String APP_FILE_NAME = "AppFileName";

    public static final String TOMCAT_GENERIC_WEBAPP_DEPLOYER = "tomcatGenericWebappsDeplyer";
    public static final String TOMCAT_JAGGERY_WEBAPP_DEPLOYER = "tomcatJaggeryWebappsDeplyer";
//    public static final String TOMCAT_JAX_WEBAPP_DEPLOYER = "tomcatJaxWebappsDeplyer";

    public static final String WEBAPP_METADATA_DIR = "artifactMetafiles" + File.separator + "webapp";
    public static final String WEBAPP_METADATA_BASE_DIR = "artifactMetafiles";
    public static final String WEBAPP_GROUP_METADATA_DIR = "artifactMetafiles" + File.separator + "webappDefaultVersion";

    public static final String ENABLE_BAM_STATISTICS = "enable.statistics";
    public static final String FAULTY_WEBAPP = "faulty.webapp";


    /**
     * This is to filter out custom webapp types. If a custom webapp deployer is added, it should
     * set this as a property to filer out the custom type.
     */
    public static final String WEBAPP_FILTER = "webappFilter";

    public static final String FWD_SLASH_REPLACEMENT = "#";
    public static final String VERSION_MARKER = "##";
    public static final String DEFAULT_VERSION = "/default";
    public static final String DEFAULT_VERSION_STRING = "0";

    public static final String WEB_APP_DEFAULT_VERSION_SUPPORT = "webapp.defaultversion";
    public static final String WEB_APP_VERSION_DEFAULT_WEBAPP = "webapp.defaultversion.name";

    public static final String KEEP_WEBAPP_METADATA_HISTORY_PARAM = "keepWebappMetadataHistory";
    //Meta data handling strategies
    public  static  final  int KEEP_DEFAULT_VERSION_META_DATA_STRATEGY = 1;

    // ClassLoader improvement related constants.
    public static final java.lang.String CARBON_HOME = "carbon.home";
    public static final java.lang.String ELE_PARENT_FIRST = "ParentFirst";
    public static final java.lang.String ELE_ENVIRONMENTS = "Environments";
    public static final java.lang.String ATT_EXCLUDES = "excludes";
    public static final java.lang.String ELE_NAME = "Name";
    public static final java.lang.String ELE_DELEGATED_PACKAGES = "DelegatedPackages";
    public static final java.lang.String ELE_DELEGATED_RESOURCES = "DelegatedResources";
    public static final java.lang.String ELE_DELEGATED_ENVIRONMENTS = "DelegatedEnvironment";
    public static final java.lang.String ELE_EXCLUSIVE_ENVIRONMENTS = "ExclusiveEnvironment";
    public static final java.lang.String ELE_CLASSPATH= "Classpath";
    public static final java.lang.String EXCLUDES_MERGE_POLICY_UNION = "union";
    public static final java.lang.String EXCLUDES_MERGE_POLICY_INTERSECT = "intersect";
    public static final java.lang.String ENVIRONMENTS_SPILIT_CHAR = ",";

    // Webapp stopped state persistence related constants
    public static final String WEBAPP_RESOURCE_PATH_ROOT = "/repository/webapps/";
    public static final String WEBAPP_STATUS = "Status";

    public static final class WebappState {
        public static final String STARTED = "started";
        public static final String STOPPED = "stopped";
        public static final String ALL = "all";

        private WebappState() {}
    }

    public enum ApplicationOpType {
        STOP, START, RELOAD
    }

    private WebappsConstants() {
    }
}
