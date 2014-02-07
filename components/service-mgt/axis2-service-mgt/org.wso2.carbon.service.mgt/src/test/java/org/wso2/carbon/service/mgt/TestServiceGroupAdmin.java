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
package org.wso2.carbon.service.mgt;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.utils.WSO2Constants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.core.util.ParameterUtil;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

public class TestServiceGroupAdmin extends BaseTestCase {
    
    private String repoPath = "target/test-classes/repository/";

    private AxisConfiguration axisCon;
    private ServiceGroupAdmin sGadmin;

    public void setUp() throws Exception{
        super.setUp();
    }


    public void testSteps() throws Exception{
        serGrpAdminConfigStuff();
        serGrpAdminMethodStuff();
    }


    public void serGrpAdminConfigStuff() throws Exception{

        ConfigurationContext confContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repoPath);
        axisCon = confContext.getAxisConfiguration();
        axisCon.setRepository(new URL("file://"+new File(repoPath).getAbsolutePath()));
        createAnAxisService();
        PrivilegedCarbonContext.getCurrentContext(axisCon).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, configRegistry);
        PrivilegedCarbonContext.getCurrentContext(axisCon).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, governanceRegistry);
            
        // The following line of code is kept for backward compatibility. Remove this once we
        // are certain that this is not required. -- Senaka.
        axisCon.addParameter(WSO2Constants.CONFIG_SYSTEM_REGISTRY_INSTANCE, configRegistry);
        
        sGadmin = new ServiceGroupAdmin(confContext);
    }

    public void serGrpAdminMethodStuff() throws Exception{

        
        AxisServiceGroup tSg = axisCon.getServiceGroup("testService");
        String testParam = "<parameter name=\"serviceType\">serviceType</parameter>";

        //ServiceGroupMetaData data1 = sGadmin.listServiceGroup(tSg.getServiceGroupName());TODO

        //ServiceGroupMetaData data1 = sGadmin.configureServiceGroupMTOM("flag",serviceGrpName);TODO

        ParameterMetaData[] pMData1 = setPramMetaData();
        sGadmin.updateServiceGroupParamters(tSg.getServiceGroupName(),pMData1);
        String[] perArray = sGadmin.getServiceGroupParameters(tSg.getServiceGroupName());
        assertEquals(1,perArray.length);
        assertEquals(testParam,perArray[0]);


        String paramElem[]= new String[3];
        String para1 = ("<parameter name='"+ "testParam1" +"'>"+"testParameter1"+"</parameter>");
        String para2 = ("<parameter name='"+ "testParam2" +"'>"+"testParameter2"+"</parameter>");
        String para3 = ("<parameter name='"+ "testParam3" +"'>"+"testParameter3"+"</parameter>");
        paramElem[0] = para1; paramElem[1] = para2; paramElem[2] = para3;


        sGadmin.setServiceGroupParameters(tSg.getServiceGroupName(),paramElem);
        ParameterMetaData pmdata = sGadmin.getServiceGroupParameter(tSg.getServiceGroupName(),"testParam1");
        assertEquals("testParam1",pmdata.getName());
        assertEquals(1,pmdata.getType());
        assertEquals("testParameter1",pmdata.getValue());
        assertFalse(pmdata.isEditable());
        assertFalse(pmdata.isLocked());

        //all parameters added to the service group parameters
        perArray = sGadmin.getServiceGroupParameters(tSg.getServiceGroupName());
        assertEquals(4,perArray.length);

        //remove a parameter
        sGadmin.removeServiceGroupParameter(tSg.getServiceGroupName(),"testParam3");

        //now check the number of available parameters
        perArray = sGadmin.getServiceGroupParameters(tSg.getServiceGroupName());
        assertEquals(3,perArray.length);
        
    }


    public void createAnAxisService() throws Exception{

        String paraName = ServerConstants.SERVICE_TYPE;
        String param1 = ("<parameter name='"+ paraName +"'>"+"serviceType"+"</parameter>");
        XMLStreamReader xmlSR = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(param1.getBytes()));
        OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
        Parameter parameter = ParameterUtil.createParameter(paramEle);


        AxisServiceGroup testGrp = new AxisServiceGroup();
        AxisService testSer = new AxisService();
        testSer.setName("testService");
        testGrp.addService(testSer);
        testGrp.addParameter(parameter);
        

        axisCon.addServiceGroup(testGrp);
        


    }


    public ParameterMetaData[] setPramMetaData() throws Exception{
        
        ParameterMetaData[] pmData = new ParameterMetaData[2];
        ParameterMetaData pm1 = new ParameterMetaData();
        ParameterMetaData pm2 = new ParameterMetaData();

        pm1.setName("param1");
        pm1.setType(1);
        pm1.setValue("parameter1");
        pm1.setEditable(true);
        pm1.setLocked(true);

        pm2.setName("param1");
        pm2.setType(1);
        pm2.setValue("parameter1");
        pm2.setEditable(true);
        pm2.setLocked(true);

        pmData[0] = pm1;
        pmData[1] = pm2;

        return pmData;

    }

    
}
