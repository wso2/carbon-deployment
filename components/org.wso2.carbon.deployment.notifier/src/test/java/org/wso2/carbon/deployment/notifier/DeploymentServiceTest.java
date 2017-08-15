/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.deployment.notifier;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.engine.internal.DeploymentEngine;
import org.wso2.carbon.deployment.notifier.deployers.CustomDeployer;
import org.wso2.carbon.deployment.notifier.service.CustomDeploymentService;
import org.wso2.carbon.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Deployment Service Test class.
 *
 * @since 5.0.0
 */
public class DeploymentServiceTest extends BaseTest {

    private static final String CARBON_REPO = "carbon-repo";
    private static final String RUNTIME_REPO = "deployment";
    private static final String DEPLOYER_REPO = CARBON_REPO + File.separator + "text-files";
    private static final String RUNTIME_DEPLOYER_REPO = RUNTIME_REPO + File.separator + "text-files";
    private CustomDeploymentService deploymentService;
    private DeploymentEngine deploymentEngine;
    private CustomDeployer customDeployer;
    private String artifactPath;
    private String artifactPath2;


    /**
     * @param testName
     */
    public DeploymentServiceTest(String testName) {
        super(testName);
    }

    @BeforeTest
    public void setup() throws Exception {
        customDeployer = new CustomDeployer();
        artifactPath = getTestResourceFile(DEPLOYER_REPO).getAbsolutePath()
                + File.separator + "sample1.txt";
        artifactPath2 = getTestResourceFile(RUNTIME_DEPLOYER_REPO).getAbsolutePath()
                       + File.separator + "sample2.txt";
        deploymentEngine = new DeploymentEngine();
        deploymentEngine.start(getTestResourceFile(CARBON_REPO).getAbsolutePath(),
                               getTestResourceFile(DEPLOYER_REPO).getAbsolutePath());
        deploymentEngine.registerDeployer(customDeployer);
    }

    @Test
    public void testDeploymentService() {
        deploymentService = new CustomDeploymentService(deploymentEngine);
    }

    @Test(dependsOnMethods = {"testDeploymentService"})
    public void testDeploy() throws CarbonDeploymentException {
        deploymentService.deploy(artifactPath, customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample1Deployed);
        deploymentService.deploy(artifactPath2, customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample2Deployed);
    }

    @Test(dependsOnMethods = {"testDeploy"})
    public void testUpdate() throws CarbonDeploymentException {
        deploymentService.redeploy(new File(artifactPath).getName(),
                customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample1Updated);
        deploymentService.redeploy(new File(artifactPath2).getName(),
                                   customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample2Updated);
    }

    @Test(dependsOnMethods = {"testUpdate"})
    public void testUndeploy() throws CarbonDeploymentException {
        deploymentService.undeploy(new File(artifactPath).getName(),
                customDeployer.getArtifactType());
        Assert.assertFalse(CustomDeployer.sample1Deployed);
        deploymentService.undeploy(new File(artifactPath2).getName(),
                                   customDeployer.getArtifactType());
        Assert.assertFalse(CustomDeployer.sample2Deployed);
    }

    @AfterTest
    public void cleanupTempfile() throws IOException {
        FileUtils.deleteDir(new File(getTestResourceFile(CARBON_REPO).getAbsolutePath() +
                File.separator + "file:text-files"));
        FileUtils.deleteDir(new File(getTestResourceFile(RUNTIME_REPO).getAbsolutePath() +
                                     File.separator + "file:text-files"));
    }
}
