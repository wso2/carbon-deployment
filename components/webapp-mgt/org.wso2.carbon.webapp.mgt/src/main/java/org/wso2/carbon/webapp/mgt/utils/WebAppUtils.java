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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAppUtils {

    public static List<String> vhostNames = getVhostNames();
    public static List<String> appBases = getAppBases();

    /**
     * This util method is used to check if the given application is a Jax-RS/WS app
     *
     * @param webApplication application object
     * @return relevant servlet mapping of the cxf servlet if its a Jax-RS/WS application.
     * Null, if its not a Jax-RS/WS application.
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
     * @return absolute path to base dir
     */
    public static String getWebappDirPath(String webappFilePath) {
        return webappFilePath.substring(0, webappFilePath.lastIndexOf(File.separator));
    }

    /**
     * @param filePath web app base dir path
     * @return virtual host name for web app dir
     */
    public static String getMatchingHostName(String filePath) {
        Container[] virtualHosts = findHostChildren();
        for (Container vHost : virtualHosts) {
            Host childHost = (Host) vHost;
            String appBase = childHost.getAppBase().replace("/", File.separator);
            if (appBase.endsWith(File.separator)) {
                appBase = appBase.substring(0, appBase.lastIndexOf(File.separator));
            }
            if (isWebappUploadedToVirtualAppBase(filePath, appBase)) {
                if (childHost.getName().equals(getDefaultHost())) {
                    return getServerConfigHostName();
                }
                return childHost.getName();
            }
        }
        return getServerConfigHostName();
    }

    /**
     * @param webAppBaseDir web application base path
     * @return true if webapp is uploaded to Virtual Host
     */
    private static boolean isWebappUploadedToVirtualAppBase(String webAppBaseDir, String appBase) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String axis2Repo = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        String defaultWebAppPath = Paths.get(axis2Repo, CarbonConstants.WEBAPP_DEPLOYMENT_FOLDER).toString();
        if (webAppBaseDir.equals(defaultWebAppPath)) {
            return false;
        } else {
            String baseDir = appBase.substring(appBase.lastIndexOf(File.separator), appBase.length());
            return (webAppBaseDir.contains(axis2Repo) && webAppBaseDir.endsWith(baseDir));
        }
    }

    /**
     * This will return a key with pair hostname:webappFileName
     *
     * @param webappFile web application file
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
            if (host.getName().equals(getDefaultHost())) {
                //read host name from carbon.xml
                vHosts.add(getServerConfigHostName());
            } else {
                vHosts.add(host.getName());
            }
        }
        return vHosts;
    }

    /**
     * This util method will return appbase value of matching host name from catalina-server.xml
     *
     * @param hostName hostname of Host element
     * @return relevant appBase for the host
     */
    public static String getAppbase(String hostName) {
        if (ServerConfiguration.getInstance().getFirstProperty("HostName") != null &&
                ServerConfiguration.getInstance().getFirstProperty("HostName").equals(hostName)) {
            return getAppbase(getDefaultHost());
        } else {
            Container[] childHosts = findHostChildren();
            for (Container host : childHosts) {
                Host vHost = (Host) host;
                if (vHost.getName().equals(hostName)) {
                    //catalina-server.xml has appBase value with file separators "/"
                    //But in windows environment File.separator should change to "\\"
                    //Hence replacing file.separators
                    return vHost.getAppBase().replace("/", File.separator);
                }
            }
        }
        return "";
    }

    /**
     * @param configurationContext ConfigurationContext instance
     * @return list of web application holders
     */
    public static Map<String, WebApplicationsHolder> getAllWebappHolders(ConfigurationContext configurationContext) {
        return (Map<String, WebApplicationsHolder>) configurationContext.
                getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
    }

    /**
     * This util method will return the web application holder of the given web app file
     *
     * @param webappFilePath AbsolutePath of webapp
     * @param configurationContext ConfigurationContext instance
     * @return relevant webapplication holder
     */
    public static WebApplicationsHolder getWebappHolder(String webappFilePath, ConfigurationContext configurationContext) {
        String baseDir = getWebappDir(webappFilePath);
        Map<String, WebApplicationsHolder> webApplicationsHolderList =
                (Map<String, WebApplicationsHolder>) configurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
        if (!appBases.contains(baseDir)) {
            return getDefaultWebappHolder(configurationContext);
        }
        return webApplicationsHolderList.get(baseDir);
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
     * @param webappFilePath path to webapp
     * @return web application name
     */
    public static String getWebappName(String webappFilePath) {
        return webappFilePath.substring(webappFilePath.lastIndexOf(File.separator) + 1, webappFilePath.length());
    }

    public static WebApplicationsHolder getDefaultWebappHolder(ConfigurationContext configurationContext) {
        return ((Map<String, WebApplicationsHolder>) configurationContext.
                getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST)).get("webapps");
    }

    /**
     * @return default host of engine element
     */
    public static String getDefaultHost() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        if (carbonTomcatService != null) {
            return carbonTomcatService.getTomcat().getEngine().getDefaultHost();
        }
        return null;
    }

    /**
     * This will read "HostName" value from carbon.xml and will return
     * default host (from catalina-server.xml) if the value is null (or not defined)
     *
     * @return host name read from carbon.xml
     */
    public static String getServerConfigHostName() {
        String hostName = ServerConfiguration.getInstance().getFirstProperty("HostName");
        if (hostName == null) {
            return getDefaultHost();
        }
        return hostName;
    }

    private static Container[] findHostChildren() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        return carbonTomcatService.getTomcat().getEngine().findChildren();
    }

    public static List<String> getAppBases() {
        List<String> baseDirs = new ArrayList<String>();
        Container[] childHosts = findHostChildren();
        for (Container host : childHosts) {
            Host vHost = (Host) host;
            //Replacing file separators according to current OS
            String appBase = vHost.getAppBase().replace("/", File.separator);
            if (appBase.endsWith(File.separator)) {
                appBase = appBase.substring(0, appBase.lastIndexOf(File.separator));
            }
            baseDirs.add(appBase.substring(appBase.lastIndexOf(File.separator) + 1, appBase.length()));
        }
        return baseDirs;
    }

    public static String generateMetaFileDirName(String webappFilePath, ConfigurationContext configurationContext) {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(webappFilePath, configurationContext);
        return webApplicationsHolder.getWebappsDir().getName();
    }

    /**
     * Returns the registry path for the webapp
     *
     * @param webApplication WebApplication instance
     * @return
     */
    
    public static String getWebappResourcePath(WebApplication webApplication) {
        String fileName = webApplication.getWebappFile().getName();
        String webappName = fileName.contains("#") ? fileName.substring(0, fileName.indexOf("#")) : fileName;
        
        // remove the extension from the webapp file
        webappName = (webappName.contains(".war") || webappName.contains(".zip")) 
                     ? webappName.substring(0, webappName.indexOf(".war")) : webappName;
        return WebappsConstants.WEBAPP_RESOURCE_PATH_ROOT + webApplication.getHostName() + "/" + webappName
               + webApplication.getVersion();
    }
}
