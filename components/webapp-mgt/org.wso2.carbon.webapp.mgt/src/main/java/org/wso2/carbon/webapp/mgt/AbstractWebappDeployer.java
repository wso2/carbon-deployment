package org.wso2.carbon.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
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
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;
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
    protected AxisConfiguration axisConfig;
    protected Map<String, WebApplicationsHolder> webApplicationsHolderList;
    private boolean isGhostOn;
    private String[] defaultWatchedResources;

    public void init(ConfigurationContext configCtx) {
        this.configContext = configCtx;
        this.axisConfig = configCtx.getAxisConfiguration();
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
        webApplicationsHolderList = (Map<String, WebApplicationsHolder>)
                configCtx.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);
        if (webApplicationsHolderList == null) {
            webApplicationsHolderList = new HashMap<String, WebApplicationsHolder>();
            configCtx.setProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST, webApplicationsHolderList);
        }
        if (!webApplicationsHolderList.containsKey(webappsDir)) {
            WebApplicationsHolder webApplicationsHolder = new WebApplicationsHolder(new File(webappsDir));
            webApplicationsHolderList.put(webappsDir, webApplicationsHolder);
        }
        tomcatWebappDeployer = createTomcatGenericWebappDeployer(webContextPrefix, tenantId, tenantDomain);
        configCtx.setProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST, servletContextParameters);
        isGhostOn = GhostDeployerUtils.isGhostOn();

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
            String webappName = deploymentFileData.getFile().getName();
            if (!isGhostOn) {
                deployThisWebApp(deploymentFileData);
            } else {
                // Check the ghost file
                String absoluteFilePath = deploymentFileData.getAbsolutePath();
                File ghostFile = GhostWebappDeployerUtils.getGhostFile(absoluteFilePath, axisConfig);
                if (ghostFile == null || !ghostFile.exists()) {
                    // ghost file is not found. so this is a new webapp and we have to deploy it
                    deployThisWebApp(deploymentFileData);

                    // iterate all deployed webapps and find the deployed webapp and create the ghost file
                    WebApplication webApplication = GhostWebappDeployerUtils.
                            findDeployedWebapp(configContext, absoluteFilePath);

                    if (webApplication != null) {
                        GhostWebappDeployerUtils.updateLastUsedTime(webApplication);
                        //skip ghost meta file generation for worker nodes
                        if (!CarbonUtils.isWorkerNode()) {
                            GhostWebappDeployerUtils.serializeWebApp(webApplication, axisConfig, absoluteFilePath);
                        }
                    }
                } else {
                    // load the ghost webapp
                    WebApplication ghostWebApplication = GhostWebappDeployerUtils.createGhostWebApp(
                            ghostFile, deploymentFileData.getFile(), tomcatWebappDeployer,
                            configContext);
                    String ghostWebappFileName = deploymentFileData.getFile().getName();

                    WebApplicationsHolder webApplicationsHolder = WebAppUtils.getwebappHolder(
                            ghostWebApplication.getWebappFile().getAbsolutePath(), configContext);
                    if (!webApplicationsHolder.getStartedWebapps().containsKey(ghostWebappFileName)) {
//                        ghostWebApplication.setServletContextParameters(servletContextParameters);

                        WebApplicationsHolder webappsHolder = WebAppUtils.getwebappHolder(
                                ghostWebApplication.getWebappFile().getAbsolutePath(), configContext);

                        log.info("Deploying Ghost webapp : " + ghostWebappFileName);
                        webappsHolder.getStartedWebapps().put(ghostWebappFileName,
                                ghostWebApplication);
                        webappsHolder.getFaultyWebapps().remove(ghostWebappFileName);
                    }

                    // TODO:  add webbapp to eventlistners
                }
            }
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

            WebApplication webApplication = GhostWebappDeployerUtils.findDeployedWebapp(
                    configContext, deploymentFileData.getFile().getAbsolutePath());

            if (webApplication != null) {
                //since both Jax-WS/RS applications and web application use same deployer
                //when the application type if webapp, we need to check which type it is.
                String webappType = getType();
                if (webappType.equals(WebappsConstants.WEBAPP_FILTER_PROP) &&
                        WebAppUtils.checkJaxApplication(webApplication) != null) {
                    webappType = WebappsConstants.JAX_WEBAPP_FILTER_PROP;
                }
                webApplication.setProperty(WebappsConstants.WEBAPP_FILTER, webappType);

                if (!CarbonUtils.isWorkerNode()) {
                    persistWebappMetadata(webApplication, axisConfig);
                }

            }

        } catch (Exception e) {
            String msg = "Error occurred while deploying webapp : " + deploymentFileData.getFile().getAbsolutePath();
            // removing faulty artifacts deployed by CApps
            if (deploymentFileData.getAbsolutePath().contains("carbonapps")) {
                String failedArtifact = deploymentFileData.getFile().getName();
                WebApplicationsHolder webappsHolder = WebAppUtils.getwebappHolder(
                        deploymentFileData.getAbsolutePath(), configContext);
                webappsHolder.getFaultyWebapps().remove(failedArtifact);
            }
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        File unpackedFile = null;
        File warFile = null;
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
                    if (!warFile.exists()) {
                        handleRedeployment(unpackedFile);
                    } else {
                        handleRedeployment(warFile);
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
            String[] watchedResources = ((StandardContext) context).findWatchedResources();
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
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getwebappHolder(file.getAbsolutePath(), configContext);

        Map<String, WebApplication> webappMap = webApplicationsHolder.getStartedWebapps();
        WebApplication webapp = null;
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

        Map<String, WebApplicationsHolder> webApplicationsHolderMap =
                (Map<String, WebApplicationsHolder>) configContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER_LIST);

        for (WebApplicationsHolder webApplicationsHolder : webApplicationsHolderMap.values()) {
            if (isGhostOn && webApplicationsHolder != null) {
                for (WebApplication webApplication : webApplicationsHolder.getStartedWebapps().values()) {
                    try {
                        tomcatWebappDeployer.lazyUnload(webApplication.getWebappFile());
                    } catch (CarbonException e) {
                        String msg = "Error occurred during cleaning up webapps";
                        log.error(msg, e);
                        throw new DeploymentException(msg, e);
                    }
                }
            }
        }
    }

    private void persistWebappMetadata(WebApplication webApplication, AxisConfiguration axisConfig) throws
            ArtifactMetadataException, AxisFault {
        String bamStatsEnabled = webApplication.findParameter(WebappsConstants.ENABLE_BAM_STATISTICS);
        if (bamStatsEnabled == null) {
            webApplication.addParameter(WebappsConstants.ENABLE_BAM_STATISTICS, Boolean.FALSE.toString());
            bamStatsEnabled = "false";
        }

        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_METADATA_BASE_DIR
                + File.separator + WebAppUtils.getWebappDir(webApplication.getWebappFile().getAbsolutePath()));
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();

        manager.setParameter(webApplication.getWebappFile().getName(), type,
                WebappsConstants.ENABLE_BAM_STATISTICS, bamStatsEnabled, false);

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
            if (isGhostOn && !GhostWebappDeployerUtils.skipUndeploy(fileName)) {
                // Remove the corresponding ghost file and dummy context directory
                File ghostFile = GhostWebappDeployerUtils.getGhostFile(fileName, axisConfig);
                File dummyContextDir = GhostWebappDeployerUtils.
                        getDummyContextFile(fileName, axisConfig);
                if (ghostFile != null && ghostFile.exists() && !ghostFile.delete()) {
                    log.error("Error while deleting Ghost webapp file : " +
                            ghostFile.getAbsolutePath());
                }
                if (dummyContextDir != null && dummyContextDir.exists() &&
                        !dummyContextDir.delete()) {
                    log.error("Error while deleting dummy context file : " +
                            dummyContextDir.getAbsolutePath());
                }
            }

        } catch (CarbonException e) {
            String msg = "Error occurred during undeploying webapp: " + fileName;
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
        super.undeploy(fileName);
    }


    public boolean isExistingFaultyApp(String filePath) {
        WebApplicationsHolder webApplicationsHolder = WebAppUtils.getwebappHolder(filePath, configContext);
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
