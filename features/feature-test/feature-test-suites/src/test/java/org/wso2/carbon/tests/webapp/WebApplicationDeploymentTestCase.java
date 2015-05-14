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
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpClientUtil;
import org.wso2.carbon.commons.utils.FeatureIntegrationBaseTest;
import org.wso2.carbon.commons.admin.clients.WebAppAdminClient;
import org.wso2.carbon.commons.utils.WebAppDeploymentUtil;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Copy a war file and check the war is deployed
 */
public class WebApplicationDeploymentTestCase extends FeatureIntegrationBaseTest {
    private WebAppAdminClient webAppAdminClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);
    }

    @Test(groups = "carbon_deployment.test", description = "Deploying web application")
    public void testWebApplicationDeployment() throws Exception {
        String webAppFileName = "appServer-valied-deploymant-1.0.0.war";

        webAppAdminClient.warFileUploader(
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "carbon_deployment" +
                File.separator + "war" + File.separator + webAppFileName);

        String webAppName = "appServer-valied-deploymant-1.0.0";

        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, webAppName),
                   "Web Application Deployment failed");

    }

    @Test(groups = "carbon_deployment.test", description = "Invoke web application",
            dependsOnMethods = "testWebApplicationDeployment")
    public void testInvokeWebApp() throws Exception {
        String webAppURLLocal = webAppURL + "/appServer-valied-deploymant-1.0.0";
        HttpClientUtil client = new HttpClientUtil();
        OMElement omElement = client.get(webAppURLLocal);
        assertEquals(omElement.toString(), "<status>success</status>", "Web app invocation fail");
    }
}