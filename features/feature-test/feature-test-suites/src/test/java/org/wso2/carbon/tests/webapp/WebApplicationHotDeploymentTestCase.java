/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tests.webapp;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.automation.test.utils.http.client.HttpClientUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.commons.utils.FeatureIntegrationBaseTest;
import org.wso2.carbon.commons.utils.WebAppDeploymentUtil;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Deploying the web application by dropping the war file to the repository/deployment/server/webapp folder &
 * invoke once deployed successfully
 */
public class WebApplicationHotDeploymentTestCase extends FeatureIntegrationBaseTest {
    private final String webAppFileName = "appServer-valied-deploymant-1.0.0.war";
    private final String webAppName = "appServer-valied-deploymant-1.0.0";
    private String webAppDeploymentDir;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        webAppDeploymentDir =
                System.getProperty(ServerConstants.CARBON_HOME) + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server"
                + File.separator + "webapps" + File.separator;
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.as", description = "Deploying web application by copying war file to deployment directory")
    public void testWebApplicationHotDeployment() throws Exception {

        FileManager.copyJarFile(new File(
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "carbon_deployment" + File.separator + "war" + File.separator + webAppFileName)
                , webAppDeploymentDir);

        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(
                backendURL, sessionCookie, webAppName)
                , "Web Application Deployment failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.as", description = "Invoke web application",
            dependsOnMethods = "testWebApplicationHotDeployment")
    public void testInvokeWebApp() throws Exception {
        String webAppURLLocal = webAppURL + "/appServer-valied-deploymant-1.0.0";
        HttpClientUtil client = new HttpClientUtil();
        OMElement omElement = client.get(webAppURLLocal);
        assertEquals(omElement.toString(), "<status>success</status>", "Web app invocation fail");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.as", description = "UnDeploying web application by deleting war file from deployment directory",
            dependsOnMethods = "testInvokeWebApp")
    public void testDeleteWebApplication() throws Exception {
        assertTrue(FileManager.deleteFile(webAppDeploymentDir + webAppFileName));

        assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(backendURL, sessionCookie, webAppName),
                    "Web Application unDeployment failed");

        String webAppURLLocal = webAppURL + "/appServer-valied-deploymant-1.0.0";
        HttpResponse response = HttpRequestUtil.sendGetRequest(webAppURLLocal, null);

        assertEquals(response.getResponseCode(), 302, "Response code mismatch. Client request " +
                                                             "got a response even after web app is undeployed");
    }
}
