/*
* Copyright 2004,2013 The Apache Software Foundation.
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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataException;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataManager;
import org.wso2.carbon.core.persistence.metadata.DeploymentArtifactMetadataFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class WebappMetadataDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(WebappMetadataDeployer.class);

    private ConfigurationContext configContext;
    private AxisConfiguration axisConfig;
    private DeploymentArtifactMetadataFactory metadataFactory;
    private String webappMetaDataDir;

    public void init(ConfigurationContext configurationContext) {
        this.configContext = configurationContext;
        axisConfig = configurationContext.getAxisConfiguration();
        try {
            metadataFactory = DeploymentArtifactMetadataFactory.getInstance(axisConfig);
        } catch (AxisFault e) {
            log.error("Error obtaining Deployment Artifact Metadata Factory.", e);
        }
    }
    
    public void setDirectory(String directory) {
        if (directory.contains("/")) {
            directory = directory.replace("/", File.separator);
        }
        webappMetaDataDir = directory;
    }
    
    public void setExtension(String s) {
    }
    
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String artifactName = getArtifactFilePath(deploymentFileData);
        
        if (log.isDebugEnabled()) {
            log.debug("Detected Webapp meta file change.." + artifactName);
        }

        try {
            
            WebApplicationsHolder webappsHolder = (WebApplicationsHolder)
                    configContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
            //TODO support for versioned webapps
            Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
            WebApplication webapp = startedWebapps.get(artifactName);

            if (webapp != null) {
    //            webapp.reload();
                loadPersistData(deploymentFileData.getAbsolutePath(), webapp);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No stopped webapp " + artifactName + "found for tenant:");
                }
            }
        } catch (AxisFault e) {
            log.info(e.getMessage(), e);
            throw new DeploymentException(e);
        } catch (ArtifactMetadataException e) {
            log.info(e.getMessage(), e);
            throw new DeploymentException(e);
        }

    }

    private void loadPersistData(String path, WebApplication webapp) throws ArtifactMetadataException, AxisFault {
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();
        Properties prop = manager.loadParameters(path);
        for (String propName : prop.stringPropertyNames()) {
            String propValue = prop.getProperty(propName);
            webapp.addParameter(propName, propValue);
        }
    }

    private String getArtifactFilePath(DeploymentFileData deploymentFileData) {
        String path = deploymentFileData.getAbsolutePath();
        String artifactName = path.substring(path.indexOf(webappMetaDataDir) + webappMetaDataDir.length() + 1);
        artifactName = artifactName.substring(0, artifactName.lastIndexOf(".properties"));
        return artifactName.replace("\\", "/").replace("/", "#");
    }
}
