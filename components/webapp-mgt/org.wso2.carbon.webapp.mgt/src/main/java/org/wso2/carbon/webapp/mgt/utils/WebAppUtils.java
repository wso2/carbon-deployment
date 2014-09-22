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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardWrapper;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAppUtils {

    /**
     * This util method is used to check if the given application is a Jax-RS/WS app
     *
     * @param webApplication application object
     * @return relevant servlet mapping of the cxf servlet if its a Jax-RS/WS application.
     *         Null, if its not a Jax-RS/WS application.
     */
    public static String checkJaxApplication(WebApplication webApplication) {
        for (Container container : webApplication.getContext().findChildren()) {
            if (((StandardWrapper) container).getServletClass().equals(
                    "org.apache.cxf.transport.servlet.CXFServlet"))
                return (((StandardWrapper) container).findMappings())[0];
            else if (((StandardWrapper) container).getServletName().toLowerCase().contains("cxf") || "JAXServlet".equals(((StandardWrapper) container).getServletName())) {
                return (((StandardWrapper) container).findMappings())[0];
            }
        }
        return null;
    }

    public static boolean validateWebappFileName(String filename) {
        Pattern pattern = Pattern.compile(".*[\\]\\[!\"$%&'()*+,/:;<=>?@~{|}^`].*");
        Matcher matcher = pattern.matcher(filename);
        boolean isMatch = matcher.matches();
        return isMatch;
    }

    /**
     * This util method is used to return file path of base dir. eg: for input "/deployment/server/webapps/calenda.war"
     * will return "/deployment/server/webapps"
     *
     * @param webappFilePath path to webapp
     * @return  absolute path to base dir
     */
    public static String getWebappDirPath(String webappFilePath) {
        return webappFilePath.substring(0, webappFilePath.lastIndexOf(File.separator));
    }

    /**
     *
     * @param filePath web aoo base dir path
     * @return  virtual host name for web app dir
     */
    public static String getMatchingHostName(String filePath) {
        Container[] childHosts = findHostChildren();
        for (Container host : childHosts) {
            Host vHost = (Host) host;
            String appBase = vHost.getAppBase();
            if (appBase.endsWith(File.separator)) {
                appBase = appBase.substring(0, appBase.lastIndexOf(File.separator));
            }
            if (appBase.equals(filePath)) {
                return vHost.getName();
            }

        }
        return getDefaultHost();
    }

    /**
     * This will return a key with pair hostname:webappFileName
     *
     * @param webappFile
     * @return <hostname>:<webapp-name>
     */
    public static String getWebappKey(File webappFile) {
        String baseDir = getWebappDirPath(webappFile.getAbsolutePath());
        String hostName = getMatchingHostName(baseDir);
        return hostName + ":" + webappFile.getName();
    }

    /**
     * @return List of virtual hosts
     */

    public static List<String> getVhostNames() {
        List<String> vHosts = new ArrayList<String>();
        Container[] childHosts = findHostChildren();
        for (Container vHost : childHosts) {
            Host host = (Host) vHost;
            vHosts.add(host.getName());
        }
        return vHosts;
    }

    /**
     * This util method will return appbase value of matching host name from catalina-server.xml
     *
     * @param hostName
     * @return
     */
    public static String getAppbase(String hostName) {
        String appBase = "";
        Container[] childHosts = findHostChildren();
        for (Container host : childHosts) {
            Host vHost = (Host) host;
            if (vHost.getName().equals(hostName)) {
                appBase = vHost.getAppBase();
                break;
            }
        }
        return appBase;
    }

    /**
     * This util method will return the web application holder of the given web app file
     *
     * @param webappFilePath
     * @param configurationContext
     * @return
     */
    public static WebApplicationsHolder getwebappHolder(String webappFilePath, ConfigurationContext configurationContext) {
        String baseDir = getWebappDir(webappFilePath);
        Map<String, WebApplicationsHolder> webApplicationsHolderList =
                (Map<String, WebApplicationsHolder>) configurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
        WebApplicationsHolder webApplicationsHolder = webApplicationsHolderList.get(baseDir);
        return webApplicationsHolder;
    }

    /**
     * This util method is used to return base dir name of webapp. eg: for input "/deployment/server/webapps/calenda.war"
     * will return "webapps"
     *
     * @param webappFilePath path to web app
     * @return parent dir of web application
     */
    public static String getWebappDir(String webappFilePath) {
        String baseDir = getWebappDirPath(webappFilePath);
        return baseDir.substring(baseDir.lastIndexOf(File.separator) + 1, baseDir.length());
    }

    /**
     *
     * @param webappFilePath path to webapp
     * @return web application name
     */
    public static String getWebappName(String webappFilePath) {
        String webappName = webappFilePath.substring(webappFilePath.lastIndexOf(File.separator) + 1, webappFilePath.length());
        return webappName;
    }

    /**
     *
     * @param configurationContext
     * @return list of web application holders
     */
    public static Map<String, WebApplicationsHolder> getWebapplicationHolders(ConfigurationContext configurationContext) {
        return (Map<String, WebApplicationsHolder>) configurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
    }

    /**
     * @return default host of engine element
     */
    public static String getDefaultHost() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        return carbonTomcatService.getTomcat().getEngine().getDefaultHost();
    }

    private static Container[] findHostChildren() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        return carbonTomcatService.getTomcat().getEngine().findChildren();
    }
}
