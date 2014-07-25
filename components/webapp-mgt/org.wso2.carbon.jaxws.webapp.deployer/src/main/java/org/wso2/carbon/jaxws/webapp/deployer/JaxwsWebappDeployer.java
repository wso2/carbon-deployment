/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.jaxws.webapp.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.webapp.mgt.AbstractWebappDeployer;
import org.wso2.carbon.webapp.mgt.TomcatGenericWebappsDeployer;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

/**
 * Axis2 Deployer for deploying JAX-WS/JAX-RS Web applications
 * @deprecated Use {@link org.wso2.carbon.webapp.deployer.WebappDeployer} instead
 */
public class JaxwsWebappDeployer extends AbstractWebappDeployer {

    private static final Log log = LogFactory.getLog(JaxwsWebappDeployer.class);

    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
//        configCtx.setProperty(WebappsConstants.TOMCAT_JAX_WEBAPP_DEPLOYER, tomcatWebappDeployer);
    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        log.info("Deploying JAX-WS/JAX-RS Web Application : " + deploymentFileData.getAbsolutePath());
        super.deploy(deploymentFileData);
    }

    @Override
    protected TomcatGenericWebappsDeployer createTomcatGenericWebappDeployer(
            String webContextPrefix, int tenantId, String tenantDomain) {
        return new TomcatGenericWebappsDeployer(webContextPrefix, tenantId, tenantDomain, webappsHolder, configContext);
    }

    @Override
    protected String getType() {
        return WebappsConstants.JAX_WEBAPP_FILTER_PROP;
    }

    @Override
    public void setDirectory(String repoDir) {
        this.webappsDir = repoDir;
    }

    @Override
    public void setExtension(String extension) {

    }

}
