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

package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractWebappDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(AbstractWebappDeployer.class);
    protected String webappsDir;
    protected String extension;
    protected TomcatGenericWebappsDeployer tomcatWebappDeployer;
    protected final List<WebContextParameter> servletContextParameters = new ArrayList<WebContextParameter>();
    protected ConfigurationContext configContext;
    protected Map<String, WebApplicationsHolder> webApplicationsHolderMap;
    private String[] defaultWatchedResources;

    public void init(ConfigurationContext configCtx) {
        this.configContext = configCtx;
        String repoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        File webappsDirFile = new File(repoPath + File.separator + webappsDir);
        if (!webappsDirFile.exists() && !webappsDirFile.mkdirs()) {
            log.warn("Could not create directory " + webappsDirFile.getAbsolutePath());
        }
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.
                getThreadLocalCarbonContext();
        int tenantId = privilegedCarbonContext.getTenantId();
        String tenantDomain = privilegedCarbonContext.getTenantDomain();
        String webContextPrefix = (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/" + this.webappsDir + "/" :
                "";

        // try to get the webapps holder list from config ctx. if null, create one..
        webApplicationsHolderMap = (Map<String, WebApplicationsHolder>)
                configCtx.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
        if (webApplicationsHolderMap == null) {
            webApplicationsHolderMap = new HashMap<String, WebApplicationsHolder>();
            configCtx.setProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST, webApplicationsHolderMap);
        }
        if (!webApplicationsHolderMap.containsKey(webappsDir) && WebAppUtils.appBases.contains(webappsDir)) {
            WebApplicationsHolder webApplicationsHolder = new WebApplicationsHolder(new File(webappsDir));
            webApplicationsHolderMap.put(webappsDir, webApplicationsHolder);
        }
        tomcatWebappDeployer = createTomcatGenericWebappDeployer(webContextPrefix, tenantId, tenantDomain);
        configCtx.setProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST, servletContextParameters);

        //setting default watched releases
        defaultWatchedResources = new String[]{"WEB-INF" + File.separator + "web.xml",
                "WEB-INF" + File.separator + "lib",
                "WEB-INF" + File.separator + "classes"};
    }

    protected abstract TomcatGenericWebappsDeployer createTomcatGenericWebappDeployer(
            String webContextPrefix, int tenantId, String tenantDomain);

    protected abstract String getType();

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        // We now support for exploded webapp deployment, so we have to check if unpackedWar
        // files are getting deployed again, which will cause conflict at tomcat level.
        if (!isSkippedWebapp(deploymentFileData.getFile())) {
            deployThisWebApp(deploymentFileData);
        }
    }

    private void deployThisWebApp(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        try {
            // Object can be of listeners interfaces in javax.servlet.*
            ArrayList<Object> listeners = new ArrayList<Object>(1);
            //            listeners.add(new CarbonServletRequestListener());
            tomcatWebappDeployer.deploy(deploymentFileData.getFile(),
                    (ArrayList<WebContextParameter>) configContext.getProperty(
                            CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST),
                    listeners);
            super.deploy(deploymentFileData);

        } catch (Exception e) {
            String msg = "Error occurred while deploying webapp : " + deploymentFileData.getFile().getAbsolutePath();
            // removing faulty artifacts deployed by CApps
            if (deploymentFileData.getAbsolutePath().contains("carbonapps")) {
                String failedArtifact = deploymentFileData.getFile().getName();
                WebApplicationsHolder webappsHolder = WebAppUtils.getWebappHolder(
                        deploymentFileData.getAbsolutePath(), configContext);
                webappsHolder.getFaultyWebapps().remove(failedArtifact);
            }
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        File unpackedFile;
        File warFile;

        if (fileName.endsWith(".war")) {
            warFile = new File(fileName);
            // Since CApp extracted artifact is not in hot deployemnt directory the file will available in the
            // extracted location though it need to me undeployed
            if (fileName.contains("carbonapps")) {
                handleUndeployment(fileName, warFile);
            } else {
                if (!warFile.exists()) {
                    handleUndeployment(fileName, warFile);
                } else {
                    handleUndeployment(fileName, warFile);
                    handleRedeployment(warFile);
                }
            }
        } else {
            warFile = new File(fileName.concat(".war"));
            unpackedFile = new File(fileName);
            if (!unpackedFile.exists()) {
                if (!warFile.exists()) {
                    handleUndeployment(fileName, unpackedFile);
                } else {
                    handleUndeployment(fileName, unpackedFile);
                    handleRedeployment(warFile);
                }
            } else {
                if (isWatchedResourceChanged(fileName, unpackedFile)) {
                    // if watchedResources are modified, reload the context
                    Context context = getWebappContext(unpackedFile);
                    if (context != null) {
                        context.reload();
                        log.info("Reloaded Context with name: " + context.getName());
                    }
                }
            }
        }
    }

    private boolean isWatchedResourceChanged(String fileName, File file) {
        //check for default watched resources
        for (String watchedResource : defaultWatchedResources) {
            File watchedResourceFile = new File(fileName + File.separator + watchedResource);
            if (watchedResourceFile.lastModified() > file.lastModified()) {
                return true;
            }
        }
        //check for explicitly mentioned watched releases
        Context context = getWebappContext(file);
        if (context != null) {
            String[] watchedResources = context.findWatchedResources();
            for (String watchedResource : watchedResources) {
                File watchedResourceFile = new File(fileName + File.separator + watchedResource);
                if (watchedResourceFile.lastModified() >= file.lastModified()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Context getWebappContext(File file) {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(file.getAbsolutePath(), configContext);

        Map<String, WebApplication> webappMap = webApplicationsHolder.getStartedWebapps();
        WebApplication webapp;
        if ((webapp = webappMap.get(file.getName() + ".war")) != null
                || (webapp = webappMap.get(file.getName())) != null) {
            return webapp.getContext();

        }
        webappMap = webApplicationsHolder.getStoppedWebapps();
        if ((webapp = webappMap.get(file.getName() + ".war")) != null
                || (webapp = webappMap.get(file.getName())) != null) {
            return webapp.getContext();

        }
        return null;

    }

    @Override
    public void cleanup() throws DeploymentException {
        for (String filePath : deploymentFileDataMap.keySet()) {
            try {
                tomcatWebappDeployer.lazyUnload(new File(filePath));
            } catch (CarbonException e) {
                String msg = "Error occurred during cleaning up webapps";
                log.error(msg, e);
                throw new DeploymentException(msg, e);
            }
        }
    }

    private boolean isSkippedWebapp(File webappFile) {
        String webappFilePath = webappFile.getPath();
        boolean isSkipped = true;

        if (webappFilePath.contains(WebappsConstants.VERSION_MARKER)) {
            log.info("Unsupported file path format : " + webappFile);
            return true;
        }

        // Here we are checking WebappDeployer with .war extension or null extension
        // If foo.war and foo dir is found, then we will allow  .war based WebappDeployer to deploy that webapp.
        // If only foo dir found then directory based WebappDeployer will deploy that webapp.
        if ("war".equals(extension) || webappFilePath.endsWith(".war")) {
            // We should not deploy .WAR files inside a another application. e.g- webapps/mvcapp/newapp.war
            return isInsideAnotherApp(webappFilePath);
        } else {
            // return false if jaxwebapp or jaggery app is being deployed
            if (webappFilePath.contains("jaxwebapps") || webappFilePath.contains("jaggeryapps")
                    || webappFilePath.contains("carbonapps")) {
                return false;
            }


            // if it's a dir  then make sure it is not a unpacked content of .WAR file.
            String warFilePath = webappFilePath.concat(".war");
            File warFile = new File(warFilePath);
            if (warFile.exists()) {
                // .WAR exists skip this dir
                return true;
            }

            Host host = DataHolder.getCarbonTomcatService().getTomcat().getHost();
            String webappContext = "/" + webappFile.getName();
            //Make sure we are not re-deploying faulty apps on faulty list again.
            boolean isExistingFaultyApp = isExistingFaultyApp(webappFile.getAbsolutePath());
            if (host.findChild(webappContext) == null && webappFile.isDirectory() && !isExistingFaultyApp) {
                isSkipped = false;
            }
        }
        return isSkipped;
    }

    private boolean isHotUpdating(File file) {
        return file.exists();
    }

    private void handleUndeployment(String fileName, File webappToUndeploy)
            throws DeploymentException {
        try {

            tomcatWebappDeployer.undeploy(webappToUndeploy);

        } catch (CarbonException e) {
            String msg = "Error occurred during undeploying webapp: " + fileName;
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
        super.undeploy(fileName);
    }


    public boolean isExistingFaultyApp(String filePath) {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getWebappHolder(filePath, configContext);
        if (webApplicationsHolder.getFaultyWebapps() != null) {
            if (webApplicationsHolder.getFaultyWebapps().get(WebAppUtils.getWebappName(filePath)) != null) {
                return true;
            } else if (webApplicationsHolder.getFaultyWebapps().get(WebAppUtils.getWebappName(filePath) + ".war") != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideAnotherApp(String path) {
        //Exclude CApps
        boolean fromCApp = path.contains("carbonapps");
        if (path != null && path.endsWith(".war") && !fromCApp) {
            String base = path.substring(0, path.lastIndexOf(File.separator));
            int index = base.lastIndexOf(File.separator) + 1;
            String baseName = base.substring(index);
            if (base != null && !webappsDir.equals(baseName)) {
                // .WAR file is not directly under $webappsDir dir hence ignore.
                return true;
            } else {
                // make sure .WAR file is not under a webapp called as $webappsDir
                String preBase = base.substring(0, index - 1);
                String preBaseName = preBase.substring(preBase.lastIndexOf(File.separator) + 1);
                if (preBaseName != null && webappsDir.equals(preBaseName)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void handleRedeployment(File file) throws DeploymentException {
        DeploymentFileData data = new DeploymentFileData(file, this);
        deploy(data);
    }
}
