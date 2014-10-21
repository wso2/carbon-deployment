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
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

import java.io.File;
import java.lang.management.ManagementPermission;
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
    protected WebApplicationsHolder webappsHolder;
    protected Map<String, CarbonTomcatClusterableSessionManager> sessionManagerMap =
            new ConcurrentHashMap<String, CarbonTomcatClusterableSessionManager>();

    /**
     * Constructor
     *
     * @param webContextPrefix The Web context prefix
     * @param tenantId         The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain     The tenant domain of the tenant to whom this deployer belongs to
     * @param webappsHolder    WebApplicationsHolder
     */
    public TomcatGenericWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        WebApplicationsHolder webappsHolder,
                                        ConfigurationContext configurationContext) {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.webContextPrefix = webContextPrefix;
        this.webappsHolder = webappsHolder;
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
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();

        if (webappFile.isDirectory()) {
            privilegedCarbonContext.setApplicationName(webappName);
        } else if (webappName.contains(".war") || webappName.contains(".zip")) {
            //removing extension to get app name for .war and .zip
            privilegedCarbonContext.setApplicationName(webappName.substring(0, webappName.indexOf(".war")));
        }
        long lastModifiedTime = webappFile.lastModified();
        WebApplication deployedWebapp =
                webappsHolder.getStartedWebapps().get(webappFile.getName());

        WebApplication deployedUnpackedWebapp = null;
        if(deployedWebapp == null && (webappName.contains(".war") || webappName.contains(".zip"))){
            String unpackDirName = webappName.endsWith(".war") ? webappName.replace(".war", "") : webappName;
            unpackDirName = webappName.endsWith(".zip") ? unpackDirName.replace(".zip", "") : unpackDirName;
            deployedUnpackedWebapp = webappsHolder.getStartedWebapps().get(unpackDirName);
        }

        WebApplication undeployedWebapp =
                webappsHolder.getStoppedWebapps().get(webappFile.getName());
        WebApplication faultyWebapp =
                webappsHolder.getFaultyWebapps().get(webappFile.getName());

        WebApplication faultyUnpackedWebapp = null;
        if (deployedWebapp == null && (webappName.contains(".war") || webappName.contains(".zip"))) {
            String unpackDirName = webappName.endsWith(".war") ? webappName.replace(".war", "") : webappName;
            unpackDirName = webappName.endsWith(".zip") ? unpackDirName.replace(".zip", "") : unpackDirName;
            faultyUnpackedWebapp = webappsHolder.getFaultyWebapps().get(unpackDirName);
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
            clearFaultyWebapp(faultyUnpackedWebapp.getWebappFile().getName());
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
        if (filename.equals("ROOT.war")) {  // FIXME: This is not working for some reason!
            if (webContextPrefix != null && !webContextPrefix.endsWith("/")) {
                warContext = "/";
            }
        } else {
            warContext = filename.substring(0, filename.indexOf(".war"));
        }
        if (!warContext.equals("/") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
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
        if (filename.equals("ROOT")) {
            if (webContextPrefix != null && !webContextPrefix.endsWith("/")) {
                warContext = "/";
            }
        } else {
            warContext = filename;
        }
        if (!warContext.equals("/") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        }

        String contextPath = handleAppVersion(webContextPrefix + warContext);

        handleWebappDeployment(webappDir, contextPath,
                webContextParams, applicationEventListeners);
    }

    protected void handleWebappDeployment(File webappFile, String contextStr,
                                          List<WebContextParameter> webContextParams,
                                          List<Object> applicationEventListeners) throws CarbonException {
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String filename = webappFile.getName();
        try {
            Context context =
                    DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath());
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
                CarbonTomcatClusterableSessionManager sessionManager;

                if (manager instanceof CarbonTomcatClusterableSessionManager) {
                    sessionManager = (CarbonTomcatClusterableSessionManager) manager;
                    sessionManager.setOwnerTenantId(tenantId);
                } else {
                    sessionManager = new CarbonTomcatClusterableSessionManager(tenantId);
                    context.setManager(sessionManager);
                }

                Object alreadyinsertedSMMap = configurationContext.getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
                if(alreadyinsertedSMMap != null){
                    ((Map<String, CarbonTomcatClusterableSessionManager>) alreadyinsertedSMMap).put(context.getName(), sessionManager);
                }else{
                    sessionManagerMap.put(context.getName(), sessionManager);
                    configurationContext.setProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP,
                            sessionManagerMap);
                }

            } else {
                if (manager instanceof CarbonTomcatSessionManager) {
                    ((CarbonTomcatSessionManager) manager).setOwnerTenantId(tenantId);
                } else {
                    context.setManager(new CarbonTomcatSessionManager(tenantId));
                }
            }

            context.setReloadable(false);
            WebApplication webapp = new WebApplication(this, context, webappFile);
            webapp.setServletContextParameters(webContextParams);

            String bamEnable =   recievePersistedWebappMetaData(webappFile.getName(), WebappsConstants.ENABLE_BAM_STATISTICS);
            if(bamEnable == null || "".equals(bamEnable))  {
                bamEnable = context.findParameter(WebappsConstants.ENABLE_BAM_STATISTICS);
                if(bamEnable == null || "".equals(bamEnable))  {
                    bamEnable="false";
                }
            }
            webapp.addParameter(WebappsConstants.ENABLE_BAM_STATISTICS, bamEnable);

            webapp.setState("Started");
            webappsHolder.getStartedWebapps().put(filename, webapp);
            webappsHolder.getFaultyWebapps().remove(filename);
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
            webappsHolder.getFaultyWebapps().put(filename, webapp);
            webappsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        }  finally {
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
        Map<String, WebApplication> deployedWebapps = webappsHolder.getStartedWebapps();
        Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();
        Map<String, WebApplication> faultyWebapps = webappsHolder.getFaultyWebapps();
        String fileName = webappFile.getName();

        removeMetadata(fileName);

        if (deployedWebapps.containsKey(fileName)) {
            undeploy(deployedWebapps.get(fileName));
        }
        // app = app.war and make sure check using both patterns.
        if(!fileName.endsWith(".war")){
            String warFileName =  fileName.concat(".war");
            if(deployedWebapps.containsKey(warFileName)){
                undeploy(deployedWebapps.get(warFileName));
            }
        }
        //also checking the stopped webapps.
        else if (stoppedWebapps.containsKey(fileName)) {
            undeploy(stoppedWebapps.get(fileName));
        }

        else if (faultyWebapps.containsKey(fileName)) {
            undeploy(faultyWebapps.get(fileName));
        }

        clearFaultyWebapp(fileName);

    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The webapp file to be undeployed
     * @throws CarbonException If an error occurs while lazy unloading
     */
    public void lazyUnload(File webappFile) throws CarbonException {
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Map<String, WebApplication> deployedWebapps = webappsHolder.getStartedWebapps();
        String fileName = webappFile.getName();
        if (deployedWebapps.containsKey(fileName)) {
            WebApplication deployWebapp = deployedWebapps.get(fileName);
            Context context = deployWebapp.getContext();
            privilegedCarbonContext.setApplicationName(
                    TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
            deployWebapp.lazyUnload();
        }

        clearFaultyWebapp(fileName);
    }

    private void clearFaultyWebapp(String fileName) {
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Map<String, WebApplication> faultyWebapps = webappsHolder.getFaultyWebapps();
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
     * @param artifactName
     * @param propertyName
     * @return  bam enable or disable
     * @throws AxisFault
     * @throws ArtifactMetadataException
     */
    protected String recievePersistedWebappMetaData(String artifactName, String propertyName) throws AxisFault, ArtifactMetadataException {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_DIR);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();
        return manager.loadParameter(artifactName ,type,propertyName);
    }

    /**
     * This method stores the value in the webapp metadata file.
     * @param artifactName
     * @param propertyName
     * @param value
     * @throws AxisFault
     * @throws ArtifactMetadataException
     */
    protected void setPersistedWebappMetaData(String artifactName, String propertyName, String value) throws AxisFault, ArtifactMetadataException {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_DIR);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();

        manager.setParameter(artifactName, type,
                propertyName, value, true);

    }

    private void removeMetadata(String artifactFileName) throws CarbonException {
        try {
            Map<String, WebApplication> deployedWebapps = webappsHolder.getStartedWebapps();
            Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();

            String keepMetadataHistory = null;
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
            ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_DIR);
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
        PrivilegedCarbonContext privilegedCarbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        Context context = webapp.getContext();
        privilegedCarbonContext.setApplicationName(
                TomcatUtil.getApplicationNameFromContext(context.getBaseName()));
        webappsHolder.undeployWebapp(webapp);
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

    private boolean isUnpackedDirExists(String warPath){
        File dir = new File(warPath.replace(".war",""));
        if(dir.exists() && dir.isDirectory()){
            return true;
        }
        return false;
    }
}
