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


package org.wso2.carbon.tests.aarservice;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.commons.admin.clients.AARServiceUploaderClient;
import org.wso2.carbon.commons.utils.FeatureIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * Deploy a aar service and check it using web services.
 */
public class AARServiceTestCase extends FeatureIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(AARServiceTestCase.class);
    private TestUserMode userMode;

    @Factory(dataProvider = "userModeProvider")
    public AARServiceTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    /**
     * Create the necessary variables for this test
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(this.userMode);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.TENANT_USER}
        };
    }

    @Test(groups = "carbon_deployment.test", description = "Upload aar service and verify deployment")
    public void testAarServiceUpload() throws Exception {
        AARServiceUploaderClient aarServiceUploaderClient
                = new AARServiceUploaderClient(backendURL, sessionCookie);

        aarServiceUploaderClient.uploadAARFile(
                "Axis2Service.aar",
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "carbon_deployment" + File.separator + "aar" + File.separator +
                "Axis2Service.aar", "");

        String axis2Service = "Axis2Service";
        isServiceDeployed(axis2Service);
        log.info("Axis2Service.aar service uploaded successfully");
    }

    @Test(groups = "carbon_deployment.test", description = "invoke aar service", dependsOnMethods = "testAarServiceUpload")
    public void invokeService() throws XPathExpressionException, AxisFault {
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        String endpoint = getServiceUrl("Axis2Service");
        OMElement response = axisServiceClient.sendReceive(createPayLoad(), endpoint, "echoInt");
        log.info("Response : " + response);
        assertTrue(response.toString().contains("<ns:return>25</ns:return>"));
    }

    public static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns");
        OMElement getOme = fac.createOMElement("echoInt", omNs);
        OMElement getOmeTwo = fac.createOMElement("x", omNs);
        getOmeTwo.setText("25");
        getOme.addChild(getOmeTwo);
        return getOme;
    }

}
