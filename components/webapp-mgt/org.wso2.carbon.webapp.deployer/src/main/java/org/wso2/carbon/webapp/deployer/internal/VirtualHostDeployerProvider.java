/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.deployer.internal;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;
import org.wso2.carbon.utils.deployment.Axis2DeployerProvider;
import org.wso2.carbon.webapp.mgt.DataHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class reads virtual hosts and their appbase values from CarbonTomcatService and builds deployer configs
 */

public class VirtualHostDeployerProvider implements Axis2DeployerProvider {

    private List<DeployerConfig> deployerConfigs = new ArrayList<DeployerConfig>();

    /**
     * builds deloyerconfigs from host entries of tomcat engine element
     */
    public VirtualHostDeployerProvider() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        if (carbonTomcatService != null) {
            Container[] virtualHosts = carbonTomcatService.getTomcat().getEngine().findChildren();
            for (org.apache.catalina.Container vHost : virtualHosts) {
                Host childHost = (Host) vHost;
                String directory = getDirectoryName(childHost.getAppBase());

                deployerConfigs.add(getDeployerConfig(directory, ".war"));
                deployerConfigs.add(getDeployerConfig(directory, null));
            }
        }
    }

    @Override
    public List<DeployerConfig> getDeployerConfigs() {
        return deployerConfigs;
    }

    private DeployerConfig getDeployerConfig(String directoryName, String extension) {
        DeployerConfig deployerConfig = new DeployerConfig();
        deployerConfig.setClassStr("org.wso2.carbon.webapp.deployer.WebappDeployer");
        deployerConfig.setDirectory(directoryName);
        deployerConfig.setExtension(extension);

        return deployerConfig;
    }

    private String getDirectoryName(String appBase) {
        String baseDir;
        appBase = appBase.replace("/", File.separator);
        if (appBase.endsWith(File.separator)) {
            baseDir = appBase.substring(0, appBase.lastIndexOf(File.separator));
        } else {
            baseDir = appBase;
        }

        return baseDir.substring(baseDir.lastIndexOf(File.separator) + 1, baseDir.length());
    }
}