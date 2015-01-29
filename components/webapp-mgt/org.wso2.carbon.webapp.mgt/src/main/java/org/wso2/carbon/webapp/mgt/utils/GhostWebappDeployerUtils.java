/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.ContextConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.DeploymentFileDataWrapper;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.deployment.GhostArtifactRepository;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.TomcatGenericWebappsDeployer;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebContextParameter;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling lazy loading of webapps
 */
public class GhostWebappDeployerUtils {

    private static final Log log = LogFactory.getLog(GhostWebappDeployerUtils.class);

    // Map of ghost services which are currently being converted into actual services
    private static final String TRANSIT_GHOST_WEBAPP_MAP = "TransitGhostWebappMap";

    private GhostWebappDeployerUtils() {
        //disable external instantiation
    }

    /**
     * Removes the given ghostWebapp and deploys the actual webapp. Before deploying the actual
     * webapp this method will check whether the webapp of interest is already deployed and return
     * it if found.
     *
     * @param ghostWebapp          Existing ghost Webapp
     * @param configurationContext ConfigurationContext instance
     * @return newly deployed real webapp
     */
    public static WebApplication deployActualWebApp(WebApplication ghostWebapp,
                                                    ConfigurationContext configurationContext) {
        WebApplication newWebApp = null;
        /**
         * There can be multiple requests for the same ghost webapp depending on the level
         * of concurrency. Therefore we have to synchronize on the ghost webapp instance.
         */
        synchronized (ghostWebapp.getContextName().intern()) {
            // there can be situations in which the actual webapp is already deployed and
            // available in the webapps holder

            WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(
                    ghostWebapp.getWebappFile().getAbsolutePath(), configurationContext);

            if (webApplicationsHolder != null) {
                WebApplication deployedWebapp = webApplicationsHolder.getStartedWebapps().
                        get(ghostWebapp.getWebappFile().getName());
                if (deployedWebapp == null) {
                    return null;
                }

                if (!isGhostWebApp(deployedWebapp)) {
                    // if the webapp from webappsholder is not a ghost, return it
                    newWebApp = deployedWebapp;
                } else {
                    try {

                        GhostArtifactRepository ghostArtifactRepository =
                                GhostDeployerUtils.getGhostArtifactRepository(configurationContext.getAxisConfiguration());

                        if (ghostArtifactRepository == null) {
                            return null;
                        }
                        DeploymentFileDataWrapper dfdWrapper = ghostArtifactRepository.getDeploymentFileData(deployedWebapp
                                .getWebappFile().getPath());
                        Host host = DataHolder.getCarbonTomcatService().getTomcat().getHost();

                        if (dfdWrapper != null) {
                            // remove the existing webapp
                            log.info("Removing Ghost webapp and loading actual webapp : " +
                                    deployedWebapp.getWebappFile().getName());
                            DeploymentFileData dfd = dfdWrapper.getDeploymentFileData();
                            Map<String, WebApplication> transitGhostList =
                                    getTransitGhostWebAppsMap(configurationContext);
                            transitGhostList.put(deployedWebapp.getContextName(), deployedWebapp);

                            webApplicationsHolder.undeployWebapp(deployedWebapp);

                            TomcatGenericWebappsDeployer tomcatWebappDeployer =
                                    deployedWebapp.getTomcatGenericWebappsDeployer();

                            WebContextParameter serverUrlParam =
                                    new WebContextParameter("webServiceServerURL", CarbonUtils.
                                            getServerURL(ServerConfiguration.getInstance(),
                                                    configurationContext));

                            List<WebContextParameter> servletContextParameters =
                                    (ArrayList<WebContextParameter>) configurationContext.
                                            getProperty(CarbonConstants.
                                                    SERVLET_CONTEXT_PARAMETER_LIST);
                            if (servletContextParameters != null) {
                                servletContextParameters.add(serverUrlParam);
                            }

                            if (host != null &&
                                host.findChild(deployedWebapp.getContextName()) != null) {
                                host.removeChild(deployedWebapp.getContext());
                            }
                            ArrayList<Object> listeners = new ArrayList<Object>(1);
                            tomcatWebappDeployer.deploy(dfd.getFile(),
                                    servletContextParameters,
                                    listeners);
                            newWebApp = webApplicationsHolder.getStartedWebapps().get(dfd.getFile().
                                    getName());
                            newWebApp.setProperty(CarbonConstants.GHOST_WEBAPP_PARAM, "false");
                            // Check for jaxwebapps or jaggery apps
                            newWebApp.setProperty(WebappsConstants.WEBAPP_FILTER, deployedWebapp.getProperty(
                                    WebappsConstants.WEBAPP_FILTER));

                            newWebApp.setIsGhostWebapp(false);

                            //change the state from ghost to actual
                            ghostArtifactRepository.addDeploymentFileData(dfd, Boolean.FALSE);

                            transitGhostList.remove(newWebApp.getContextName());
                        }

                    } catch (CarbonException e) {
                        log.error("Error while loading actual webapp : " +
                                deployedWebapp.getWebappFile().getName(), e);
                    }

                }
                updateLastUsedTime(newWebApp);
            }
        }
        return newWebApp;
    }


    /**
     * Updates the last used timestamp of the given webapp. A new Paramter is created if it
     * doesn't already exists..
     *
     * @param webApplication to be updated
     */
    public static void updateLastUsedTime(WebApplication webApplication) {
        if (webApplication == null) {
            return;
        }
        try {
            webApplication.setProperty(CarbonConstants.WEB_APP_LAST_USED_TIME,
                    String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("Error while updating " + CarbonConstants.WEB_APP_LAST_USED_TIME +
                    " parameter in webapp : " + webApplication.getContextName(), e);
        }
    }

    /**
     * Get the map of services which are in transit. A particular service will be in this
     * map from the time the ghost service is removed from AxisConfig and the actual service is
     * deployed..
     *
     * @param cfgContext - ConfigurationContext that holds the transitMap property
     * @return returns a map of strings which are the context names of the webapps
     */
    public static synchronized Map<String, WebApplication> getTransitGhostWebAppsMap(
            ConfigurationContext cfgContext) {
        Map<String, WebApplication> transitMap = null;
        transitMap = (Map<String, WebApplication>) cfgContext.getProperty(TRANSIT_GHOST_WEBAPP_MAP);

        if (transitMap == null) {
            transitMap = new HashMap<String, WebApplication>();
            cfgContext.setProperty(TRANSIT_GHOST_WEBAPP_MAP, transitMap);
        }
        return transitMap;
    }

    /**
     * When a ghost webapp is removed from the webappsholder and the corresponding actual webapp is
     * deployed, there's a time interval in which there's no Webapp in the webapps holder for the
     * particular webapp context. Within this interval, if a request comes in to the webapp,
     * we have to somehow dispatch the webapp.
     * Within the above mentioned time interval, webapp is kept in a map. This method checks
     * whether the relevant webapp is available in that map and if it is found, returns the
     * name of the context of the webapp.
     *
     * @param ctxName - contextName of webapp
     * @param cfgContext - ConfigurationContext to get the transitGhost webapps map
     * @return the webapp
     */

    public static WebApplication dispatchWebAppFromTransitGhosts(String ctxName,
                                                                 ConfigurationContext cfgContext) {
        WebApplication actualWebapp = null;
        // get the map of ghost webapps which are being redeployed..
        Map<String, WebApplication> transitGhostMap = getTransitGhostWebAppsMap(cfgContext);

        if (ctxName != null && transitGhostMap.containsKey(ctxName)) {
            actualWebapp = transitGhostMap.get(ctxName);
        }
        return actualWebapp;
    }

    /**
     * When a ghost webapp is removed from the WebappsHolder, context name of that webapp is kept
     * temporary in a map. This method waits until the provided contrext name is removed from that
     * map. In other words, it waits until the actual webapp is deployed. After the actual
     * webapp is deployed, it is safe to forward the request further..
     *
     * @param contextName - contextName to bec checked with the ghostTransitMap
     * @param cfgContext  - ConfigurationContext to get the transitGhost webapps map
     */
    public static void waitForWebAppToLeaveTransit(String contextName,
                                                   ConfigurationContext cfgContext) {
        Map<String, WebApplication> transitGhostMap = getTransitGhostWebAppsMap(cfgContext);
        while (transitGhostMap.containsKey(contextName)) {
            // wait until the webapp is removed from ghost map
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }

    /**
     * Checks if the given webapp is a ghost webapp.
     *
     * @param webApplication - the webapp to be checked for ghost param
     * @return true if the webapp is ghost
     */
    public static boolean isGhostWebApp(WebApplication webApplication) {
        if (webApplication == null) {
            return false;
        }
        String ghostParam = (String) webApplication.getProperty(CarbonConstants.GHOST_WEBAPP_PARAM);
        return ghostParam != null && "true".equals(ghostParam);
    }

    /**
     * Creates the Ghost webapplication by reading the ghost metadata file. The created object will
     * contain the basic metadata of the webapplication that are enough to show in the webapp info
     * page.
     *
     * @param ghostFile    - ghost metadata file
     * @param originalFile - original webapp file
     * @param configContext   - configContext to get tenant details and webapp deployer instance
     * @return - WebApplication which is created
     */
    public static WebApplication addGhostWebApp(File ghostFile, File originalFile,
                                                TomcatGenericWebappsDeployer applicationDeployer,
                                                ConfigurationContext configContext) {
        WebApplication ghostWebApp = null;

        OMElement webAppElm;
        try {
            InputStream xmlInputStream = new FileInputStream(ghostFile);
            webAppElm = new StAXOMBuilder(xmlInputStream).getDocumentElement();
        } catch (Exception e) {
            log.error("Error while parsing ghost XML file : " + ghostFile.getAbsolutePath());
            return null;
        }

        try {
            String contextName = webAppElm.
                    getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_NAME));
            String contextPath = webAppElm.
                    getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_WEBAPP_CONTEXT_PATH));
            String jaxContext = webAppElm.getAttributeValue(new QName("jaxContext"));
            String jaxMapping = webAppElm.getAttributeValue(new QName("jaxMapping"));
            Context context = new StandardContext();
            context.setName(contextName);
            context.setPath(contextPath);
            if(jaxMapping != null && jaxContext!= null){
                Wrapper jaxContextWrapper = context.createWrapper();
                jaxContextWrapper.setName(jaxMapping);
                jaxContextWrapper.setLoadOnStartup(1);
                jaxContextWrapper.setServletClass(DefaultServlet.class.getName());
                context.addChild(jaxContextWrapper);
                context.addServletMapping(jaxContext, jaxMapping);
            }

            String filterProp = webAppElm.
                    getAttributeValue(new QName(WebappsConstants.WEBAPP_FILTER));

            if(applicationDeployer == null) {
                if (WebappsConstants.WEBAPP_FILTER_PROP.equals(filterProp)) {
                    applicationDeployer = (TomcatGenericWebappsDeployer)
                            configContext.getProperty(WebappsConstants.TOMCAT_GENERIC_WEBAPP_DEPLOYER);
                } /*else if (WebappsConstants.JAX_WEBAPP_FILTER_PROP.equals(filterProp)) {
                    applicationDeployer = (TomcatGenericWebappsDeployer)
                            configContext.getProperty(WebappsConstants.TOMCAT_JAX_WEBAPP_DEPLOYER);
                }*/ else if (WebappsConstants.JAGGERY_WEBAPP_FILTER_PROP.equals(filterProp)) {
                    applicationDeployer = (TomcatGenericWebappsDeployer)
                            configContext.getProperty(WebappsConstants.TOMCAT_JAGGERY_WEBAPP_DEPLOYER);
                }
            }

            if (applicationDeployer != null) {
                String displayName = webAppElm.
                        getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_WEBAPP_DISPLAY_NAME));

                // We have to add a context for the ghost webapp in tomcat so that when Tomcat's CoyoteAdaptor
                // searches for available contexts, this will result in not null for a request for ghost webapp
                String dummyContextPath = getDummyContextDirectoryPath(contextName, configContext.getAxisConfiguration());

                File dummyCtxFolder = new File(dummyContextPath);
                if (!dummyCtxFolder.exists() && !dummyCtxFolder.mkdirs()) {
                    log.error("Error while creating dummy context folder at : " + dummyContextPath);
                    return null;
                }
                if (dummyContextPath != null) {
                    String hostName = WebAppUtils.getMatchingHostName(WebAppUtils.getWebappDirPath(originalFile.getAbsolutePath()));
                    CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
                    if (carbonTomcatService != null) {
                        Host host = (Host) carbonTomcatService.getTomcat().getEngine().findChild(hostName);
                        if (host == null) {
                            host = (Host) carbonTomcatService.getTomcat().
                                    getEngine().findChild(WebAppUtils.getDefaultHost());
                        }
                        if (host.findChild(contextName) == null) {
                            context.setDocBase(dummyContextPath);
                            ContextConfig ctxCfg = new ContextConfig();
                            context.addLifecycleListener(ctxCfg);
                            context.addLifecycleListener(new DefaultConfListener(displayName));
                            host.addChild(context);
                        }
                    }

                }

                ghostWebApp = new WebApplication(applicationDeployer, context, originalFile);
                ghostWebApp.setProperty(CarbonConstants.GHOST_WEBAPP_PARAM, "true");
                ghostWebApp.setIsGhostWebapp(true);
                if (displayName != null) {
                    ghostWebApp.setDisplayName(displayName);
                }

                String webAppFile = webAppElm.
                        getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_WEBAPP_FILE));
                if (webAppFile != null) {
                    ghostWebApp.setProperty(WebappsConstants.APP_FILE_NAME, webAppFile);
                }

                String lastModifiedTime = webAppElm.
                        getAttributeValue(new QName(CarbonConstants.GHOST_ATTR_LAST_MODIFIED_TIME));

                if (lastModifiedTime != null) {
                    ghostWebApp.setLastModifiedTime(Long.parseLong(lastModifiedTime));
                }

                // Check for jaxWebapps filter property
                if (filterProp != null) {
                    ghostWebApp.setProperty(WebappsConstants.WEBAPP_FILTER, filterProp);
                }

            } else {
                log.error("Unable to retrieve " + filterProp + " Application Deployer instance.");
            }

        } catch (Exception e) {
            log.error("Error while creating Ghost Webapp from Ghost File : " +
                    ghostFile.getAbsolutePath(), e);
        }

        return ghostWebApp;
    }

    //Setting the webapp displayname at goast app deployment
    private static class DefaultConfListener implements LifecycleListener {
        private String displayName;

        private DefaultConfListener(String displayName) {
            this.displayName = displayName;
        }

        public void lifecycleEvent(LifecycleEvent event) {
            if (Lifecycle.START_EVENT.equals(event.getType())) {
                ((Context) event.getLifecycle()).setDisplayName(this.displayName);
            }
        }
    }

    /**
     * Creates the ghost file for the current web application. This file will contain basic meadata
     * such as Webapp name, context name, display name, war file name, last modified time and
     * noOf active sessions. These information for a webapp is enough to show it in the
     * webapp info page
     *
     * @param webApplication - Webapplication to be serialized
     * @param axisConfig     - AxisConfiguration instance
     * @param webappPath     - Absolute path to the webapp artifact
     */
    public static void serializeWebApp(WebApplication webApplication,
                                       AxisConfiguration axisConfig, String webappPath) {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();

        String contextPath = webApplication.getContextName();
        String jaxContext = null;
        String jaxMapping = null;
        for (Container container : webApplication.getContext().findChildren()) {
            if(((StandardWrapper) container).getServletClass().equals(
                    "org.apache.cxf.transport.servlet.CXFServlet")) {
                jaxContext = (((StandardWrapper) container).findMappings())[0];
                jaxMapping = ((StandardWrapper) container).getServletName();
                if(jaxContext.endsWith("/*")) {
                    jaxContext = jaxContext.substring(0, jaxContext.indexOf("/*"));
                }
                break;
            }
        }

        // webapp element
        OMElement webappEle = omFactory
                .createOMElement(new QName(CarbonConstants.GHOST_WEBAPP));
        webappEle.addAttribute(CarbonConstants.GHOST_ATTR_NAME,
                webApplication.getContext().getName(), null);

        // get the service type
        if (contextPath != null) {
            webappEle.addAttribute(CarbonConstants.GHOST_ATTR_WEBAPP_CONTEXT_PATH,
                    contextPath, null);
        }

        if(jaxContext != null && jaxMapping != null) {
            webappEle.addAttribute("jaxContext", jaxContext, null);
            webappEle.addAttribute("jaxMapping", jaxMapping, null);
        }

        // get the service type
        String displayName = webApplication.getDisplayName();
        if (displayName != null) {
            webappEle.addAttribute(CarbonConstants.GHOST_ATTR_WEBAPP_DISPLAY_NAME,
                    displayName, null);
        }

        // get the service type
        String file = webApplication.getWebappFile().getName();
        if (file != null) {
            webappEle.addAttribute(CarbonConstants.GHOST_ATTR_WEBAPP_FILE, file, null);
        }

        // get the service type
        String lastModifiedTime = String.valueOf(webApplication.getLastModifiedTime());
        if (lastModifiedTime != null) {
            webappEle.addAttribute(CarbonConstants.GHOST_ATTR_LAST_MODIFIED_TIME,
                    lastModifiedTime, null);
        }

//        String filterProp = (String) webApplication.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If this is a JAX webapp, then add the filter property to ghost file..
        webappEle.addAttribute(WebappsConstants.WEBAPP_FILTER,
                (String) webApplication.getProperty(WebappsConstants.WEBAPP_FILTER), null);

        // Now create a ghostFile and serialize the created OMElement
        String ghostMetafilesDirPath = CarbonUtils.getGhostMetafileDir(axisConfig);
        if (ghostMetafilesDirPath == null) {
            return;
        }
        String ghostPath = ghostMetafilesDirPath +
                File.separator + CarbonConstants.GHOST_WEBAPPS_FOLDER;
        File ghostFolder = new File(ghostPath);
        if (!ghostFolder.exists() && !ghostFolder.mkdir()) {
            log.error("Error while creating ghostWebapps folder at : " + ghostPath);
            return;
        }

        FileOutputStream fos = null;
        try {
            File serviceFile = new File(ghostPath + File.separator +
                    calculateGhostFileName(webappPath,
                            axisConfig.getRepository().getPath()));
            fos = new FileOutputStream(serviceFile);
            webappEle.serialize(fos);
            fos.flush();
        } catch (Exception e) {
            log.error("Error while serializing OMElement for Ghost Webapp", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Error while closing the file output stream", e);
                }
            }
        }
    }

    /**
     * Creates a File instance for the ghost service according to the original file name..
     *
     * @param fileName   - original service file name
     * @param axisConfig - AxisConfiguration instance
     * @return - File instance created
     */
    public static File getGhostFile(String fileName, AxisConfiguration axisConfig) {
        String ghostMetafilesDirPath = CarbonUtils.getGhostMetafileDir(axisConfig);
        if (ghostMetafilesDirPath == null) {
            return null;
        }
        return new File(ghostMetafilesDirPath + File.separator +
                CarbonConstants.GHOST_WEBAPPS_FOLDER + File.separator +
                calculateGhostFileName(fileName, axisConfig.getRepository().getPath()));
    }

    /**
     * Calculate the ghost file name using the original file name.
     *
     * @param fileName - original file name
     * @param repoPath - path of the axis2 repository
     * @return - derived ghost file name
     */
    public static String calculateGhostFileName(String fileName, String repoPath) {
        String javaTmpDir = System.getProperty("java.io.tmpdir");
        String cappUnzipPath  = javaTmpDir.endsWith(File.separator) ?
                javaTmpDir + "carbonapps" :
                javaTmpDir + File.separator + "carbonapps";
        fileName = separatorsToUnix(fileName);
        cappUnzipPath = separatorsToUnix(cappUnzipPath);
        repoPath = separatorsToUnix(
                new File(repoPath).getAbsolutePath().concat(File.separator));

        String ghostFileName = null;

        if (fileName.startsWith(repoPath)) {
            // first drop the repo path
            ghostFileName = fileName.substring(repoPath.length());

            //Check whether artifact is from a CApp
        } else if (fileName.startsWith(cappUnzipPath)) {
            ghostFileName = fileName.substring(cappUnzipPath.length());

            // now the ghostFileName looks like following string. We need remove the temp car file name as well.
            //  /14144224998641144capp_1.0.0.car/datasource-test_1.0.0/datasource-test-1.0.0.aar
            ghostFileName = ghostFileName.substring(ghostFileName.indexOf(".car/") + 5);
        }

        if (ghostFileName != null) {
            // then remove the extension
            if (!(new File(fileName).isDirectory()) && (ghostFileName.lastIndexOf('.') != -1)) {
                ghostFileName = ghostFileName.substring(0, ghostFileName.lastIndexOf('.'));
            }
            // adjust the path for windows..
            if (File.separatorChar == '\\') {
                ghostFileName = ghostFileName.replace('\\', '/');
            }
            // replace '/' with '_'
            ghostFileName = ghostFileName.replace('/', '_');
            // ghost file is always an XML
            ghostFileName += ".xml";
        }

        return ghostFileName;
    }

    /**
     * This method is used to find a webapplication from the webapps holder by comparing the webapp
     * file absolute path.
     *
     * @param configurationContext - configurationContext instance that has the webapps holder property
     * @param webappFilePath           - absolute path of the webapp to be compared with other webapps in the holder
     * @return webapplicatioon found by the comparison or null
     */
    public static WebApplication findDeployedWebapp(ConfigurationContext configurationContext,
                                                    String webappFilePath) {

        try {
            WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(webappFilePath, configurationContext);

            if (webApplicationsHolder != null) {
                return webApplicationsHolder.getStartedWebapps().get(WebAppUtils.getWebappName(webappFilePath));
            }

        } catch (Exception e) {
            log.error("Error while retrieving the webapp from webappsHolder..", e);
        }
        return null;
    }

    /**
     * Method which will return the dummy context directory path for current tenant which will be
     * created in /tmp/tenants/ directory
     *
     * @param contextName - context name
     * @param axisConfig  - axisConfig used to get tenant info
     * @return - dummyContextDirPath
     */
    public static String getDummyContextDirectoryPath(String contextName,
                                                      AxisConfiguration axisConfig) {
        if (contextName.contains("/t/")) {
            String tenantCtx = "/t/" + TenantAxisUtils.getTenantDomain(contextName);
            if (contextName.contains("/" + WebappsConstants.WEBAPP_PREFIX + "/")) {
                tenantCtx = tenantCtx + File.separator + WebappsConstants.WEBAPP_PREFIX;
            } else if (contextName.contains("/" + WebappsConstants.JAGGERY_APPS_PREFIX + "/")) {
                tenantCtx = tenantCtx + File.separator + WebappsConstants.JAGGERY_APPS_PREFIX;
            } else if (contextName.contains(("/" + WebappsConstants.JAX_WEBAPPS_PREFIX + "/"))) {
                tenantCtx = tenantCtx + File.separator + WebappsConstants.JAX_WEBAPPS_PREFIX;
            } else {
                return null;
            }
            contextName = contextName.substring(tenantCtx.length() + 1);
        }

        String tenantTmpDirPath = CarbonUtils.getTenantTmpDirPath(axisConfig) + File.separator +
                CarbonConstants.GHOST_WEBAPPS_FOLDER ;
        File tenantTmpDir = new File(tenantTmpDirPath);

        if (!tenantTmpDir.exists() && !tenantTmpDir.mkdir()) {
            log.error("Error while creating tenant temporary directory at : " + tenantTmpDirPath);
            return null;
        }

        return tenantTmpDirPath + File.separator + contextName;
    }

    /**
     * Calculate the dummy context file name using the original file name.
     *
     * @param fileName   - original file name
     * @param axisConfig - axisConfig
     * @return - derived context file
     */
    public static File getDummyContextFile(String fileName, AxisConfiguration axisConfig) {
        File dummyContextFile = null;
        String dummyContextName;
        String repoPath = axisConfig.getRepository().getPath();
        if (fileName != null && fileName.startsWith(repoPath)) {
            // first drop the repo path
            dummyContextName = fileName.
                    substring((repoPath + File.separator + WebappsConstants.
                            WEBAPP_DEPLOYMENT_FOLDER).length());
            // then remove the extension
            if (dummyContextName.lastIndexOf('.') != -1) {
                dummyContextName = dummyContextName.substring(0, dummyContextName.lastIndexOf('.'));
            }
            // adjust the path for windows..
            if (File.separatorChar == '\\') {
                dummyContextName = dummyContextName.replace('\\', '/');
            }

            String tenantTmpDirPath = CarbonUtils.getTenantTmpDirPath(axisConfig);
            dummyContextFile = new File(tenantTmpDirPath + File.separator +
                    CarbonConstants.GHOST_WEBAPPS_FOLDER + File.separator +
                    dummyContextName);
        }
        return dummyContextFile;
    }

    /**
     * Method to check if the undeploying of ghost related files needs to be skipped
     *
     * @param fileName name of the webapp file
     * @return true or false
     */
    public static boolean skipUndeploy(String fileName) {
        // check for .svn and .meta files to be skipped. these are created by depsynch
        if (fileName.endsWith(".svn") || fileName.endsWith(".meta")) {
            return true;
        }
        File warFile = new File(fileName + ".war");
        return warFile.exists();
    }

    public static void deployGhostArtifacts(ConfigurationContext configContext) {
        // load the ghost service group
        File[] ghostMetaArtifacts;
        File ghostMetafilesDir = new File(CarbonUtils.
                getGhostMetafileDir(configContext.getAxisConfiguration()) + File.separator +
                                          CarbonConstants.GHOST_WEBAPPS_FOLDER);

        if (ghostMetafilesDir.exists()) {
            ghostMetaArtifacts = ghostMetafilesDir.listFiles();
        } else {
            return;
        }

        for (File ghostFile : ghostMetaArtifacts) {
            if (!ghostFile.getPath().endsWith(".svn")) {
                try {
                    WebApplication ghostWebApplication = GhostWebappDeployerUtils.addGhostWebApp(
                            ghostFile, ghostFile, null, configContext);
                    WebApplicationsHolder webappsHolder = WebAppUtils.getWebappHolder(ghostFile.getAbsolutePath(), configContext);

                    String ghostWebappFileName = ghostWebApplication.getWebappFile().getName();
                    String webappFileProperty = (String) ghostWebApplication.
                            getProperty(WebappsConstants.APP_FILE_NAME);

                    if (!(webappsHolder.getStartedWebapps().containsKey(ghostWebappFileName) ||
                        webappsHolder.getStartedWebapps().containsKey(webappFileProperty))) {
                        log.info("Deploying Ghost webapp : " + webappFileProperty);
                        webappsHolder.getStartedWebapps().put(ghostWebappFileName, ghostWebApplication);
                        webappsHolder.getFaultyWebapps().remove(ghostWebappFileName);
                    }
                } catch (Exception e) {
                    log.error("Error while deploying Ghost webapp :"+ghostFile.getPath());
                }
            }
        }
    }

    public static String  separatorsToUnix(String path) {
        if (path == null || !path.contains("\\")) {
            return path;
        }
        return path.replace("\\", "/");

    }
}
