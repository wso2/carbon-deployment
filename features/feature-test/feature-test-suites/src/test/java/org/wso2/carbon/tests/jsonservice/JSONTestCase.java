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
package org.wso2.carbon.tests.jsonservice;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClientUtils;
import org.wso2.carbon.commons.admin.clients.AARServiceUploaderClient;
import org.wso2.carbon.commons.utils.FeatureIntegrationBaseTest;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * copy a JSONService.aar to the server , verify deployment and invokes the service
 */
public class JSONTestCase extends FeatureIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(JSONTestCase.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_JSON_BADGERFISH = "application/json/badgerfish";
    private TestUserMode userMode;

    @AfterClass(alwaysRun = true)
    public void jsonServiceDelete() throws Exception {
        deleteService("JSONService");
        log.info("JSONService service deleted");
    }

    @Factory(dataProvider = "userModeProvider")
    public JSONTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(this.userMode);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }


    @Test(groups = "wso2.as", description = "upload JSONService.aar service and verify deployment")
    public void jasonServiceUpload() throws Exception {
        AARServiceUploaderClient aarServiceUploaderClient
                = new AARServiceUploaderClient(backendURL, sessionCookie);

        aarServiceUploaderClient.uploadAARFile(
                "JSONService.aar",
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "carbon_deployment" + File.separator + "aar" + File.separator +
                "JSONService.aar", "");

        AxisServiceClientUtils.waitForServiceDeployment(
                this.automationContext.getContextUrls().getServiceUrl() + "/JSONService");

        log.info("JSONService.aar service uploaded successfully");
    }

    @Test(groups = {"wso2.as"}, description = "invoke the service",
            dependsOnMethods = "jasonServiceUpload")
    public void testGetQuoteRequest()
            throws AxisFault, XMLStreamException, XPathExpressionException {
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        String endpoint = getServiceUrl("JSONService");

        OMElement result =
                axisServiceClient.sendReceive(createPayloadOne(), endpoint, "echoInt",
                                              APPLICATION_JSON);
        log.info(result);
        assertNotNull(result, "Result cannot be null");
        assertEquals(createPayloadOne().toString(), result.toString().trim());

    }

    @Test(groups = {"wso2.as"}, description = "invoke the service",
            dependsOnMethods = "testGetQuoteRequest")
    public void testGetQuoteRequestTwo()
            throws AxisFault, XMLStreamException, XPathExpressionException {
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        String endpoint = getServiceUrl("JSONService");

        OMElement result = axisServiceClient.sendReceive(
                createPayloadTwo(), endpoint, "echoInt",
                APPLICATION_JSON_BADGERFISH);

        log.info(result);
        assertNotNull(result, "Result cannot be null");
        assertEquals(createPayloadTwo().toString(), result.toString().trim());

    }

    private OMElement createPayloadOne() throws XMLStreamException {
        String request = "<echo><value>" + "Hello JSON Service" + "</value></echo>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

    private OMElement createPayloadTwo() throws XMLStreamException {
        String request = "<echo><ns:value xmlns:ns=\"http://services.wsas.training.wso2.org\">" +
                         "Hello JSON Service" + "</ns:value></echo>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

}
