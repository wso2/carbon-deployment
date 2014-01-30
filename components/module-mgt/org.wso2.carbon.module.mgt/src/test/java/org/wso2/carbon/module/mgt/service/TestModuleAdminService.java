/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.module.mgt.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.*;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.persistence.ModulePersistenceManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.ServiceGroupPersistenceManager;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;
import org.wso2.carbon.security.SecurityServiceHolder;
import org.wso2.carbon.utils.WSO2Constants;
import org.wso2.carbon.module.mgt.ModuleMetaData;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.io.InputStream;

public class TestModuleAdminService extends BaseTestCase {

    private static String REPO_PATH = "target/test-classes/repository/";

    private static AxisConfiguration axisConfig;
    private static AxisModule testModule1;
    private static AxisModule testModule2;
    private static ModuleAdminService moduleAdmin;
    private static ModuleMetaData metaData1;
    private static ModuleMetaData metaData2;

    public void setUp() throws Exception {
        super.setUp();

        if (axisConfig == null) {
            InputStream regConfigStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("registry.xml");
            final InMemoryEmbeddedRegistryService embeddedRegistryService =
                    new InMemoryEmbeddedRegistryService(regConfigStream);
            RegistryCoreServiceComponent component = new RegistryCoreServiceComponent() {
                {
                    setRealmService(embeddedRegistryService.getRealmService());
                }
            };
            component.registerBuiltInHandlers(embeddedRegistryService);
            Registry registry = embeddedRegistryService.getConfigSystemRegistry();
            Registry governanceRegistry = embeddedRegistryService.getGovernanceSystemRegistry();
            SecurityServiceHolder.setRegistryService(embeddedRegistryService);

            //create an axisconfig object using an axis2.xml
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(REPO_PATH);
            axisConfig = configContext.getAxisConfiguration();

            PrivilegedCarbonContext.getCurrentContext(axisConfig).setRegistry(
                    RegistryType.SYSTEM_CONFIGURATION, registry);
            PrivilegedCarbonContext.getCurrentContext(axisConfig).setRegistry(
                    RegistryType.SYSTEM_CONFIGURATION, governanceRegistry);

            // The following line of code is kept for backward compatibility. Remove this once we
            // are certain that this is not required. -- Senaka.
            axisConfig.addParameter(WSO2Constants.CONFIG_SYSTEM_REGISTRY_INSTANCE, registry);


            //calling deployModule to deploy a newmodule to the axisconfig
            deployModule();

            //Creating a module admin service object
            moduleAdmin = new ModuleAdminService(axisConfig);

            metaData1 = moduleAdmin.getModuleInfo(testModule1.getName(),
                    testModule1.getVersion().toString());
            metaData2 = moduleAdmin.getModuleInfo(testModule2.getName(),
                    testModule2.getVersion().toString());

            ModulePersistenceManager mpm = new ModulePersistenceManager(axisConfig);
            mpm.handleNewModuleAddition(testModule1, testModule1.getName(),
                    testModule1.getVersion().toString());
            mpm.handleNewModuleAddition(testModule2, testModule2.getName(),
                    testModule2.getVersion().toString());

            //create and deploy a new service to the axisConfig
            createAxisService();
        }
    }

    public void testTotalModules() throws Exception {
        HashMap<String, AxisModule> a = axisConfig.getModules();
        Collection<AxisModule> b = a.values();
        assertEquals(3, b.size());
    }

    public void testAxisMethodStuff() throws Exception {
        ModuleMetaData[] list;
        ModuleMetaData[] listG;
        ModuleMetaData[] listMod;
        ModuleMetaData[] listMod1;
        ModuleMetaData[] listMod2;
        ModuleMetaData[] listModW;
        ModuleMetaData[] listModW1;
        ModuleMetaData[] listModW2;

        assertEquals("testNewModule", metaData1.getModulename());
        assertEquals("testNewModule-1", metaData1.getModuleId());
        assertEquals("1", metaData1.getModuleVersion());

        assertEquals("testNewModule2", metaData2.getModulename());
        assertEquals("testNewModule2-1", metaData2.getModuleId());
        assertEquals("1", metaData2.getModuleVersion());

        int expectedModules = 2;

        listMod = moduleAdmin.listModulesForServiceGroup("echo");
        listMod1 = moduleAdmin.listModulesForService("echo");
        listMod2 = moduleAdmin.listModulesForOperation("echo", "echoOMElement");
        assertEquals(expectedModules, listMod.length);
        assertEquals(expectedModules, listMod1.length);
        assertEquals(expectedModules, listMod2.length);

        listModW = moduleAdmin.listModulesForServiceGroup("weather");
        listModW1 = moduleAdmin.listModulesForService("weather");
        listModW2 = moduleAdmin.listModulesForOperation("weather", "c2f");
        assertEquals(expectedModules, listModW.length);
        assertEquals(expectedModules, listModW1.length);
        assertEquals(expectedModules, listModW2.length);

        list = moduleAdmin.listModules();
        assertEquals(expectedModules, list.length);

        boolean testEngage = moduleAdmin.globallyEngageModule(metaData1.getModuleId());
        assertEquals(true, testEngage);

        listG = moduleAdmin.listGloballyEngagedModules();
        assertEquals(1, listG.length);

        testEngage = moduleAdmin.globallyDisengageModule(metaData1.getModuleId());
        assertEquals(true, testEngage);

        listG = moduleAdmin.listGloballyEngagedModules();
        assertEquals(0, listG.length);
    }


    public void testEngageModules() throws Exception {
        AxisService weatherService = axisConfig.getService("weather");
        AxisService echoService = axisConfig.getService("echo");

        AxisOperation weatherOp = weatherService.getOperationByAction("c2f");
        AxisOperation echoOp = echoService.getOperationByAction("echoOMElement");

        AxisServiceGroup echoSerGp = echoService.getAxisServiceGroup();

        //boolean testOp;
        boolean testOp = moduleAdmin.engageModuleForOperation(metaData2
                .getModuleId(), "weather", "c2f");
        assertTrue(testOp);
        assertTrue(weatherOp.isEngaged(metaData2.getModuleId()));

        try {
            moduleAdmin.engageModuleForOperation(metaData2.getModuleId(), "weather", "c2f");
            fail("Exception must occure when engadging " +
                    "an already engadged module to an operation fails");
        } catch (Exception ex) {
        }

        ServicePersistenceManager spm = PersistenceFactory.getInstance(axisConfig).getServicePM();
        OMElement weatherServiceElement = spm.getService(weatherService);
        AXIOMXPath xpath = new AXIOMXPath("/service/operation"+ PersistenceUtils.getXPathAttrPredicate("name", "c2f")+"/module");
        OMElement moduleElement = (OMElement) xpath.selectSingleNode(weatherServiceElement);
        assertNotNull("module element should be persisted in the file",moduleElement);
        assertEquals("testNewModule2", moduleElement.getAttributeValue(new QName("name")));
        assertEquals("1", moduleElement.getAttributeValue(new QName("version")));

        testOp = moduleAdmin.disengageModuleForOperation(metaData2
                .getModuleId(), "weather", "c2f");
        assertTrue(testOp);
        assertFalse(weatherOp.isEngaged(metaData2.getModuleId()));


        boolean testSer = moduleAdmin.engageModuleForService(metaData1.getModuleId(), "weather");
        assertTrue(testSer);
        assertTrue(weatherService.isEngaged(metaData1.getModuleId()));
        assertTrue(weatherOp.isEngaged(metaData1.getModuleId()));

        try {
            moduleAdmin.engageModuleForService(metaData1.getModuleId(), "weather");
            fail("Exception must occure when engadging " +
                    "an already engadged module to a service fails");
        } catch (Exception ex) {
        }

        try {
            moduleAdmin.engageModuleForOperation(metaData1.getModuleId(), "weather", "c2f");
            fail("Exception must occure when engadging " +
                    "an already engadged module to an operation fails");
        } catch (Exception ex) {
        }

        testSer = moduleAdmin.disengageModuleForService(metaData1.getModuleId(), "weather");
        assertTrue(testSer);
        assertFalse(weatherService.isEngaged(metaData1.getModuleId()));
        assertFalse(weatherOp.isEngaged(metaData1.getModuleId()));

        boolean testSerG = moduleAdmin.engageModuleForServiceGroup(metaData2.getModuleId(), "echo");
        assertTrue(testSerG);
        assertTrue(echoSerGp.isEngaged(metaData2.getModuleId()));
        assertTrue(echoService.isEngaged(metaData2.getModuleId()));
        assertTrue(echoOp.isEngaged(metaData2.getModuleId()));

        try {
            moduleAdmin.engageModuleForServiceGroup(metaData2.getModuleId(), "echo");
            fail("Exception must occure when engadging " +
                    "an already engadged module to a service group fails");
        } catch (Exception ex) {
        }

        try {
            moduleAdmin.engageModuleForService(metaData2.getModuleId(), "echo");
            fail("Exception must occure when" +
                    " engadging an already engadged module to a service fails");
        } catch (Exception ex) {
        }

        try {
            moduleAdmin.engageModuleForOperation(metaData2.getModuleId(), "echo", "echoOMElement");
            fail("Exception omust occure when" +
                    " engadging an already engadged module to an operation fails");
        } catch (Exception ex) {
        }

        testSerG = moduleAdmin.disengageModuleForServiceGroup(metaData2.getModuleId(), "echo");
        assertTrue(testSerG);
        assertFalse(echoSerGp.isEngaged(metaData2.getModuleId()));
        assertFalse(echoService.isEngaged(metaData2.getModuleId()));
        assertFalse(echoOp.isEngaged(metaData2.getModuleId()));
    }


    public void testModuleStuff() throws Exception {
        String setParam[] = new String[3];
        String paraName = "testParamOne";
        String paraValue = "testValueOne";
        String para1Name = "testParamTwo";
        String para1Value = "testValueTwo";
        String para2Name = "testParamThree";
        String para2Value = "testValueThree";

        setParam[0] = ("<parameter name='" + paraName + "'>" + paraValue + "</parameter>");
        setParam[1] = ("<parameter name='" + para1Name + "'>" + para1Value + "</parameter>");
        setParam[2] = ("<parameter name='" + para2Name + "'>" + para2Value + "</parameter>");

        moduleAdmin.setModuleParameters(metaData1.getModulename(),
                metaData1.getModuleVersion(), setParam);
        ArrayList paramList = testModule1.getParameters();
        assertEquals(4, paramList.toArray().length);

        moduleAdmin.removeModuleParameter(metaData1.getModulename(),
                metaData1.getModuleVersion(), "testParamOne");
        paramList = testModule1.getParameters();
        assertEquals(3, paramList.toArray().length);
    }


    private void createAxisService() throws Exception {
        QName serviceName = new QName("weather");
        QName operationName = new QName("c2f");
//        AxisServiceGroup myServiceGroup = new AxisServiceGroup(axisConfig);
//        myServiceGroup.setServiceGroupName("weatherGroup");
        AxisService myService = org.apache.axis2.util.Utils.createSimpleService(serviceName,
                org.wso2.carbon.module.mgt.service.weather.weather.class.getName(), operationName);
//        myServiceGroup.addService(myService);

        ServiceGroupPersistenceManager sgpm = PersistenceFactory.getInstance(axisConfig).getServiceGroupPM();
        ServicePersistenceManager spm = PersistenceFactory.getInstance(axisConfig).getServicePM();
        axisConfig.addService(myService);
        sgpm.handleNewServiceGroupAddition(myService.getAxisServiceGroup());
        spm.handleNewServiceAddition(myService);

        AxisService echoService = axisConfig.getService("echo");
        AxisService calculatorService = axisConfig.getService("CalculatorService");
        sgpm.handleNewServiceGroupAddition(echoService.getAxisServiceGroup());
        spm.handleNewServiceAddition(echoService);
        spm.handleNewServiceAddition(calculatorService);

    }

    private void deployModule() throws Exception {
        testModule1 = new AxisModule();
        testModule1.setName("testNewModule");
        testModule1.setModuleDescription("module created for testing");
        testModule1.setVersion(new Version("1"));

        testModule2 = new AxisModule();
        testModule2.setName("testNewModule2");
        testModule2.setModuleDescription("module 2 created for testing");
        testModule2.setVersion(new Version("1"));

        axisConfig.addModule(testModule1);
        axisConfig.addModule(testModule2);
    }
}
