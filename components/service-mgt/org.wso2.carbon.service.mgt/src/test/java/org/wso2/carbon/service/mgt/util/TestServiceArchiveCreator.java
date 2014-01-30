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
package org.wso2.carbon.service.mgt.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.service.mgt.BaseTestCase;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.WSO2Constants;
import org.wso2.carbon.core.util.ParameterUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.*;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;

public class TestServiceArchiveCreator extends BaseTestCase {
    
    private String repoPath = "src/test/resources/repository/";
    protected static Registry registry = null;
    protected static Registry governanceRegistry = null;
    private AxisConfiguration axisCon;
    private AxisModule newModule;

    public void setup() throws Exception{
        super.setUp();
    }


    public void testSteps() throws Exception{

        configStuff();
        SerArchMethods();
        
    }

    
    public void configStuff() throws Exception{

        ConfigurationContext confContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repoPath);
        axisCon = confContext.getAxisConfiguration();
        createAxisService();
        createModule();

        PrivilegedCarbonContext.getCurrentContext(axisCon).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, registry);
        PrivilegedCarbonContext.getCurrentContext(axisCon).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, governanceRegistry);
            
        // The following line of code is kept for backward compatibility. Remove this once we
        // are certain that this is not required. -- Senaka.
        axisCon.addParameter(WSO2Constants.CONFIG_SYSTEM_REGISTRY_INSTANCE, registry);
    }

    public void SerArchMethods() throws Exception{
        
        AxisServiceGroup testGrp =  axisCon.getServiceGroup("testService");
        AxisServiceGroup testGrp1 =  axisCon.getServiceGroup("testService1");
        AxisService testSer = axisCon.getService("testService");
        AxisService testSer1 = axisCon.getService("testService1");

        
        String srcGroupName = testSer.getAxisServiceGroup().getServiceGroupName();
        String srcGroupName1 = testSer1.getAxisServiceGroup().getServiceGroupName();
        assertEquals("testService", srcGroupName);
        assertEquals("testService1",srcGroupName1 );

        //String cArchive = ServiceArchiveCreator.createArchive(confContext,srcGroupName);TODO

        OMElement om1 = ServiceArchiveCreator.createServiceGroupXMLInfoset(testGrp);
        OMNamespace ox = om1.getNamespace();
        assertEquals("serviceGroup",om1.getLocalName());
        assertEquals("",ox.getPrefix());


        testGrp1.engageModule(newModule);

        OMElement om2 = ServiceArchiveCreator.createServiceGroupXMLInfoset(testGrp1);
        OMNamespace ox1 = om2.getNamespace();
        assertEquals("serviceGroup",om2.getLocalName());
        assertEquals("",ox1.getPrefix());


        OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace("http://www.wso2.com/Nmsp", "Nmsp");
        OMElement om3 = ServiceArchiveCreator.createOMElement(fac ,ns ,"testCreatingOMElements");
        assertEquals("Nmsp",om3.getNamespace().getPrefix());
        

        OMNamespace ns1 = fac.createOMNamespace("http://www.wso2.com/Nmsp1", "Nmsp1");
        OMElement om4 = ServiceArchiveCreator.createOMElement(fac ,ns1 ,"test2", "testingText");
        assertEquals("Nmsp1",om4.getNamespace().getPrefix());
        assertEquals("testingText",om4.getText());


        OMAttribute oAb1 = ServiceArchiveCreator.createOMAttribute(fac, ns1, "testingText", "AttributeValue");
        assertEquals("AttributeValue",oAb1.getAttributeValue());
        om4.addAttribute(oAb1);
        assertEquals("AttributeValue",om4.getAttributeValue(oAb1.getQName()));
        
    }

    public void createAxisService() throws Exception{

        AxisServiceGroup testGrp = new AxisServiceGroup();
        AxisService testSer = new AxisService();
        testSer.setName("testService");
        testGrp.addService(testSer);

        AxisServiceGroup testGrp1 = new AxisServiceGroup();
        AxisService testSer1 = new AxisService();
        testSer1.setName("testService1");
        testGrp1.addService(testSer1);


        String param1 = ("<parameter name='"+"testParameter1" +"'>"+"serviceType"+"</parameter>");
        String param2 = ("<parameter name='"+"testParameter2" +"'>"+"testparam2"+"</parameter>");


        XMLStreamReader xmlSR = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(param2.getBytes()));
        OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
        Parameter parameter = ParameterUtil.createParameter(paramEle);


        XMLStreamReader xmlSR1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(param1.getBytes()));
        OMElement paramEle1 = new StAXOMBuilder(xmlSR1).getDocumentElement();
        Parameter parameter1 = ParameterUtil.createParameter(paramEle1);


        testGrp.addParameter(parameter);
        testGrp1.addParameter(parameter);
        testGrp1.addParameter(parameter1);

        
        ArrayList<Parameter> ap = testGrp.getParameters();
        ArrayList<Parameter> ap1 = testGrp1.getParameters();
        assertEquals(1,ap.size());
        assertEquals(2,ap1.size());


        axisCon.addServiceGroup(testGrp);
        axisCon.addServiceGroup(testGrp1);

    }

    public void createModule() throws Exception{
       newModule = new AxisModule();
       newModule.setName("testNewModule");
       newModule.setModuleDescription("module created for testing");
       newModule.setVersion(new Version("1"));
       axisCon.addModule(newModule);
       //return newModule;
    }


}
