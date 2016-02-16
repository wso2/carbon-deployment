/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataException;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataManager;
import org.wso2.carbon.core.persistence.metadata.ArtifactType;
import org.wso2.carbon.core.persistence.metadata.DeploymentArtifactMetadataFactory;
import org.wso2.carbon.tomcat.CarbonTomcatException;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.mgt.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.io.File;
import java.lang.management.ManagementPermission;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This deployer is responsible for deploying/undeploying/updating those webapps.
 *
 * @see org.wso2.carbon.tomcat
 */
@SuppressWarnings("unused")
public class TomcatGenericWebappsDeployer {

    private static Log log = LogFactory.getLog(TomcatGenericWebappsDeployer.class);

    protected String webContextPrefix;
    protected int tenantId;
    protected String tenantDomain;
    protected ConfigurationContext configurationContext;
    protected Map<String, WebApplicationsHolder> webApplicationsHolderMap;
    protected Map<String, Manager> sessionManagerMap =
            new ConcurrentHashMap<String, Manager>();

    /**
     * Constructor
     *
     * @param webContextPrefix         The Web context prefix
     * @param tenantId                 The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain             The tenant domain of the tenant to whom this deployer belongs to
     * @param webApplicationsHolderMap WebApplicationsHolder
     */
    public TomcatGenericWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        Map<String, WebApplicationsHolder> webApplicationsHolderMap,
                                        ConfigurationContext configurationContext) {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.webContextPrefix = webContextPrefix;
        this.webApplicationsHolderMap = webApplicationsHolderMap;
        this.configurationContext = configurationContext;
    }

    /**
     * Deploy webapps
     *
     * @param webappFile                The webapp file to be deployed
     * @param webContextParams          context-params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    public void deploy(File webappFile,
                       List<WebContextParameter> webContextParams,
                       List<Object> applicationEventListeners) throws CarbonException {
        String webappName = webappFile.getName();
        if (webappName.startsWith("#") || webappName.endsWith("#.war") || webappName.endsWith("#")) {
            throw new CarbonException("Invalid filename. Webapp name can't start or ends with '#'");
        }
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();

        if (webappFile.isDirectory()) {
            privilegedCarbonContext.setApplicationName(webappName);
        } else if (webappName.contains(".war") || webappName.contains(".zip")) {
            //removing extension to get app name for .war and .zip
            privilegedCarbonContext.setApplicationName(webappName.substring(0, webappName.indexOf(".war")));
        }
        long lastModifiedTime = webappFile.lastModified();
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                webappFile.getAbsolutePath(),this.configurationContext);
        WebApplication deployedWebapp =
                webApplicationsHolder.getStartedWebapps().get(webappFile.getName());

        WebApplication deployedUnpackedWebapp = null;
        if (deployedWebapp == null && (webappName.contains(".war") || webappName.contains(".zip"))) {
            String unpackDirName = webappName.endsWith(".war") ? webappName.replace(".war", "") : webappName;
            unpackDirName = webappName.endsWith(".zip") ? unpackDirName.replace(".zip", "") : unpackDirName;
            deployedUnpackedWebapp = webApplicationsHolder.getStartedWebapps().get(unpackDirName);
        }

        WebApplication undeployedWebapp =
                webApplicationsHolder.getStoppedWebapps().get(webappFile.getName());
        WebApplication faultyWebapp =
                webApplicationsHolder.getFaultyWebapps().get(webappFile.getName());

        WebApplication faultyUnpackedWebapp = null;
        if (deployedWebapp == null && (webappName.contains(".war") || webappName.contains(".zip"))) {
            String unpackDirName = webappName.endsWith(".war") ? webappName.replace(".war", "") : webappName;
            unpackDirName = webappName.endsWith(".zip") ? unpackDirName.replace(".zip", "") : unpackDirName;
            faultyUnpackedWebapp = webApplicationsHolder.getFaultyWebapps().get(unpackDirName);
        }

        if (deployedWebapp == null && faultyWebapp == null && undeployedWebapp == null
                && deployedUnpackedWebapp == null && faultyUnpackedWebapp == null) {
            handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
        } else if (deployedWebapp != null &&
                deployedWebapp.getLastModifiedTime() != lastModifiedTime) {
            handleHotUpdate(deployedWebapp, webContextParams, applicationEventListeners);
        } else if (faultyWebapp != null &&
                faultyWebapp.getLastModifiedTime() != lastModifiedTime) {
            handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
        } else if (deployedUnpackedWebapp != null &&
                deployedUnpackedWebapp.getLastModifiedTime() != lastModifiedTime) {
            undeploy(deployedUnpackedWebapp.getWebappFile());
            deployedUnpackedWebapp.delete();
            handleWarWebappDeployment(webappFile, webContextParams, applicationEventListeners);
        } else if (faultyUnpackedWebapp != null &&
                faultyUnpackedWebapp.getLastModifiedTime() != lastModifiedTime) {
            clearFaultyWebapp(faultyUnpackedWebapp.getWebappFile().getAbsolutePath());
            handleWarWebappDeployment(webappFile, webContextParams, applicationEventListeners);

        }
    }

    /**
     * Hot deploy a webapp. i.e., deploy a webapp that has newly become available.
     *
     * @param webapp                    The webapp WAR or directory that needs to be deployed
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If an error occurs during deployment
     */
    protected void handleHotDeployment(File webapp, List<WebContextParameter> webContextParams,
                                       List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webapp.getName();
        if (webapp.isDirectory()) {
            handleExplodedWebappDeployment(webapp, webContextParams, applicationEventListeners);
        } else if (filename.endsWith(".war")) {
            handleWarWebappDeployment(webapp, webContextParams, applicationEventListeners);
        } else if (filename.endsWith(".zip")) {
            handleZipWebappDeployment(webapp, webContextParams, applicationEventListeners);
        }
    }

    /**
     * Handle the deployment of a an archive webapp. i.e., a WAR
     *
     * @param webappWAR                 The WAR webapp file
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    protected void handleWarWebappDeployment(File webappWAR,
                                             List<WebContextParameter> webContextParams,
                                             List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webappWAR.getName();
        String warContext = "";
        if (!filename.equals("ROOT.war")) {  // FIXME: This is not working for some reason!
            warContext = filename.substring(0, filename.indexOf(".war"));
        }
        if (!warContext.equals("") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        } else if (warContext.equals("")){
            webContextPrefix = "";
        }

        String contextPath = handleAppVersion(webContextPrefix + warContext);

        handleWebappDeployment(webappWAR, contextPath,
                webContextParams, applicationEventListeners);
    }

    protected void handleZipWebappDeployment(File webapp,
                                             List<WebContextParameter> webContextParams,
                                             List<Object> applicationEventListeners)
            throws CarbonException {

    }

    /**
     * Handle the deployment of a an exploded webapp. i.e., a webapp deployed as a directory
     * & not an archive
     *
     * @param webappDir                 The exploded webapp directory
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    protected void handleExplodedWebappDeployment(File webappDir,
                                                  List<WebContextParameter> webContextParams,
                                                  List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webappDir.getName();
        String warContext = "";
        if (!filename.equals("ROOT")) {
            warContext = filename;
        }
        if (!warContext.equals("") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        } else if (warContext.equals("")){
            webContextPrefix = "";
        }

        String contextPath = handleAppVersion(webContextPrefix + warContext);

        handleWebappDeployment(webappDir, contextPath,
                webContextParams, applicationEventListeners);
    }

    /**
     *
     * @param baseDir web application directory path
     * @return host name that matches the directory path
     */
    private Host getMatchingVirtualHost(String baseDir) {
        Host virtualHost = null;
        Container[] virtualHosts = DataHolder.getCarbonTomcatService().getTomcat().getEngine().findChildren();
        for (Container vHost : virtualHosts) {
            Host childHost = (Host) vHost;
            String appBase = childHost.getAppBase().replace("/", File.separator);
            if (appBase.endsWith(File.separator)) {
                appBase = appBase.substring(0, appBase.lastIndexOf(File.separator));
            }

            if (isWebappUploadedToVirtualAppBase(baseDir, appBase)) {
                virtualHost = childHost;
                break;
            }
        }
        return virtualHost;
    }

    /**
     *
     * @param webAppBaseDir web application base path
     * @return true if webapp is uploaded to Virtual Host
     */
    private boolean isWebappUploadedToVirtualAppBase(String webAppBaseDir, String appBase) {
        String axis2Repo = MultitenantUtils.getAxis2RepositoryPath(this.tenantId);
        String defaultWebAppPath = Paths.get(axis2Repo, CarbonConstants.WEBAPP_DEPLOYMENT_FOLDER).toString();
        if (webAppBaseDir.equals(defaultWebAppPath)) {
            return false;
        } else {
            String baseDir = appBase.substring(appBase.lastIndexOf(File.separator), appBase.length());
            return (webAppBaseDir.contains(axis2Repo) && webAppBaseDir.endsWith(baseDir));
        }
    }

    private Host getHost(String webappFilePath) throws CarbonTomcatException {
        String baseDir = webappFilePath.substring(0, webappFilePath.lastIndexOf(File.separator));
        Host defaultHost = (Host) DataHolder.getCarbonTomcatService().getTomcat().getEngine().findChild(
                DataHolder.getCarbonTomcatService().getTomcat().getEngine().getDefaultHost());
        Host virtualHost = getMatchingVirtualHost(baseDir);

        if (virtualHost != null) {
            return virtualHost;
        } else {
            return defaultHost;
        }
    }

    protected void handleWebappDeployment(File webappFile, String contextStr,
                                          List<WebContextParameter> webContextParams,
                                          List<Object> applicationEventListeners) throws CarbonException {
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String filename = webappFile.getName();
        try {
            Context context = DataHolder.getCarbonTomcatService().addWebApp(getHost(webappFile.getAbsolutePath()),
                                                                            contextStr, webappFile.getAbsolutePath());

            //deploying web app for url-mapper
            if (DataHolder.getHotUpdateService() != null) {
                List<String> hostNames = DataHolder.getHotUpdateService().getMappigsPerWebapp(contextStr);
                for (String hostName : hostNames) {
                    CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
                    Host host = DataHolder.getHotUpdateService().addHost(hostName);
                    URLMappingHolder.getInstance().putUrlMappingForApplication(hostName, contextStr);
                    Context contextForHost =
                            DataHolder.getCarbonTomcatService().addWebApp(host, "/", webappFile.getAbsolutePath());
                    log.info("Deployed webapp on host: " + contextForHost);
                }
            }

            Manager manager = context.getManager();
            if (context.getDistributable() && (DataHolder.getCarbonTomcatService().getTomcat().
                    getService().getContainer().getCluster()) != null) {

                // Using clusterable manager
                Manager sessionManager;

                if (manager instanceof CarbonTomcatClusterableSessionManager) {
                    sessionManager = manager;
                    ((CarbonTomcatClusterableSessionManager) manager).setOwnerTenantId(tenantId);
                } else if (manager instanceof org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager) {
                    //kept for backward compatibility. Remove once the session managers in carbon core are removed.
                    sessionManager = manager;
                    ((org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager) manager).
                            setOwnerTenantId(tenantId);
                } else {
                    sessionManager = new CarbonTomcatClusterableSessionManager(tenantId);
                    context.setManager(sessionManager);
                }

                Object alreadyinsertedSMMap = configurationContext.getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
                if(alreadyinsertedSMMap != null){
                    ((Map<String, Manager>) alreadyinsertedSMMap).put(context.getName(), sessionManager);
                }else{
                    sessionManagerMap.put(context.getName(), sessionManager);
                    configurationContext.setProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP,
                            sessionManagerMap);
                }

            } else {
                if (manager instanceof CarbonTomcatSessionManager) {
                    ((CarbonTomcatSessionManager) manager).setOwnerTenantId(tenantId);
                } else if (manager instanceof CarbonTomcatSessionPersistentManager){
                    ((CarbonTomcatSessionPersistentManager) manager).setOwnerTenantId(tenantId);
                    log.debug(manager.getInfo() +
                             " enabled Tomcat HTTP Session Persistent mode using " +
                             ((CarbonTomcatSessionPersistentManager) manager).getStore().getInfo());
                } else {
                    context.setManager(new CarbonTomcatSessionManager(tenantId));
                }
            }

            WebApplication webapp = new WebApplication(this, context, webappFile);
            webapp.setServletContextParameters(webContextParams);

            String bamEnable = recievePersistedWebappMetaData(webappFile, WebappsConstants.ENABLE_BAM_STATISTICS);
            if (bamEnable == null || "".equals(bamEnable)) {
                bamEnable = context.findParameter(WebappsConstants.ENABLE_BAM_STATISTICS);
                if (bamEnable == null || "".equals(bamEnable)) {
                    bamEnable = "false";
                }
            }
            webapp.addParameter(WebappsConstants.ENABLE_BAM_STATISTICS, bamEnable);

            webapp.setState("Started");

            WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                    webapp.getWebappFile().getAbsolutePath(),this.configurationContext);

            webApplicationsHolder.getStartedWebapps().put(filename, webapp);
            webApplicationsHolder.getFaultyWebapps().remove(filename);
            registerApplicationEventListeners(applicationEventListeners, context);
            /*ErrorPage page = new ErrorPage();
            page.setErrorCode(503);
            page.setLocation("/503.jsp");
            context.addErrorPage(page);*/
            log.info("Deployed webapp: " + webapp);
        } catch (Throwable e) {
            //catching a Throwable here to avoid web-apps crashing the server during startup
            StandardContext context = new StandardContext();
            context.setName(webappFile.getName());
            context.addParameter(WebappsConstants.FAULTY_WEBAPP, "true");
            WebApplication webapp = new WebApplication(this, context, webappFile);
            String msg = "Error while deploying webapp: " + webapp;
            log.error(msg, e);
            webapp.setFaultReason(new Exception(msg, e));

            WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                    webapp.getWebappFile().getAbsolutePath(), this.configurationContext);

            webApplicationsHolder.getFaultyWebapps().put(filename, webapp);
            webApplicationsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        } finally {
            privilegedCarbonContext.setApplicationName(null);
        }
    }

    private void registerApplicationEventListeners(List<Object> applicationEventListeners,
                                                   Context context) {
        Object[] originalEventListeners = context.getApplicationEventListeners();
        Object[] newEventListeners = new Object[originalEventListeners.length + applicationEventListeners.size()];
        if (originalEventListeners.length != 0) {
            System.arraycopy(originalEventListeners, 0, newEventListeners, 0, originalEventListeners.length);
            int i = originalEventListeners.length;
            for (Object eventListener : applicationEventListeners) {
                newEventListeners[i++] = eventListener;
            }
        } else {
            newEventListeners =
                    applicationEventListeners.toArray(new Object[applicationEventListeners.size()]);
        }
        context.setApplicationEventListeners(newEventListeners);
    }

    /**
     * Hot update an existing webapp. i.e., reload or redeploy a webapp archive which has been
     * updated
     *
     * @param webApplication            The webapp which needs to be hot updated
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    protected void handleHotUpdate(WebApplication webApplication,
                                   List<WebContextParameter> webContextParams,
                                   List<Object> applicationEventListeners) throws CarbonException {
        File webappFile = webApplication.getWebappFile();
        if (webappFile.isDirectory()) {  // webapp deployed as an exploded directory
            webApplication.reload();
            webApplication.setServletContextParameters(webContextParams);
            webApplication.setLastModifiedTime(webappFile.lastModified());
        } else { // webapp deployed from WAR
            // NOTE: context.reload() does not work for webapps deployed from WARs. Hence, we 
            // need to undeploy & redeploy.
            // See http://tomcat.apache.org/tomcat-5.5-doc/manager-howto.html#Reload An Existing Application
            undeploy(webApplication);
            handleWarWebappDeployment(webappFile, webContextParams, applicationEventListeners);
        }
        log.info("Redeployed webapp: " + webApplication);
    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The webapp file to be undeployed
     * @throws CarbonException If an error occurs while undeploying webapp
     */
    public void undeploy(File webappFile) throws CarbonException {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                webappFile.getAbsolutePath(), this.configurationContext);

        Map<String, WebApplication> deployedWebapps = webApplicationsHolder.getStartedWebapps();
        Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();
        Map<String, WebApplication> faultyWebapps = webApplicationsHolder.getFaultyWebapps();
        String fileName = webappFile.getName();

        removeMetadata(webappFile.getAbsolutePath());

        if (deployedWebapps.containsKey(fileName)) {
            undeploy(deployedWebapps.get(fileName));
        }
        // app = app.war and make sure check using both patterns.
        if (!fileName.endsWith(".war")) {
            String warFileName = fileName.concat(".war");
            if (deployedWebapps.containsKey(warFileName)) {
                undeploy(deployedWebapps.get(warFileName));
            }
        }
        //also checking the stopped webapps.
        else if (stoppedWebapps.containsKey(fileName)) {
            undeploy(stoppedWebapps.get(fileName));
        } else if (faultyWebapps.containsKey(fileName)) {
            undeploy(faultyWebapps.get(fileName));
        }

        clearFaultyWebapp(webappFile.getAbsolutePath());

    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The webapp file to be undeployed
     * @throws CarbonException If an error occurs while lazy unloading
     */
    public void lazyUnload(File webappFile) throws CarbonException {

        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                webappFile.getAbsolutePath(), this.configurationContext);
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Map<String, WebApplication> deployedWebapps = webApplicationsHolder.getStartedWebapps();
        Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();
        String fileName = webappFile.getName();
        if (deployedWebapps.containsKey(fileName)) {
            WebApplication deployWebapp = deployedWebapps.get(fileName);
            Context context = deployWebapp.getContext();
            privilegedCarbonContext.setApplicationName(
                    TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
            deployWebapp.lazyUnload();
        } else if (stoppedWebapps.containsKey(fileName)) {
            WebApplication stoppedWebapp = stoppedWebapps.get(fileName);
            Context context = stoppedWebapp.getContext();
            privilegedCarbonContext.setApplicationName(
                    TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
            stoppedWebapp.lazyUnload();
        }

        clearFaultyWebapp(webappFile.getAbsolutePath());
    }

    private void clearFaultyWebapp(String filePath) {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(filePath,this.configurationContext);
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Map<String, WebApplication> faultyWebapps = webApplicationsHolder.getFaultyWebapps();
        String fileName = WebAppUtils.getWebappName(filePath);
        if (faultyWebapps.containsKey(fileName)) {
            WebApplication faultyWebapp = faultyWebapps.get(fileName);
            Context context = faultyWebapp.getContext();
            privilegedCarbonContext.setApplicationName(
                    TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
            faultyWebapps.remove(fileName);
            log.info("Removed faulty webapp " + faultyWebapp);
        }
    }

    /**
     * This method reads from webapp meta files to check weather bam statistics are enabled.
     *
     * @param webappFile
     * @param propertyName
     * @return bam enable or disable
     * @throws AxisFault
     * @throws ArtifactMetadataException
     */
    protected String recievePersistedWebappMetaData(File webappFile, String propertyName) throws AxisFault, ArtifactMetadataException {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        String artifactDir = WebAppUtils.generateMetaFileDirName(webappFile.getAbsolutePath(), this.configurationContext);
        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_BASE_DIR +
                File.separator + artifactDir);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();
        return manager.loadParameter(webappFile.getName(), type, propertyName);
    }

    /**
     * This method stores the value in the webapp metadata file.
     *
     * @param webappFile
     * @param propertyName
     * @param value
     * @throws AxisFault
     * @throws ArtifactMetadataException
     */
    protected void setPersistedWebappMetaData(File webappFile, String propertyName, String value) throws AxisFault, ArtifactMetadataException {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        String artifactDir = WebAppUtils.generateMetaFileDirName(webappFile.getAbsolutePath(), this.configurationContext);
        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_BASE_DIR +
                File.separator + artifactDir);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();

        manager.setParameter(webappFile.getName(), type,
                propertyName, value, true);

    }

    private void removeMetadata(String artifactFilePath) throws CarbonException {
        try {
            WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                    artifactFilePath, this.configurationContext);
            Map<String, WebApplication> deployedWebapps = webApplicationsHolder.getStartedWebapps();
            Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();

            String keepMetadataHistory = null;
            String artifactFileName = WebAppUtils.getWebappName(artifactFilePath);
            if (deployedWebapps.containsKey(artifactFileName)) {
                keepMetadataHistory = deployedWebapps.get(artifactFileName).
                        findParameter(WebappsConstants.KEEP_WEBAPP_METADATA_HISTORY_PARAM);
            }

            if (keepMetadataHistory == null && stoppedWebapps.containsKey(artifactFileName)) {
                keepMetadataHistory = stoppedWebapps.get(artifactFileName).
                        findParameter(WebappsConstants.KEEP_WEBAPP_METADATA_HISTORY_PARAM);
            }

            if (!JavaUtils.isFalse(keepMetadataHistory)) {
                return;
            }

            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            String artifactDir = WebAppUtils.generateMetaFileDirName(artifactFilePath, this.configurationContext);
            ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP,
                    WebappsConstants.WEBAPP_METADATA_BASE_DIR +
                    File.separator + artifactDir);
            ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                    getMetadataManager();
            manager.deleteMetafile(artifactFileName, type);
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);

        }
    }

    /**
     * Undeploy a webapp
     *
     * @param webapp The webapp being undeployed
     * @throws CarbonException If an error occurs while undeploying
     */
    private void undeploy(WebApplication webapp) throws CarbonException {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                webapp.getWebappFile().getAbsolutePath(), this.configurationContext);
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Context context = webapp.getContext();
        privilegedCarbonContext.setApplicationName(
                TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
        webApplicationsHolder.undeployWebapp(webapp);
        log.info("Undeployed webapp: " + webapp);
    }


    protected String handleAppVersion(String warContext) {

        String path = warContext;

        int versionIndex = path.indexOf(WebappsConstants.VERSION_MARKER);
        // Don't confuse with AS versioning with Tomcat versioning
        // Here we  look for Tomcat versioning
        String version = null;
        if (versionIndex > -1) {
            version = path.substring(versionIndex + 2);
            path = path.substring(0, versionIndex);
        }

        // Now here we handle AS versioning
        if (path.contains(WebappsConstants.FWD_SLASH_REPLACEMENT)) {
            path = path.replaceAll("#", "/");
        }

        //Add Tomcat versioning value back to the context
        if (version != null) {
            return path + WebappsConstants.VERSION_MARKER + version;
        }

        return path;
    }

    private boolean isUnpackedDirExists(String warPath) {
        File dir = new File(warPath.replace(".war", ""));
        if (dir.exists() && dir.isDirectory()) {
            return true;
        }
        return false;
    }

}
