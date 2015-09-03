/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DataPaginator;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.WebappsConstants.ApplicationOpType;
import org.wso2.carbon.webapp.mgt.sync.ApplicationSynchronizeRequest;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;
import org.wso2.carbon.webapp.mgt.version.AppVersionGroupPersister;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Admin service for managing webapps
 */
@SuppressWarnings("unused")
public class WebappAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(WebappAdmin.class);
    private static Boolean defaultVersionManagement = null;
    private Map<String, Boolean> versioningConfiguration = new HashMap<String, Boolean>();
    /*
      This map contains - MAP <Web-application-group-name , original-file-name-of-current-default-version> mapping.

      This is useful when reverting current default version into it's original version.

      e.g - app => app#4.war
            mvcapp => mvcapp#2.war
     */
    private Map<String, String> appGroupToCurrentVersionMap = null;


    public WebappAdmin() {
    }

    public WebappAdmin(AxisConfiguration axisConfig) throws Exception {
        super(axisConfig);
    }

    /**
     * Get a page of started webapps
     *
     * @param webappSearchString Search string
     * @param webappState        State of the webapp.
     *                           Can be either WebappsConstants.WebappState.STARTED or
     *                           WebappsConstants.WebappState.STOPPED
     * @param webappType         application type
     * @param pageNumber         The pageNumber of the page to be fetched
     * @return WebappsWrapper
     * @throws AxisFault
     */
    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState, String webappType,
                                                 int pageNumber) throws AxisFault {
        if (webappState.equalsIgnoreCase(WebappsConstants.WebappState.STARTED)) {
            return getPagedWebapps(pageNumber,
                    getStartedWebapps(webappType, webappSearchString), webappType);
        } else if (webappState.equalsIgnoreCase(WebappsConstants.WebappState.STOPPED)) {
            return getPagedWebapps(pageNumber,
                    getStoppedWebapps(webappType, webappSearchString), webappType);
        } else if (webappState.equalsIgnoreCase(WebappsConstants.WebappState.ALL)) {
            Map<String, VersionedWebappMetadata> startedWebapps = getStartedWebapps(webappType, webappSearchString);
            Map<String, VersionedWebappMetadata> stoppedWebapps = getStoppedWebapps(webappType, webappSearchString);

            Map<String, VersionedWebappMetadata> webapps =
                    new ConcurrentHashMap<String, VersionedWebappMetadata>(startedWebapps);

            for (String versionGroup : stoppedWebapps.keySet()) {
                if (webapps.containsKey(versionGroup)) {
                    for (WebappMetadata webappMetadata : stoppedWebapps.get(versionGroup).getVersionGroups()) {
                        webapps.get(versionGroup).addWebappVersion(webappMetadata);
                    }
                } else {
                    webapps.put(versionGroup, stoppedWebapps.get(versionGroup));
                }
            }
            return getPagedWebapps(pageNumber, webapps, webappType);
        } else {
            throw new AxisFault("Invalid webapp state: ", webappState);
        }
    }

    /**
     * Get the details of a deplyed webapp
     * @param webappFileName
     * @param hostName
     * @return meta data for webapp
     */
    public WebappMetadata getStartedWebapp(String webappFileName, String hostName) {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        WebappMetadata webappMetadata = null;
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            WebApplication webApplication = webApplicationsHolder.getStartedWebapps().get(webappFileName);
            if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                webappMetadata = getWebapp(webApplication);
                webappMetadata.setStarted(true);
                break;
            }
        }

        return webappMetadata;
    }

    /**
     * Get the details of an stopped webapp
     * @param webappFileName
     * @param hostName
     * @return meta data for webapp
     */
    public WebappMetadata getStoppedWebapp(String webappFileName, String hostName) {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        WebappMetadata webappMetadata = null;
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            WebApplication webApplication = webApplicationsHolder.getStoppedWebapps().get(webappFileName);
            if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                webappMetadata = getWebapp(webApplication);
                webappMetadata.setStarted(false);
                break;
            }
        }
        return webappMetadata;
    }

    //TODO WSAS-2125
    private void setServiceListPath(WebApplication webApplication) {
        String serviceListPathParamName = "service-list-path";
        String serviceListPathParam =
                webApplication.getContext().getServletContext().getInitParameter(serviceListPathParamName);
        if (serviceListPathParam == null || "".equals(serviceListPathParam)) {
            Map<String, ? extends ServletRegistration> servletRegs =
                    webApplication.getContext().getServletContext().getServletRegistrations();
            for (ServletRegistration servletReg : servletRegs.values()) {
                serviceListPathParam = servletReg.getInitParameter(serviceListPathParamName);
                if (serviceListPathParam != null || !"".equals(serviceListPathParam) ) {
                    break;
                }
            }
        }
        if (serviceListPathParam == null || "".equals(serviceListPathParam)) {
            serviceListPathParam = "/services";
        } else {
            serviceListPathParam = "";
        }
        webApplication.setServiceListPath(serviceListPathParam);
    }

    private WebappMetadata getWebapp(WebApplication webApplication) {
        WebappMetadata webappMetadata;
        webappMetadata = new WebappMetadata();

        String appContext = WebAppUtils.checkJaxApplication(webApplication);
        if(appContext != null){
            webApplication.setProperty(WebappsConstants.WEBAPP_FILTER, WebappsConstants.JAX_WEBAPP_FILTER_PROP);
            setServiceListPath(webApplication);
        }
        if (appContext == null) {
            appContext = "/";
        } else if (appContext.endsWith("/*")) {
            appContext = appContext.substring(0, appContext.indexOf("/*"));
        }

        //Check if webapp is deployed from a CApp
        Path webAppPath = Paths.get(webApplication.getWebappFile().getAbsolutePath());
        if (webAppPath != null) {
            String tenantId = AppDeployerUtils.getTenantIdString();
            // Check whether there is an application in the system from the given name
            ArrayList<CarbonApplication> appList = DataHolder.getApplicationManager().getCarbonApps(tenantId);
            for (CarbonApplication application : appList) {
                Path cappPath = Paths.get(application.getExtractedPath());
                if (webAppPath.startsWith(cappPath)) {
                    webappMetadata.setCAppArtifact(true);
                    break;
                }
            }
        }
        webappMetadata.setDisplayName(webApplication.getDisplayName());
        webappMetadata.setContext(webApplication.getContextName());
        webappMetadata.setHostName(webApplication.getHostName());
        webappMetadata.setWebappKey(WebAppUtils.getWebappKey(webApplication.getWebappFile()));
        webappMetadata.setServletContext(appContext);
        webappMetadata.setLastModifiedTime(webApplication.getLastModifiedTime());
        webappMetadata.setWebappFile(webApplication.getWebappFile().getName());
        webappMetadata.setState(webApplication.getState());
        webappMetadata.setServiceListPath(webApplication.getServiceListPath());
        webappMetadata.setAppVersion(webApplication.getVersion());
        webappMetadata.setContextPath(webApplication.getContext().getPath());
        WebApplication.Statistics statistics = webApplication.getStatistics();
        WebappStatistics stats = new WebappStatistics();
        stats.setActiveSessions(statistics.getActiveSessions());
        stats.setAvgSessionLifetime(statistics.getAvgSessionLifetime());
        stats.setExpiredSessions(statistics.getExpiredSessions());
        stats.setMaxActiveSessions(statistics.getMaxActiveSessions());
        stats.setMaxSessionInactivityInterval(statistics.getMaxSessionInactivityInterval());
        stats.setMaxSessionLifetime(statistics.getMaxSessionLifetime());
        stats.setRejectedSessions(statistics.getRejectedSessions());

        webappMetadata.setStatistics(stats);
        webappMetadata.setWebappType((String) webApplication.getProperty(WebappsConstants.WEBAPP_FILTER));

        return webappMetadata;
    }

    /**
     * @param webappSearchString
     * @param webappType         application type
     * @param pageNumber
     * @return
     * @throws AxisFault
     */
    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       String webappType,
                                                       int pageNumber) throws AxisFault {
        return getPagedWebapps(pageNumber, getFaultyWebapps(webappSearchString), webappType);
    }

    private WebappsWrapper getPagedWebapps(int pageNumber, Map<String, VersionedWebappMetadata> webapps, String webappType) {
        List<VersionedWebappMetadata> webappsList = new ArrayList<VersionedWebappMetadata>(webapps.values());
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        WebappsWrapper webappsWrapper = getWebappsWrapper(webApplicationsHolderMap, webappsList, webappType);
        try {
            webappsWrapper.setHostName(NetworkUtils.getLocalHostname());
        } catch (SocketException e) {
            log.error("Error occurred while getting local hostname", e);
        }

//        DataHolder.getCarbonTomcatService().getTomcat().getHost().

        if (getConfigContext().getAxisConfiguration().getTransportIn("http") != null) {
            int httpProxyPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (httpProxyPort != -1) {
                webappsWrapper.setHttpPort(httpProxyPort);
            } else {
                int httpPort = CarbonUtils.getTransportPort(getConfigContext(), "http");
                webappsWrapper.setHttpPort(httpPort);
            }
        }

        if (getConfigContext().getAxisConfiguration().getTransportIn("https") != null) {
            int httpsProxyPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "https");
            if (httpsProxyPort != -1) {
                webappsWrapper.setHttpsPort(httpsProxyPort);
            } else {
                int httpsPort = CarbonUtils.getTransportPort(getConfigContext(), "https");
                webappsWrapper.setHttpsPort(httpsPort);
            }
        }

        sortWebapps(webappsList);
        DataPaginator.doPaging(pageNumber, webappsList, webappsWrapper);
        return webappsWrapper;
    }

    private void sortWebapps(List<VersionedWebappMetadata> webapps) {
        if (webapps.size() > 0) {
            Collections.sort(webapps, new Comparator<VersionedWebappMetadata>() {
                public int compare(VersionedWebappMetadata arg0, VersionedWebappMetadata arg1) {
                    return arg0.getAppVersionRoot().compareToIgnoreCase(arg1.getAppVersionRoot());
                }
            });
        }
        for (VersionedWebappMetadata versionedWebappMetadata : webapps) {
            versionedWebappMetadata.sort();
        }
    }

    private Map<String, VersionedWebappMetadata> getStartedWebapps(String webappType, String webappSearchString) {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        Collection<WebApplication> allwebapps = new ArrayList<WebApplication>();
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            allwebapps.addAll(webApplicationsHolder.getStartedWebapps().values());
        }
        return getWebapps(allwebapps, webappType, webappSearchString);
    }

    private Map<String, VersionedWebappMetadata> getStoppedWebapps(String webappType, String webappSearchString) {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        Collection<WebApplication> allStoppedWebapps = new ArrayList<WebApplication>();
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            allStoppedWebapps.addAll(webApplicationsHolder.getStoppedWebapps().values());
        }
        return getWebapps(allStoppedWebapps, webappType, webappSearchString);
    }

    private Map<String, VersionedWebappMetadata> getWebapps(Collection<WebApplication> allWebapps,
                                                            String webappType, String webappsSearchString) {
        Map<String, VersionedWebappMetadata> webapps = new ConcurrentHashMap<String, VersionedWebappMetadata>();
        for (WebApplication webapp : allWebapps) {
            if (!doesWebappSatisfySearchString(webapp, webappsSearchString)) {
                continue;
            }

            // Check whether this is a generic webapp, if not ignore..
            if (!isWebappRelevant(webapp, webappType)) {
                continue;
            }
            WebappMetadata webappMetadata = getWebapp(webapp);
            WebappStatistics stats = webappMetadata.getStatistics();
            String appVersionRoot = webappMetadata.getContext();
            String webappKey = webappMetadata.getWebappKey();
            if (!WebappsConstants.DEFAULT_VERSION.equals(webapp.getVersion())) {
                appVersionRoot = appVersionRoot.substring(0,
                        appVersionRoot.lastIndexOf(webappMetadata.getAppVersion()));
            }

            VersionedWebappMetadata versionedWebappMetadata;
            if (webapps.containsKey(webappKey)) {
                versionedWebappMetadata = webapps.get(webappKey);
            } else {
                versionedWebappMetadata = new VersionedWebappMetadata(appVersionRoot);
                webapps.put(webappKey, versionedWebappMetadata);
            }
            versionedWebappMetadata.addWebappVersion(webappMetadata);
        }
        return webapps;
    }

    private Map<String, VersionedWebappMetadata> getFaultyWebapps(String webappsSearchString) {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        if (webApplicationsHolderMap == null) {
            return null;
        }
        String faultyAppVersionRoot = "/faulty";
        Map<String, VersionedWebappMetadata> webapps = new ConcurrentHashMap<String, VersionedWebappMetadata>();
        VersionedWebappMetadata versionedWebappMetadata = new VersionedWebappMetadata(faultyAppVersionRoot);
        webapps.put(faultyAppVersionRoot, versionedWebappMetadata);

        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            for (WebApplication webapp : webApplicationsHolder.getFaultyWebapps().values()) {
                if (!doesWebappSatisfySearchString(webapp, webappsSearchString)) {
                    continue;
                }
                WebappMetadata webappMetadata = new WebappMetadata();
                webappMetadata.setContext(webapp.getContextName());
                webappMetadata.setHostName(webapp.getHostName());
                webappMetadata.setWebappKey(WebAppUtils.getWebappKey(webapp.getWebappFile()));
                webappMetadata.setLastModifiedTime(webapp.getLastModifiedTime());
                webappMetadata.setWebappFile(webapp.getWebappFile().getName());
                webappMetadata.setStarted(false); //TODO
                webappMetadata.setRunning(false); //TODO
                webappMetadata.setFaulty(true);

                // Set the fault reason
                StringWriter sw = new StringWriter();
                webapp.getFaultReason().printStackTrace(new PrintWriter(sw));
                String faultException = sw.toString();
                webappMetadata.setFaultException(faultException);

                versionedWebappMetadata.addWebappVersion(webappMetadata);
            }

        }
        return webapps;

    }

    protected boolean doesWebappSatisfySearchString(WebApplication webapp,
                                                    String searchString) {

        if (searchString != null) {
            String regex = searchString.toLowerCase().
                    replace("..?", ".?").replace("..*", ".*").
                    replaceAll("\\?", ".?").replaceAll("\\*", ".*?");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(webapp.getContextName().toLowerCase());

            return regex.trim().length() == 0 || matcher.find();
        }

        return false;
    }

    private WebappsWrapper getWebappsWrapper(Map<String, WebApplicationsHolder> webappsHolderList,
                                             List<VersionedWebappMetadata> webapps, String webappType) {
        Map<String, ArrayList<WebApplication>> allwebapps = new HashMap<String, ArrayList<WebApplication>>();
        Map<String, ArrayList<WebApplication>> allFaultyWebapps = new HashMap<String, ArrayList<WebApplication>>();

        WebappsWrapper webappsWrapper = new WebappsWrapper();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolderList.values()) {
            for (WebApplication webApplication : webApplicationsHolder.getStartedWebapps().values()) {
                addValuetoMap(allwebapps, webApplication.getWebappFile().getName(), webApplication);
            }
            for (WebApplication webApplication : webApplicationsHolder.getFaultyWebapps().values()) {
                addValuetoMap(allFaultyWebapps, webApplication.getWebappFile().getName(), webApplication);
            }
        }
        webappsWrapper.setWebapps(webapps.toArray(new VersionedWebappMetadata[webapps.size()]));
        webappsWrapper.setNumberOfCorrectWebapps(
                getNumberOfWebapps(allwebapps, webappType));
        webappsWrapper.setNumberOfFaultyWebapps(
                getNumberOfWebapps(allFaultyWebapps, webappType));
        return webappsWrapper;
    }

    private void addValuetoMap(Map<String, ArrayList<WebApplication>> map, String key, WebApplication webApplication) {
        ArrayList<WebApplication> list = map.get(key);
        if (list == null) {
            list = new ArrayList<WebApplication>();
            map.put(key, list);
        }
        list.add(webApplication);
    }

    private int getNumberOfWebapps(Map<String, ArrayList<WebApplication>> webappMap, String webappType) {
        int number = 0;
        for (Map.Entry<String, ArrayList<WebApplication>> webappEntry : webappMap.entrySet()) {
            // Check whether this is a generic webapp, if so count..
            for (WebApplication webApplication : webappEntry.getValue()) {
                if (isWebappRelevant(webApplication, webappType)){
                    number++;
                }
            }
        }
        return number;
    }

    /**
     * This method can be used to check whether the given web app is relevant for this Webapp
     * type. Only generic webapps are relevant for this Admin service.
     *
     * @param webapp     - WebApplication instance
     * @param webappType application type
     * @return - true if relevant
     */
    protected boolean isWebappRelevant(WebApplication webapp, String webappType) {
        // skip the Stratos landing page webapp
        if (webapp.getContextName().contains("STRATOS_ROOT")) {
            return false;
        }
        String filterProp = (String) webapp.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If non of the filters are set, this is a generic webapp, so return true

        if (WebappsConstants.ALL_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            return true;
        } else if (WebappsConstants.JAX_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            return filterProp != null &&
                    WebappsConstants.JAX_WEBAPP_FILTER_PROP.equalsIgnoreCase
                            (filterProp);
        } else if (WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            return filterProp != null &&
                    WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(filterProp);
        } else {
            return filterProp == null ||
                    WebappsConstants.WEBAPP_FILTER_PROP.equalsIgnoreCase(filterProp);
        }
    }

    /**
     * Delete a set of started webapps
     *
     * @param webappKey The name:hostname pairs of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStartedWebapps(String[] webappKey) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteWebapps(webappKey, webApplicationsHolder.getStartedWebapps());
        }
    }

    /**
     * Delete a set of stopped webapps
     *
     * @param webappKey The name:hostname pairs of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStoppedWebapps(String[] webappKey) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteWebapps(webappKey, webApplicationsHolder.getStoppedWebapps());
        }
    }

    /**
     * Delete a set of faulty webapps
     *
     * @param webappKey The name:hostname pairs of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteFaultyWebapps(String[] webappKey) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteWebapps(webappKey, webApplicationsHolder.getFaultyWebapps());
        }
    }

    /**
     * Delete set of all types of webapps. (started, stopped, faulty)
     *
     * @param webappKey The name:hostname pairs of the webapp files to be deleted
     * @throws AxisFault AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllWebApps(String[] webappKey) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteWebapps(webappKey, webApplicationsHolder.getStartedWebapps());
            deleteWebapps(webappKey, webApplicationsHolder.getStoppedWebapps());
            deleteWebapps(webappKey, webApplicationsHolder.getFaultyWebapps());
        }
    }

    //Delete webapps. "webappKey" has a <hostName>:<webappName> pair

    private void deleteWebapps(String[] webappKey, Map<String, WebApplication> webapps) throws AxisFault {

        for (String key : webappKey) {
            WebApplication webApplication = null;
            if (key.contains(":")) {
                webApplication = webapps.get(getProperty("WebappName", key));

            } else {
                webApplication = webapps.get(key);
            }
            if ((webApplication != null && (getProperty("HostName", key)).equals(webApplication.getHostName()))) {
                try {
                    webapps.remove(webApplication.getWebappFile().getName());
                    webApplication.delete();
                } catch (CarbonException e) {
                    handleException("Could not delete webapp " + webApplication, e);
                }
            }


        }
    }

    private String getProperty(String propertyName, String keyvalue) {
        if (propertyName.equals("HostName")) {
            return keyvalue.split(":")[0];
        } else if (propertyName.equals("WebappName")) {
            return keyvalue.split(":")[1];
        }
        return "";
    }

    private void undeployWebapps(String[] webappFileNames,
                                 Map<String, WebApplication> webapps) throws AxisFault {
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = webapps.get(webappFileName);
            try {
                webapp.undeploy();
                webapps.remove(webappFileName);
            } catch (CarbonException e) {
                handleException("Could not delete webapp " + webapp, e);
            }
        }
    }

    /**
     * Delete all started webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStartedWebapps() throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteAllWebapps(webApplicationsHolder.getStartedWebapps());
        }
    }

    /**
     * Delete all stopped webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStoppedWebapps() throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteAllWebapps(webApplicationsHolder.getStoppedWebapps());
        }
    }

    /**
     * Delete all faulty webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllFaultyWebapps() throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            deleteAllWebapps(webApplicationsHolder.getFaultyWebapps());
        }
    }

    private void deleteAllWebapps(Map<String, WebApplication> webapps) throws AxisFault {
        for (WebApplication webapp : webapps.values()) {
            try {
                webapp.delete();
            } catch (CarbonException e) {
                handleException("Could not delete started webapp " + webapp, e);
            }
        }
        webapps.clear();
    }

    /**
     * Reload all webapps
     */
    public void reloadAllWebapps() {
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            Map<String, WebApplication> startedWebapps = webApplicationsHolder.getStartedWebapps();
            for (WebApplication webapp : startedWebapps.values()) {
                webapp.reload();
                webapplicationHelperList.add(new WebapplicationHelper(webapp.getHostName(), webapp.getWebappFile().getName()));
            }
            sendClusterSyncMessage(ApplicationOpType.RELOAD, webapplicationHelperList);
        }
    }

    /**
     * Reload a set of webapps
     *
     * @param webappKey The file_name:hostname pairs of the webapps to be reloaded
     */
    public void reloadWebapps(String[] webappKey) {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            for (String key : webappKey) {
                WebApplication webApplication;
                if (key.contains(":")) {
                    webApplication = webApplicationsHolder.getStartedWebapps().get(getProperty("WebappName", key));
                } else {
                    webApplication = webApplicationsHolder.getStartedWebapps().get(key);
                }
                if ((webApplication != null && (getProperty("HostName", key)).equals(webApplication.getHostName())) ||
                        (webApplication != null && WebAppUtils.getServerConfigHostName().equals(webApplication.getHostName()))) {
                    webApplication.reload();
                    webapplicationHelperList.add(new WebapplicationHelper(webApplication.getHostName(), webApplication.getWebappFile().getName()));
                }
            }


        }
        sendClusterSyncMessage(ApplicationOpType.RELOAD, webapplicationHelperList);
    }

    /**
     * Reset the bam enable and disable option.
     *
     * @param webappFileName
     * @param value
     */
    public void setBamConfiguration(String webappFileName, String value, String hostName) {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            WebApplication webApplication = webApplicationsHolder.getStartedWebapps().get(webappFileName);
            if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                webApplication.updateWebappMetaDataforBam(value);
                break;
            }
        }
        /*String []  webappFileNames = new String [1];
        webappFileNames[0] = webappFileName;
        sendClusterSyncMessage(ApplicationOpType.RELOAD, webappFileNames);*/
    }

    /**
     * This method returns the bam configuration statistic enabled or not
     *
     * @param webappFileName
     * @return
     */
    public String getBamConfiguration(String webappFileName, String hostName) {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        String metadata = null;
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            try {
                WebApplication webApplication = webApplicationsHolder.getStartedWebapps().get(webappFileName);
                if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                    metadata = webApplication.getBamEnableFromWebappMetaData();
                    break;
                }
            } catch (Exception e) {
                log.error("Unable to read bam configurations", e);
                return null;
            }

        }
        return metadata;
    }

    /**
     * Undeploy all webapps
     *
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopAllWebapps() throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            Map<String, WebApplication> startedWebapps = webApplicationsHolder.getStartedWebapps();
            for (WebApplication webapp : startedWebapps.values()) {
                webapplicationHelperList.add(new WebapplicationHelper(webapp.getHostName(), webapp.getWebappFile().getName()));
                try {
                    webapp.stop();
                    persistWebappStoppedState(webapp);
                } catch (CarbonException e) {
                    handleException("Error occurred while undeploying all webapps", e);
                }
            }
            startedWebapps.clear();
        }
        sendClusterSyncMessage(ApplicationOpType.STOP, webapplicationHelperList);

    }

    /**
     * Undeploy a set of webapps
     *
     * @param webappKeys The hostname:file_name pairs of the webapps to be stopped
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopWebapps(String[] webappKeys) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            Map<String, WebApplication> startedWebapps = webApplicationsHolder.getStartedWebapps();
            for (String key : webappKeys) {
                WebApplication webApplication;
                if (key.contains(":")) {
                    webApplication = startedWebapps.get(getProperty("WebappName", key));
                } else {
                    webApplication = startedWebapps.get(key);
                }
                try {
                    if ((webApplication != null && getProperty("HostName", key).equals(webApplication.getHostName())) ||
                            (webApplication != null && WebAppUtils.getServerConfigHostName().equals(webApplication.getHostName()))) {
                        webApplicationsHolder.stopWebapp(webApplication);
                        persistWebappStoppedState(webApplication);
                        webapplicationHelperList.add(new WebapplicationHelper(webApplication.getHostName(), webApplication.getWebappFile().getName()));
                    }
                } catch (CarbonException e) {
                    handleException("Error occurred while undeploying webapps", e);
                }
            }
        }
        sendClusterSyncMessage(ApplicationOpType.STOP, webapplicationHelperList);
    }

    /**
     * Redeploy all webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while restarting webapps
     */
    public void startAllWebapps() throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();
            Deployer webappDeployer =
                    ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(
                            webApplicationsHolder.getWebappsDir().getName(), WebappsConstants.WEBAPP_EXTENSION);
            for (WebApplication webapp : stoppedWebapps.values()) {
                startWebapp(stoppedWebapps, webapp, webApplicationsHolder);
                persistWebappStoppedState(webapp);
                webapplicationHelperList.add(new WebapplicationHelper(webapp.getHostName(), webapp.getWebappFile().getName()));
            }
            stoppedWebapps.clear();
        }
        sendClusterSyncMessage(ApplicationOpType.START, webapplicationHelperList);
    }

    /**
     * Redeploy a set of webapps
     *
     * @param webappKeys The hostname:file_name pairs of the webapps to be restarted
     * @throws org.apache.axis2.AxisFault If a deployment error occurs
     */
    public void startWebapps(String[] webappKeys) throws AxisFault {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<WebapplicationHelper> webapplicationHelperList = new ArrayList<WebapplicationHelper>();
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();
            Deployer webappDeployer =
                    ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(
                            webApplicationsHolder.getWebappsDir().getName(), WebappsConstants.WEBAPP_EXTENSION);
            for (String key : webappKeys) {
                WebApplication webApplication;
                if (key.contains(":")) {
                    webApplication = stoppedWebapps.get(getProperty("WebappName", key));
                } else {
                    webApplication = stoppedWebapps.get(key);
                }
                if (webApplication != null && getProperty("HostName", key).equals(webApplication.getHostName()) ||
                        (webApplication != null && WebAppUtils.getServerConfigHostName().equals(webApplication.getHostName()))) {
                    startWebapp(stoppedWebapps, webApplication, webApplicationsHolder);
                    persistWebappStoppedState(webApplication);
                    webapplicationHelperList.add(new WebapplicationHelper(webApplication.getHostName(), webApplication.getWebappFile().getName()));
                }
            }
        }
        sendClusterSyncMessage(ApplicationOpType.START, webapplicationHelperList);
    }

    private void startWebapp(Map<String, WebApplication> stoppedWebapps,
                             WebApplication webapp, WebApplicationsHolder webappsHolder) throws AxisFault {
        try {
            boolean started = webapp.start();
            if (started) {
                String webappFileName = webapp.getWebappFile().getName();
                stoppedWebapps.remove(webappFileName);
                Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
                startedWebapps.put(webappFileName, webapp);
            }
        } catch (CarbonException e) {
            String msg = "Cannot start webapp " + webapp;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    /**
     * Get all active sessions of a webapp
     *
     * @param webappFileName The names of the webapp file
     * @param pageNumber     The number of the page to fetch, starting with 0
     * @return The session array
     */
    public SessionsWrapper getActiveSessions(String webappFileName, int pageNumber, String hostName) {
        Map<String, WebApplicationsHolder> webappsHolders = WebAppUtils.getAllWebappHolders(getConfigContext());
        List<SessionMetadata> sessionMetadataList = new ArrayList<SessionMetadata>();
        int numOfActiveSessions = 0;
        ;
        for (WebApplicationsHolder webApplicationsHolder : webappsHolders.values()) {
            WebApplication webapp = webApplicationsHolder.getStartedWebapps().get(webappFileName);
            if (webapp != null && webapp.getHostName().equals(hostName)) {
                List<WebApplication.HttpSession> sessions = webapp.getSessions();
                numOfActiveSessions = sessions.size();
                for (WebApplication.HttpSession session : sessions) {
                    sessionMetadataList.add(new SessionMetadata(session));
                }
                break;
            }
        }
        sortSessions(sessionMetadataList);
        SessionsWrapper sessionsWrapper = new SessionsWrapper(sessionMetadataList);
        DataPaginator.doPaging(pageNumber, sessionMetadataList, sessionsWrapper);
        sessionsWrapper.setWebappFileName(webappFileName);
        sessionsWrapper.setNumberOfActiveSessions(numOfActiveSessions);
        return sessionsWrapper;
    }

    private void sortSessions(List<SessionMetadata> sessions) {
        if (sessions.size() > 0) {
            Collections.sort(sessions, new Comparator<SessionMetadata>() {
                public int compare(SessionMetadata arg0, SessionMetadata arg1) {
                    return (int) (arg0.getLastAccessedTime() - arg1.getLastAccessedTime());
                }
            });
        }
    }

    /**
     * Expire all sessions in all webapps
     */
    public void expireSessionsInAllWebapps() {
        for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
            Map<String, WebApplication> webapps = webApplicationsHolder.getStartedWebapps();
            for (WebApplication webapp : webapps.values()) {
                webapp.expireAllSessions();
            }
        }

    }

    /**
     * Expire all sessions in specified webapps
     *
     * @param webappKeySet The file names of the webapps whose sessions should be expired
     */
    public void expireSessionsInWebapps(String[] webappKeySet) {
        for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
            Map<String, WebApplication> webapps = webApplicationsHolder.getStartedWebapps();
            for (String key : webappKeySet) {
                WebApplication webapp;
                if (key.contains(":")) {
                    webapp = webapps.get(getProperty("WebappName", key));
                } else {
                    webapp = webapps.get(key);
                }
                if ((webapp != null && getProperty("HostName", key).equals(webapp.getHostName())) ||
                        (webapp != null && WebAppUtils.getServerConfigHostName().equals(webapp.getHostName()))) {
                    webapp.expireAllSessions();
                }
            }
        }
    }

    /**
     * Expire all sessions in the specified webapp which has a
     * lifetime >= <code>maxSessionLifetimeMillis</code>
     *
     * @param webappKey                The file name of the webapp whose sessions should be expired
     * @param maxSessionLifetimeMillis The max allowed lifetime for the sessions
     */
    public void expireSessionsInWebapp(String webappKey, long maxSessionLifetimeMillis) {
        for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
            Map<String, WebApplication> webapps = webApplicationsHolder.getStartedWebapps();
            WebApplication webapp;
            if (webappKey.contains(":")) {
                webapp = webapps.get(getProperty("WebappName", webappKey));
            } else {
                webapp = webapps.get(webappKey);
            }
            if ((webapp != null && getProperty("HostName", webappKey).equals(webapp.getHostName())) ||
                    (webapp != null && WebAppUtils.getServerConfigHostName().equals(webapp.getHostName()))) {
                webapp.expireSessions(maxSessionLifetimeMillis);
                break;
            }
        }
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     * @param sessionIDs     Array of session IDs
     * @throws org.apache.axis2.AxisFault If an error occurs while retrieving sessions
     */
    public void expireSessions(String webappFileName, String[] sessionIDs, String hostName) throws AxisFault {
        for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
            Map<String, WebApplication> webapps = webApplicationsHolder.getStartedWebapps();
            WebApplication webapp = webapps.get(webappFileName);
            if (webapp != null && webapp.getHostName().equals(hostName)) {
                try {
                    webapp.expireSessions(sessionIDs);
                } catch (CarbonException e) {
                    handleException("Cannot expire specified sessions in webapp " + webappFileName, e);
                }
                break;
            }
        }
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappKey The hostname:file_name pairs of the webapp whose sessions should be expired
     */
    public void expireAllSessions(String webappKey) {
        for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
            Map<String, WebApplication> webapps = webApplicationsHolder.getStartedWebapps();
            WebApplication webapp;
            if (webappKey.contains(":")) {
                webapp = webapps.get(getProperty("WebappName", webappKey));
            } else {
                webapp = webapps.get(webappKey);
            }
            if ((webapp != null && getProperty("HostName", webappKey).equals(webapp.getHostName())) ||
                    (webapp != null && WebAppUtils.getServerConfigHostName().equals(webapp.getHostName()))) {
                webapp.expireAllSessions();
                break;
            }
        }
    }

    /**
     * Upload a webapp
     * @param webappUploadDataList Array of data representing the webapps that are to be uploaded
     * @return true - if upload was successful
     * @throws AxisFault If an error occurrs while uploading
     */
    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
        AxisConfiguration axisConfig = getAxisConfig();

        for (WebappUploadData uploadData : webappUploadDataList) {
            String hostname = uploadData.getHostName();
            if (uploadData.getHostName() == null) {
                hostname = WebAppUtils.getServerConfigHostName();
            }
            File webappsDir = new File(getWebappDeploymentDirPath(WebappsConstants.WEBAPP_FILTER_PROP, hostname));
            if (!webappsDir.exists() && !webappsDir.mkdirs()) {
                log.warn("Could not create directory " + webappsDir.getAbsolutePath());
            }

            String fileName = uploadData.getFileName();
            String version = uploadData.getVersion();
            if (version != "" && version != null) {
                if (fileName.contains(".war")) {
                    fileName = fileName.replace(".war", "#" + version + ".war");
                } else if (fileName.contains(".zip")) {
                    fileName = fileName.replace(".zip", "#" + version + ".zip");
                }
            }
            if (WebAppUtils.validateWebappFileName(fileName)) {
                String msg = "Web app file name consists unsupported characters  - " + fileName;
                log.error(msg);
                throw new AxisFault(msg);
            }
            fileName = fileName.substring(fileName.lastIndexOf(System.getProperty("file.separator")) + 1);
            File destFile = new File(webappsDir, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destFile);
                uploadData.getDataHandler().writeTo(fos);
            } catch (IOException e) {
                handleException("Error occured while uploading the webapp " + fileName, e);
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException e) {
                    log.warn("Could not close file " + destFile.getAbsolutePath());
                }
            }
        }
        return true;
    }

    protected String getWebappDeploymentDirPath(String webappType, String hostName) {
        String webappDeploymentDir;
        if (WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            webappDeploymentDir = WebappsConstants.JAGGERY_WEBAPP_REPO;
            return getAxisConfig().getRepository().getPath() + File.separator + webappDeploymentDir;
        } else {
            String appBase = WebAppUtils.getAppbase(hostName);
            if (appBase.endsWith(File.separator)) {
                appBase = appBase.substring(0, appBase.length() - 1);
            }
            webappDeploymentDir = appBase.substring(appBase.lastIndexOf(File.separator), appBase.length());
            return getAxisConfig().getRepository().getPath() + webappDeploymentDir;
        }

    }

    protected String getWebappDeploymentDirPath(String webappType) {
        String webappDeploymentDir;
        if (WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            webappDeploymentDir = WebappsConstants.JAGGERY_WEBAPP_REPO;

        } else {
            webappDeploymentDir = WebappsConstants.WEBAPP_DEPLOYMENT_FOLDER;
        }
        return getAxisConfig().getRepository().getPath() + File.separator + webappDeploymentDir;
    }

    /**
     * Return the location of the actual webapp file
     *
     * @param fileName   name of the webapp file
     * @param webappType type of the webapp
     * @return
     */
    protected String getWebappDeploymentFile(String fileName, String hostName, String webappType) {
        String webappDeploymentDir;
        String webappFilepath = null;
        Map<String, WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(getConfigContext());
        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            WebApplication webApplication = webApplicationsHolder.getAllWebapps().get(fileName);
            if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                File webappFile = webApplicationsHolder.getAllWebapps().get(fileName).getWebappFile();
                // if webapp deployed using CApp this give the actual webapp file
                // since its not inside repository/deployment/webapps directory
                if (webappFile.getAbsolutePath().contains("carbonapps")) {
                    webappFilepath = webappFile.getAbsolutePath();
                } else {
                    if (WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
                        webappDeploymentDir = WebappsConstants.JAGGERY_WEBAPP_REPO;
                        webappFilepath = getAxisConfig().getRepository().getPath() +
                                webappDeploymentDir + File.separator + fileName;
                    } else {
                        webappFilepath = getAxisConfig().getRepository().getPath() +
                                WebAppUtils.getWebappDir(webappFile.getAbsolutePath()) + File.separator + fileName;
                    }
                }
                break;
            }
        }
        return webappFilepath;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    /**
     * Downloads the webapp archive (.war) file
     *
     * @param fileName   name of the .war that needs to be downloaded
     * @param webappType application type
     * @param hostName   virtualhost
     * @return the corresponding data handler of the .war that needs to be downloaded
     */
    public DataHandler downloadWarFileHandler(String fileName, String hostName, String webappType) {
        String repoPath = getAxisConfig().getRepository().getPath();
        String appsPath = getWebappDeploymentFile(fileName, hostName, webappType);

        File webAppFile = new File(appsPath);
        DataHandler handler = null;

        if (webAppFile.isDirectory()) {
            String zipTo = "tmp" + File.separator + fileName + ".zip";
            File fDownload = new File(zipTo);
            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            synchronized (this) {
                try {
                    archiveManipulator.archiveDir(zipTo, webAppFile.getAbsolutePath());
                    FileDataSource datasource = new FileDataSource(fDownload);
                    handler = new DataHandler(datasource);
                } catch (IOException e) {
                    log.error("Error downloading WAR file.", e);
                }
            }
        } else {
            FileDataSource datasource = new FileDataSource(new File(appsPath));
            handler = new DataHandler(datasource);
        }
        return handler;
    }

    /**
     * check if unpack wars enabled
     *
     * @return true if enabled.
     */
    public boolean isUnpackWARs() {
        return TomcatUtil.checkUnpackWars();
    }

    private void sendClusterSyncMessage(ApplicationOpType applicationOpType, List<WebapplicationHelper> webapplicationHelperList) {
        // For sending clustering messages we need to use the super-tenant's AxisConfig (Main Server
        // AxisConfiguration) because we are using the clustering facility offered by the ST in the
        // tenants
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ClusteringAgent clusteringAgent =
                DataHolder.getServerConfigContext().getAxisConfiguration().getClusteringAgent();
        if (clusteringAgent != null) {
            int numberOfRetries = 0;
            UUID messageId = UUID.randomUUID();
            ApplicationSynchronizeRequest request =
                    new ApplicationSynchronizeRequest(tenantId, tenantDomain, messageId,
                            applicationOpType, webapplicationHelperList);
            while (numberOfRetries < 60) {
                try {
                    clusteringAgent.sendMessage(request, true);
                    log.info("Sent [" + request + "]");
                    break;
                } catch (ClusteringFault e) {
                    numberOfRetries++;
                    if (numberOfRetries < 60) {
                        log.warn("Could not send SynchronizeRepositoryRequest for tenant " +
                                tenantId + ". Retry will be attempted in 2s. Request: " + request, e);
                    } else {
                        log.error("Could not send SynchronizeRepositoryRequest for tenant " +
                                tenantId + ". Several retries failed. Request:" + request, e);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public void changeDefaultAppVersion(String appGroupName, String fileName, String hostName) throws AxisFault, ArtifactMetadataException {
        String appGroup = "";
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            appGroup = appGroupName.replace("/", "");
        } else {
            String[] parts = appGroupName.split("/");
            appGroup = parts[parts.length - 1];
        }

        if (appGroup != null && fileName != null && isDefaultVersionManagementEnabled()) {
            boolean proceed = false;
            boolean noCurrentDefaultApp = false;
            AxisConfiguration axisConfig = getAxisConfig();
            String webappsDirPath = getWebappDeploymentDirPath(WebappsConstants.WEBAPP_FILTER_PROP, hostName);
            if (new File(webappsDirPath).exists()) {
                proceed = true;
            }


            //phase 1 - revert current default version to it's original version.
            if (proceed) {
                String originalNameOfCurrentDefaultApp = getOriginalNameOfCurrentDefaultApp(appGroup);
                File currentAppFile = new File(webappsDirPath, appGroup.concat(".war"));
                File currentAppOriginalFile = new File(webappsDirPath, originalNameOfCurrentDefaultApp);
                if (currentAppFile.exists() && !currentAppOriginalFile.exists()) {
                    handleWebappMetaDetaFile(appGroup.concat(".war"),
                            originalNameOfCurrentDefaultApp, WebappsConstants.KEEP_DEFAULT_VERSION_META_DATA_STRATEGY, hostName);
                    proceed = currentAppFile.renameTo(currentAppOriginalFile);

                }
            }

            //phase 2 - rename new version as new default application.
            if (proceed) {
                File newAppOriginalFile = new File(webappsDirPath, fileName);
                File newAppFile = new File(webappsDirPath, appGroup.concat(".war"));
                if (newAppOriginalFile.exists() && !newAppFile.exists()) {
                    proceed = newAppOriginalFile.renameTo(newAppFile);
                    if (proceed && log.isWarnEnabled()) {
                        setOriginalNameOfCurrentDefaultApp(appGroup, fileName);
                        log.info(fileName + " is marked as new default version ");

                    } else if (log.isWarnEnabled()) {
                        log.info("Error occurred making " + fileName + " as the default version");

                    }
                }
            }

            if (!proceed && log.isWarnEnabled()) {
                log.info("Error occurred making " + fileName + " as the default version");
            }

        }
    }

    private void setOriginalNameOfCurrentDefaultApp(String appName, String fileName) throws AxisFault, ArtifactMetadataException {
        AppVersionGroupPersister.persistWebappGroupMetadata(appName, fileName, getAxisConfig());
    }

    private void handleWebappMetaDetaFile(String originalFileName, String newFileName, int handlingStrategy, String hostName) {
        switch (handlingStrategy) {
            case WebappsConstants.KEEP_DEFAULT_VERSION_META_DATA_STRATEGY:
                for (WebApplicationsHolder webApplicationsHolder : WebAppUtils.getAllWebappHolders(getConfigContext()).values()) {
                    WebApplication webApplication = webApplicationsHolder.getStartedWebapps().get(originalFileName);
                    if (webApplication != null && webApplication.getHostName().equals(hostName)) {
                        webApplication.addParameter(WebappsConstants.KEEP_WEBAPP_METADATA_HISTORY_PARAM, Boolean.TRUE.toString());
                        break;
                    }

                }
        }
    }

    private String getOriginalNameOfCurrentDefaultApp(String appName) throws AxisFault, ArtifactMetadataException {
        String originalName = AppVersionGroupPersister.readWebappGroupMetadata(appName, getAxisConfig());
        if (originalName == null) {
            /*
            If  'originalName == null' means current default app is unversioned app (e.g- app.war )
            if so just append "/0" as the file name.( e.g "app#o.war" )
            */
            StringBuilder builder = new StringBuilder(appName);
            builder.append(WebappsConstants.FWD_SLASH_REPLACEMENT).append(WebappsConstants.DEFAULT_VERSION_STRING).append(".war");
            return builder.toString();
        } else {
            return originalName;
        }
    }

    private String getVhostAppbase(String hostName) {
        return WebAppUtils.getAppbase(hostName);
    }

    public boolean isDefaultVersionManagementEnabled() {
        return (defaultVersionManagement == null) ?
                Boolean.parseBoolean(System.getProperty(WebappsConstants.WEB_APP_DEFAULT_VERSION_SUPPORT)) :
                defaultVersionManagement;
    }

    /**
     *
     * @return new VhostHolder instance
     */
    public VhostHolder getVhostHolder() {
        List<String> vhostNames = WebAppUtils.getVhostNames();
        VhostHolder vhostHolder = new VhostHolder();
        vhostHolder.setVhosts(vhostNames.toArray(new String[vhostNames.size()]));
        vhostHolder.setDefaultHostName(WebAppUtils.getServerConfigHostName());
        return vhostHolder;
    }

    /**
     * Persists the webapp stopped state in the registry
     *
     * @param webApplication WebApplication instance
     */
    private void persistWebappStoppedState(WebApplication webApplication) {
        if (DataHolder.getRegistryService() != null) {
            try {
                Registry configSystemRegistry = DataHolder.getRegistryService().getConfigSystemRegistry(
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                String webappResourcePath = WebAppUtils.getWebappResourcePath(webApplication);
                Resource webappResource;

                String webappStatus = webApplication.getState();

                if (configSystemRegistry.resourceExists(webappResourcePath)) {
                    webappResource = configSystemRegistry.get(webappResourcePath);
                } else {
                    webappResource = configSystemRegistry.newCollection();
                }

                webappResource.setProperty(WebappsConstants.WEBAPP_STATUS, webappStatus);
                configSystemRegistry.put(webappResourcePath, webappResource);
            } catch (RegistryException e) {
                log.error("Failed to persist webapp stopped state for: " + webApplication.getContext());
            }
        }
    }

}
