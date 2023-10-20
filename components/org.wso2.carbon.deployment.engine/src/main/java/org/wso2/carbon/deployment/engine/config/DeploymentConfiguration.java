/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.deployment.engine.config;

import org.wso2.carbon.config.ConfigurationUtils;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.config.annotation.Ignore;

/**
 * DeploymentConfiguration class holds static configuration parameters specified in the deployment.yml file.
 *
 * @since 5.1.0
 */
@Configuration(namespace = "wso2.artifact.deployment", description = "Deployment configuration parameters")
public class DeploymentConfiguration {

    @Element(description = "deployment mode")
    private DeploymentModeEnum mode = DeploymentModeEnum.scheduled;

    @Element(description = "server repository location")
    private String serverRepositoryLocation = "${carbon.home}/deployment/";

    @Ignore
    private String runtimeRepositoryLocation = "${wso2.runtime.path}/deployment/";

    @Element(description = "Scheduler update interval")
    private int updateInterval = 5;

    @Element(description = "Deployment notifier config")
    private DeploymentNotifierConfig deploymentNotifier = new DeploymentNotifierConfig();

    @Element(description = "Deploy artifacts by lexicographical order")
    private boolean deployByLexicographicalOrder = false;

    public DeploymentConfiguration() {
        serverRepositoryLocation = ConfigurationUtils.substituteVariables(serverRepositoryLocation);
        runtimeRepositoryLocation = ConfigurationUtils.substituteVariables(runtimeRepositoryLocation);
    }

    public DeploymentModeEnum getMode() {
        return mode;
    }

    public String getServerRepositoryLocation() {
        return serverRepositoryLocation;
    }

    public String getRuntimeRepositoryLocation() {
        return runtimeRepositoryLocation;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean getDeployByLexicographicalOrder() {
        return deployByLexicographicalOrder;
    }

    public DeploymentNotifierConfig getDeploymentNotifier() {
        return deploymentNotifier;
    }

}
