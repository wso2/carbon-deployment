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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataException;
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
import java.io.*;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Admin service for managing webapps
 *
 */
@SuppressWarnings("unused")
public class WebappAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(WebappAdmin.class);
    private static Boolean defaultVersionManagement = null;
    private Map<String, Boolean>  versioningConfiguration = new HashMap<String, Boolean>();
    /*
      This map contains - MAP <Web-application-group-name , original-file-name-of-current-default-version> mapping.

      This is useful when reverting current default version into it's original version.

      e.g - app => app#4.war
            mvcapp => mvcapp#2.war
     */
    private Map<String, String>  appGroupToCurrentVersionMap = null;


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
     * @param webappType application type
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
        } else if(webappState.equalsIgnoreCase(WebappsConstants.WebappState.ALL)){
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
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStartedWebapp(String webappFileName) {
        WebApplicationsHolder holder = getWebappsHolder();
        WebApplication webApplication = holder.getStartedWebapps().get(webappFileName);
        WebappMetadata webappMetadata = null;
        if (webApplication != null) {
            webappMetadata = getWebapp(webApplication);
            webappMetadata.setStarted(true);
        }
        return webappMetadata;
    }

    /**
     * Get the details of an stopped webapp
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStoppedWebapp(String webappFileName) {
        WebApplicationsHolder holder = getWebappsHolder();
        WebApplication webApplication = holder.getStoppedWebapps().get(webappFileName);
        WebappMetadata webappMetadata = null;
        if (webApplication != null) {
            webappMetadata = getWebapp(webApplication);
            webappMetadata.setStarted(false);
        }
        return webappMetadata;
    }

    private WebappMetadata getWebapp(WebApplication webApplication) {
        WebappMetadata webappMetadata;
        webappMetadata = new WebappMetadata();

        String appContext = WebAppUtils.checkJaxApplication(webApplication);
        if (appContext == null) {
            appContext = "/";
        } else if(appContext.endsWith("/*")) {
            appContext = appContext.substring(0, appContext.indexOf("/*"));
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
     * @param webappType application type
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
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        WebappsWrapper webappsWrapper = getWebappsWrapper(webappsHolder, webappsList, webappType);
        try {
            webappsWrapper.setHostName(NetworkUtils.getLocalHostname());
        } catch (SocketException e) {
            log.error("Error occurred while getting local hostname", e);
        }

//        DataHolder.getCarbonTomcatService().getTomcat().getHost().

        if(getConfigContext().getAxisConfiguration().getTransportIn("http") != null) {
            int httpProxyPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (httpProxyPort != -1) {
                webappsWrapper.setHttpPort(httpProxyPort);
            } else {
                int httpPort = CarbonUtils.getTransportPort(getConfigContext(), "http");
                webappsWrapper.setHttpPort(httpPort);
            }
        }

        if(getConfigContext().getAxisConfiguration().getTransportIn("https") != null) {
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
                    return  arg0.getAppVersionRoot().compareToIgnoreCase(arg1.getAppVersionRoot());
                }
            });
        }
        for (VersionedWebappMetadata versionedWebappMetadata : webapps) {
            versionedWebappMetadata.sort();
        }
    }

    private Map<String, VersionedWebappMetadata> getStartedWebapps(String webappType, String webappSearchString) {
        return getWebapps(getWebappsHolder().getStartedWebapps().values(), webappType, webappSearchString);
    }

    private Map<String, VersionedWebappMetadata> getStoppedWebapps(String webappType, String webappSearchString) {
        return getWebapps(getWebappsHolder().getStoppedWebapps().values(), webappType, webappSearchString);
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
            if (!WebappsConstants.DEFAULT_VERSION.equals(webapp.getVersion())) {
                appVersionRoot = appVersionRoot.substring(0,
                        appVersionRoot.lastIndexOf(webappMetadata.getAppVersion()));
            }

            VersionedWebappMetadata versionedWebappMetadata;
            if (webapps.containsKey(appVersionRoot)) {
                versionedWebappMetadata = webapps.get(appVersionRoot);
            } else  {
                versionedWebappMetadata = new VersionedWebappMetadata(appVersionRoot);
                webapps.put(appVersionRoot, versionedWebappMetadata);
            }
            versionedWebappMetadata.addWebappVersion(webappMetadata);
        }
        return webapps;
    }

    private Map<String, VersionedWebappMetadata> getFaultyWebapps(String webappsSearchString) {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        if (webappsHolder == null) {
            return null;
        }
        String faultyAppVersionRoot = "/faulty";
        Map<String, VersionedWebappMetadata> webapps = new ConcurrentHashMap<String, VersionedWebappMetadata>();
        VersionedWebappMetadata versionedWebappMetadata = new VersionedWebappMetadata(faultyAppVersionRoot);
        webapps.put(faultyAppVersionRoot, versionedWebappMetadata);

        for (WebApplication webapp : webappsHolder.getFaultyWebapps().values()) {
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

    private WebApplicationsHolder getWebappsHolder() {
        return (WebApplicationsHolder) getConfigContext().
                getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
    }

    private WebappsWrapper getWebappsWrapper(WebApplicationsHolder webappsHolder,
                                             List<VersionedWebappMetadata> webapps, String webappType) {
        WebappsWrapper webappsWrapper = new WebappsWrapper();
        webappsWrapper.setWebapps(webapps.toArray(new VersionedWebappMetadata[webapps.size()]));
        webappsWrapper.setNumberOfCorrectWebapps(
                getNumberOfWebapps(webappsHolder.getStartedWebapps(), webappType));
        webappsWrapper.setNumberOfFaultyWebapps(
                getNumberOfWebapps(webappsHolder.getFaultyWebapps(), webappType));
        return webappsWrapper;
    }

    private int getNumberOfWebapps(Map <String, WebApplication> webappMap, String webappType) {
        int number = 0;
        for (Map.Entry<String, WebApplication> webappEntry : webappMap.entrySet()) {
            // Check whether this is a generic webapp, if so count..
            if (isWebappRelevant(webappEntry.getValue(), webappType)) {
                number++;
            }
        }
        return number;
    }

    /**
     * This method can be used to check whether the given web app is relevant for this Webapp
     * type. Only generic webapps are relevant for this Admin service.
     *
     *
     * @param webapp - WebApplication instance
     * @param webappType  application type
     * @return - true if relevant
     */
    protected boolean isWebappRelevant(WebApplication webapp, String webappType) {
        // skip the Stratos landing page webapp
        if (webapp.getContextName().contains("STRATOS_ROOT")) {
            return false;
        }
        String filterProp = (String) webapp.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If non of the filters are set, this is a generic webapp, so return true

        if(WebappsConstants.ALL_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            return true;
        } else if(WebappsConstants.JAX_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            return filterProp != null &&
                   WebappsConstants.JAX_WEBAPP_FILTER_PROP.equalsIgnoreCase
                    (filterProp);
        } else if(WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
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
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStartedWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getStartedWebapps());
    }

    /**
     * Delete a set of stopped webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getStoppedWebapps());
    }

    /**
     * Delete a set of faulty webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteFaultyWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getFaultyWebapps());
    }

    /**
     *Delete set of all types of webapps. (started, stopped, faulty)
     *
     * @param webappFileNames  The names of the webapp files to be deleted
     * @throws AxisFault   AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllWebApps(String[] webappFileNames) throws AxisFault {
        for (String webappFileName : webappFileNames) {
            deleteWebapp(webappFileName);
        }
    }

    /**
     * Delete a single webapp which can be in any state; started, stopped or faulty. This method
     * will search the webapp in all lists and delete it if found.
     *
     * @param webappFileName - name of the file to be deleted
     * @throws AxisFault - If an error occurs while deleting the webapp
     */
    public void deleteWebapp(String webappFileName) throws AxisFault {
        WebApplicationsHolder holder = getWebappsHolder();
        if (holder.getStartedWebapps().get(webappFileName) != null) {
            deleteStartedWebapps(new String[]{webappFileName});
        } else if (holder.getStoppedWebapps().get(webappFileName) != null) {
            deleteStoppedWebapps(new String[]{webappFileName});
        } else if (holder.getFaultyWebapps().get(webappFileName) != null) {
            deleteFaultyWebapps(new String[]{webappFileName});
        }
    }

    private void deleteWebapps(String[] webappFileNames,
                               Map<String, WebApplication> webapps) throws AxisFault {
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = webapps.get(webappFileName);
            try {
                webapps.remove(webappFileName);
                webapp.delete();
            } catch (CarbonException e) {
                handleException("Could not delete webapp " + webapp, e);
            }
        }
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
        deleteAllWebapps(getWebappsHolder().getStartedWebapps());
    }

    /**
     * Delete all stopped webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStoppedWebapps() throws AxisFault {
        deleteAllWebapps(getWebappsHolder().getStoppedWebapps());
    }

    /**
     * Delete all faulty webapps
     *
     * @throws AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllFaultyWebapps() throws AxisFault {
        deleteAllWebapps(getWebappsHolder().getFaultyWebapps());
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
        Map<String, WebApplication> startedWebapps = getWebappsHolder().getStartedWebapps();
        String[] webappFileNames = Arrays.copyOf(startedWebapps.keySet().toArray(),
                startedWebapps.size(), String[].class);
        for (WebApplication webapp : startedWebapps.values()) {
            webapp.reload();
        }
        sendClusterSyncMessage(ApplicationOpType.RELOAD, webappFileNames);
    }

    /**
     * Reload a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be reloaded
     */
    public void reloadWebapps(String[] webappFileNames) {
        for (String webappFileName : webappFileNames) {
            getWebappsHolder().getStartedWebapps().get(webappFileName).reload();
        }
        sendClusterSyncMessage(ApplicationOpType.RELOAD, webappFileNames);
    }

    /**
     * Reset the bam enable and disable option.
     * @param webappFileName
     * @param value
     */
    public void setBamConfiguration(String webappFileName, String value){
        getWebappsHolder().getStartedWebapps().get(webappFileName).updateWebappMetaDataforBam(value);
        /*String []  webappFileNames = new String [1];
        webappFileNames[0] = webappFileName;
        sendClusterSyncMessage(ApplicationOpType.RELOAD, webappFileNames);*/
    }

    /**
     * This method returns the bam configuration statistic enabled or not
     * @param webappFileName
     * @return
     */
    public String getBamConfiguration(String webappFileName){
        try {
            return getWebappsHolder().getStartedWebapps().get(webappFileName).getBamEnableFromWebappMetaData();
        } catch (Exception e) {
            log.error("Unable to read bam configurations",e);
            return null;
        }
    }

    /**
     * Undeploy all webapps
     *
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopAllWebapps() throws AxisFault {
        Map<String, WebApplication> startedWebapps = getWebappsHolder().getStartedWebapps();
        String[] webappFileNames = Arrays.copyOf(startedWebapps.keySet().toArray(),
                startedWebapps.size(), String[].class);
        for (WebApplication webapp : startedWebapps.values()) {
            try {
                webapp.stop();
            } catch (CarbonException e) {
                handleException("Error occurred while undeploying all webapps", e);
            }
        }
        startedWebapps.clear();
        sendClusterSyncMessage(ApplicationOpType.STOP, webappFileNames);
    }

    /**
     * Undeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be stopped
     * @throws AxisFault If an error occurs while undeploying
     */
    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
        for (String webappFileName : webappFileNames) {
            try {
                WebApplication webApplication = startedWebapps.get(webappFileName);
                if(webApplication != null) {
                    webappsHolder.stopWebapp(webApplication);
                }
            } catch (CarbonException e) {
                handleException("Error occurred while undeploying webapps", e);
            }
        }
        sendClusterSyncMessage(ApplicationOpType.STOP, webappFileNames);
    }

    /**
     * Redeploy all webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while restarting webapps
     */
    public void startAllWebapps() throws AxisFault {
        Map<String, WebApplication> stoppedWebapps = getWebappsHolder().getStoppedWebapps();
        String[] webappFileNames = Arrays.copyOf(stoppedWebapps.keySet().toArray(),
                stoppedWebapps.size(), String[].class);
        Deployer webappDeployer =
                ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(WebappsConstants
                        .WEBAPP_DEPLOYMENT_FOLDER, WebappsConstants.WEBAPP_EXTENSION);
        for (WebApplication webapp : stoppedWebapps.values()) {
            startWebapp(stoppedWebapps, webapp);
        }
        stoppedWebapps.clear();
        sendClusterSyncMessage(ApplicationOpType.START, webappFileNames);
    }

    /**
     * Redeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be restarted
     * @throws org.apache.axis2.AxisFault If a deployment error occurs
     */
    public void startWebapps(String[] webappFileNames) throws AxisFault {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();
        Deployer webappDeployer =
                ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(WebappsConstants
                        .WEBAPP_DEPLOYMENT_FOLDER, WebappsConstants.WEBAPP_EXTENSION);
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = stoppedWebapps.get(webappFileName);
            if(webapp!= null){
                startWebapp(stoppedWebapps, webapp);
            }
        }
        sendClusterSyncMessage(ApplicationOpType.START, webappFileNames);
    }

    private void startWebapp(Map<String, WebApplication> stoppedWebapps,
                             WebApplication webapp) throws AxisFault {
        try {
            boolean started = webapp.start();
            if (started) {
                String webappFileName = webapp.getWebappFile().getName();
                stoppedWebapps.remove(webappFileName);
                WebApplicationsHolder webappsHolder = getWebappsHolder();
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
    public SessionsWrapper getActiveSessions(String webappFileName, int pageNumber) {
        WebApplication webapp = getWebappsHolder().getStartedWebapps().get(webappFileName);
        List<SessionMetadata> sessionMetadataList = new ArrayList<SessionMetadata>();
        int numOfActiveSessions = 0;
        if (webapp != null) {
            List<WebApplication.HttpSession> sessions = webapp.getSessions();
            numOfActiveSessions = sessions.size();
            for (WebApplication.HttpSession session : sessions) {
                sessionMetadataList.add(new SessionMetadata(session));
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
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        for (WebApplication webapp : webapps.values()) {
            webapp.expireAllSessions();
        }
    }

    /**
     * Expire all sessions in specified webapps
     *
     * @param webappFileNames The file names of the webapps whose sessions should be expired
     */
    public void expireSessionsInWebapps(String[] webappFileNames) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = webapps.get(webappFileName);
            webapp.expireAllSessions();
        }
    }

    /**
     * Expire all sessions in the specified webapp which has a
     * lifetime >= <code>maxSessionLifetimeMillis</code>
     *
     * @param webappFileName           The file name of the webapp whose sessions should be expired
     * @param maxSessionLifetimeMillis The max allowed lifetime for the sessions
     */
    public void expireSessionsInWebapp(String webappFileName, long maxSessionLifetimeMillis) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        webapp.expireSessions(maxSessionLifetimeMillis);
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     * @param sessionIDs     Array of session IDs
     * @throws org.apache.axis2.AxisFault If an error occurs while retrieving sessions
     */
    public void expireSessions(String webappFileName, String[] sessionIDs) throws AxisFault {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        try {
            webapp.expireSessions(sessionIDs);
        } catch (CarbonException e) {
            handleException("Cannot expire specified sessions in webapp " + webappFileName, e);
        }
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     */
    public void expireAllSessions(String webappFileName) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        webapp.expireAllSessions();
    }

    /**
     * Upload a webapp
     *
     * @param webappUploadDataList Array of data representing the webapps that are to be uploaded
     * @return true - if upload was successful
     * @throws AxisFault If an error occurrs while uploading
     */
    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
        AxisConfiguration axisConfig = getAxisConfig();
        File webappsDir = new File(getWebappDeploymentDirPath(WebappsConstants.WEBAPP_FILTER_PROP));
        if (!webappsDir.exists() && !webappsDir.mkdirs()) {
            log.warn("Could not create directory " + webappsDir.getAbsolutePath());
        }

        for (WebappUploadData uploadData : webappUploadDataList) {
            String fileName = uploadData.getFileName();
            String version = uploadData.getVersion();
            if(version != "" && version != null){
                if(fileName.contains(".war")){
                    fileName = fileName.replace(".war", "#" + version + ".war");
                } else if(fileName.contains(".zip")) {
                    fileName = fileName.replace(".zip", "#" + version + ".zip");
                }
            }
            if(WebAppUtils.validateWebappFileName(fileName)){
                String msg = "Web app file name consists unsupported characters  - " + fileName;
                log.error(msg);
                throw new AxisFault(msg);
            }
            fileName = fileName.substring(fileName.lastIndexOf(System.getProperty("file.separator"))+1);
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

    protected String getWebappDeploymentDirPath(String webappType) {
        String webappDeploymentDir;
        if(WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
            webappDeploymentDir = WebappsConstants.JAGGERY_WEBAPP_REPO;
        } else {
            webappDeploymentDir = WebappsConstants.WEBAPP_DEPLOYMENT_FOLDER;
        }
        return getAxisConfig().getRepository().getPath() + File.separator + webappDeploymentDir;
    }

    /**
     * Return the location of the actual webapp file
     * @param fileName name of the webapp file
     * @param webappType type of the webapp
     * @return
     */
    protected String getWebappDeploymentFile(String fileName, String webappType) {
        String webappDeploymentDir;
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        File webappFile = webappsHolder.getStartedWebapps().get(fileName).getWebappFile();

        // if webapp deployed using CApp this give the actual webapp file
        // since its not inside repository/deployment/webapps directory
        if (webappFile.getAbsolutePath().contains("carbonapps")) {
            return webappFile.getAbsolutePath();
        } else {
            if(WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equalsIgnoreCase(webappType)) {
                webappDeploymentDir = WebappsConstants.JAGGERY_WEBAPP_REPO;
                return getAxisConfig().getRepository().getPath() + webappDeploymentDir + File.separator + fileName;
            } else {
                webappDeploymentDir = WebappsConstants.WEBAPP_DEPLOYMENT_FOLDER; //TODO this returns the "webapps" folder
                return webappFile.getAbsolutePath();
            }
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    /**
     * Downloads the webapp archive (.war) file
     * @param fileName name of the .war that needs to be downloaded
     * @param webappType application type
     * @return the corresponding data handler of the .war that needs to be downloaded
     */
    public DataHandler downloadWarFileHandler(String fileName, String webappType) {
        String repoPath = getAxisConfig().getRepository().getPath();
        String appsPath = getWebappDeploymentFile(fileName, webappType);

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
    public boolean isUnpackWARs(){
        return TomcatUtil.checkUnpackWars();
    }

    private void sendClusterSyncMessage(ApplicationOpType applicationOpType, String[] webappFileNames) {
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
                            applicationOpType, webappFileNames);
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

    public void changeDefaultAppVersion(String appGroupName, String fileName) throws AxisFault, ArtifactMetadataException {
        String appGroup = "";
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
            appGroup = appGroupName.replace("/", "");
        } else{
            String[] parts = appGroupName.split("/");
            appGroup = parts[parts.length-1];
        }

        if (appGroup != null && fileName != null && isDefaultVersionManagementEnabled()) {
            boolean proceed = false;
            boolean noCurrentDefaultApp = false;
            AxisConfiguration axisConfig = getAxisConfig();
            String webappsDirPath = getWebappDeploymentDirPath(WebappsConstants.WEBAPP_FILTER_PROP);
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
                            originalNameOfCurrentDefaultApp, WebappsConstants.KEEP_DEFAULT_VERSION_META_DATA_STRATEGY);
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

    private void handleWebappMetaDetaFile(String originalFileName, String newFileName, int handlingStrategy) {
        switch (handlingStrategy) {
            case WebappsConstants.KEEP_DEFAULT_VERSION_META_DATA_STRATEGY:
                WebApplicationsHolder holder = getWebappsHolder();
                WebApplication webApplication = holder.getStartedWebapps().get(originalFileName);
                webApplication.addParameter(WebappsConstants.KEEP_WEBAPP_METADATA_HISTORY_PARAM, Boolean.TRUE.toString());
                break;
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


    public boolean isDefaultVersionManagementEnabled() {
        return (defaultVersionManagement == null) ?
                Boolean.parseBoolean(System.getProperty(WebappsConstants.WEB_APP_DEFAULT_VERSION_SUPPORT)) :
                defaultVersionManagement;
    }

}
