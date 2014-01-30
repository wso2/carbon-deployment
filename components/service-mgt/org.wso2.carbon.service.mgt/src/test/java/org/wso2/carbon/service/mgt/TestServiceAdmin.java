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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Version;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.multitenancy.TenantAxisConfigurator;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.WSO2Constants;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

public class TestServiceAdmin extends BaseTestCase {
  
    private String repoPath = "src/test/resources/repository/";
    private static final String EMPTY_POLICY ="<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" />";
    private static final String TARGET_POLICY = "<wsp:Policy xmlns:wsp=\" http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
                "<wsp:ExactlyOne> " +
                    "<wsp:All>" +
                        "<sp:Basic256Rsa15 />" +
                    "</wsp:All>" +
                "</wsp:ExactlyOne>" +
            "</wsp:Policy>";

    private AxisConfiguration axisConfig;
    private ServiceAdmin SerAdmin;
    private AxisService echoService;
    private AxisService weatherService;
    private AxisModule newModule;
    private String serName;
    private AxisBinding soapBinding;
    private  AxisBinding soapBinding12;


    public void setUp() throws Exception{
        super.setUp();
    }


    public void testSteps() throws Exception{
        serAdminConfigStuff();
        serAdminAddPolicyStuff();
        serAdminfirst();
        serAdminPolicyBindingstuff();
    }



    public void serAdminConfigStuff() throws Exception{

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath);
//        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath,axis2XmlPath);
        axisConfig = configContext.getAxisConfiguration();
        axisConfig.setRepository(new URL("file://" + new File(repoPath).getAbsolutePath()));

                createAnAxisService();
        createAnAxisModule();

        soapBinding = new AxisBinding();
        soapBinding.setName(new QName(Java2WSDLConstants.BINDING_NAME_SUFFIX));
        soapBinding.setType(WSDL2Constants.URI_WSDL2_SOAP);

        soapBinding12 = new AxisBinding();
        soapBinding12.setName(new QName(Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
        soapBinding12.setType(WSDL2Constants.URI_WSDL2_SOAP);

        echoService = axisConfig.getService("echo");
       
        NewResourcetoRegistryForService( echoService.getName(),soapBinding.getName().toString());
        NewResourcetoRegistryForService( weatherService.getName(),soapBinding.getName().toString());
        NewResourcetoRegistryForOperation(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoOMElement").getName().toString());
        NewResourcetoRegistryForOperation(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoString").getName().toString());
        testNewResourceRegistryForOperation(weatherService.getName(),weatherService.getOperationByAction("c2f").getName().toString());


        handleNewModuleAddition(newModule, newModule.getName(), newModule.getVersion().toString());
        PrivilegedCarbonContext.getCurrentContext(axisConfig).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, configRegistry);
        PrivilegedCarbonContext.getCurrentContext(axisConfig).setRegistry(
                RegistryType.SYSTEM_CONFIGURATION, governanceRegistry);
            
        // The following line of code is kept for backward compatibility. Remove this once we
        // are certain that this is not required. -- Senaka.
        axisConfig.addParameter(WSO2Constants.CONFIG_SYSTEM_REGISTRY_INSTANCE, configRegistry);
        
        echoService = axisConfig.getService("echo");
        serName = axisConfig.getService("echo").getName();
        assertEquals("echo" , serName);

        AxisService ws = axisConfig.getService("weather");
        assertEquals("weather" , ws.getName());

        ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_POLICY.getBytes());
        Policy pol1 = PolicyEngine.getPolicy(bais);
        pol1.setId("pol1");
        pol1.setName("testPolicy");

        SerAdmin = new ServiceAdmin(axisConfig);
        SerAdmin.setConfigurationContext(configContext);

    }



    public void serAdminAddPolicyStuff() throws Exception{

        ServiceGroupMetaData m1 = SerAdmin.listServiceGroup("echo");
        assertEquals("false",m1.getMtomStatus());
        assertEquals("echo",m1.getServiceGroupName());


        int aa = SerAdmin.getNumberOfActiveServices();
        int bb = SerAdmin.getNumberOfInactiveServices();
        int cc = SerAdmin.getNumberOfFaultyServices(); 

        assertEquals(3,aa);
        assertEquals(0,bb);
        assertEquals(0,cc);


        AxisService testSer = axisConfig.getService("testService");
        String[] serGroup = new String[1];
        serGroup[0] = testSer.getAxisServiceGroup().getServiceGroupName();
        SerAdmin.deleteServiceGroups(serGroup);


//        aa = SerAdmin.getNumberOfActiveServices();
//        bb = SerAdmin.getNumberOfInactiveServices();
        
//        assertEquals(2,aa);
//        assertEquals(0,bb);

    }



    public void serAdminfirst() throws Exception{
        
        //ServiceMetaData weatherMData = SerAdmin.getServiceData(weatherService.getName());TODO

        SerAdmin.startService(weatherService.getName());
        assertTrue(weatherService.isActive());

        SerAdmin.stopService(weatherService.getName());
        int bb = SerAdmin.getNumberOfInactiveServices();
        assertEquals(1,bb);
        assertFalse(weatherService.isActive());

        SerAdmin.changeServiceState(weatherService.getName(),true);//***
        assertTrue(weatherService.isActive());
        //SerAdmin.startService(weatherService.getName());

        SerAdmin.configureMTOM("paraTestValue",serName);


        ServiceMetaData serviceMetaData = new ServiceMetaData();
        serviceMetaData.setServiceId(serName);
        String[] getTrans = SerAdmin.getExposedTransports(serviceMetaData.getServiceId());
        assertEquals(1, getTrans.length);


        ServiceMetaData serMetaData1 = new ServiceMetaData();
        serMetaData1.setServiceId(weatherService.getName());
        //String s = SerAdmin.addTransportBinding(serMetaData1.getServiceId(),"FTP");TODO


        String setParam[]=new String[3];
        String paraName="testParamOne";
        String paraValue="testValueOne";
        String para1Name="testParamTwo";
        String para1Value="testValueTwo";
        String para2Name="testParamThree";
        String para2Value="testValueThree";


        setParam[0]=("<parameter name='"+paraName+"'>"+paraValue+"</parameter>");
        setParam[1]=("<parameter name='"+para1Name+"'>"+para1Value+"</parameter>");
        setParam[2]=("<parameter name='"+para2Name+"'>"+para2Value+"</parameter>");


        SerAdmin.setServiceParameters(serName,setParam);
        String[] serviceParam = SerAdmin.getServiceParameters(serName);
        assertEquals(5,serviceParam.length);


        SerAdmin.removeServiceParameter(serName,paraName);
        serviceParam = SerAdmin.getServiceParameters(serName);
        assertEquals(4,serviceParam.length);

        
        String polName = SerAdmin.getPolicy(serName);
        assertEquals(EMPTY_POLICY,polName);

        PolicyMetaData[] pmData = SerAdmin.getPolicies(weatherService.getName());
        assertEquals(0,pmData.length);

        PolicyMetaData[] pmData1 = SerAdmin.getPolicies(echoService.getName());
        assertEquals(0,pmData1.length);


         SerAdmin.setPolicy(weatherService.getName(),EMPTY_POLICY);
         pmData = SerAdmin.getPolicies(weatherService.getName());
         assertEquals(1,pmData.length);

        
         String[] pc = pmData[0].getPolycies();
         String wrap = pmData[0].getWrapper();
         assertEquals(1,pc.length);
         assertEquals("Policies that are applicable for weather service",wrap);

         String getPolC = SerAdmin.getPolicy(weatherService.getName());
         System.out.println("1. "+getPolC);

         AxisOperation axOp = weatherService.getOperationByAction("c2f");
        
        SerAdmin.setServiceOperationPolicy( weatherService.getName(), axOp.getName().toString(), TARGET_POLICY );
         String OpPolicy = SerAdmin.getOperationPolicy( weatherService.getName(), axOp.getName().toString());
         System.out.println("2. "+OpPolicy);
        
 
         SerAdmin.setServiceOperationMessagePolicy( weatherService.getName(), axOp.getName().toString(), "In", EMPTY_POLICY);
         OpPolicy = SerAdmin.getOperationMessagePolicy( weatherService.getName(), axOp.getName().toString(), "In"); 
         System.out.println("3. "+OpPolicy);
        
    }



    public void serAdminPolicyBindingstuff() throws Exception{

        String testData = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" />";

        String gBp = SerAdmin.getBindingPolicy(weatherService.getName(),"wso2"+Java2WSDLConstants.HTTP_BINDING);        //System.out.println("4. " +gBp);
        assertEquals(testData,gBp);

        AxisEndpoint soapEndpoint;
        AxisEndpoint soap12Endpoint;

        soapEndpoint = new AxisEndpoint();
        String soapEndpointName = ""+ WSDL2Constants.DEFAULT_SOAP11_ENDPOINT_NAME;
        soapEndpoint.setName(soapEndpointName);
        soapEndpoint.setBinding(soapBinding);
        soapEndpoint.setParent(echoService);
        soapEndpoint.setTransportInDescription("http");
        echoService.addEndpoint(soapEndpointName,soapEndpoint);

        soap12Endpoint = new AxisEndpoint();
        String soap12EndpointName = ""+ WSDL2Constants.DEFAULT_SOAP12_ENDPOINT_NAME;
        soap12Endpoint.setName(soap12EndpointName);
        soap12Endpoint.setBinding(soapBinding12);
        soap12Endpoint.setParent(echoService);
        soap12Endpoint.setTransportInDescription("http");
        echoService.addEndpoint(soap12EndpointName,soap12Endpoint);


        SerAdmin.setBindingOperationPolicy(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoOMElement").getName().toString(),EMPTY_POLICY);
        String gBOP = SerAdmin.getBindingOperationPolicy(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoOMElement").getName().toString());
        assertEquals(testData,gBOP);

        SerAdmin.setBindingOperationMessagePolicy(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoString").getName().toString(),"In",EMPTY_POLICY);
        String gBOMP = SerAdmin.getBindingOperationMessagePolicy(echoService.getName(),soapBinding12.getName().toString(),echoService.getOperationByAction("echoString").getName().toString(),"In");
        assertEquals(testData,gBOMP);

        SerAdmin.setBindingPolicy(echoService.getName(),soapBinding.getName().toString(),TARGET_POLICY);
        String echoG = SerAdmin.getBindingPolicy(echoService.getName(),Java2WSDLConstants.BINDING_NAME_SUFFIX);
        System.out.println("4. "+echoG);


        //ByteArrayInputStream bais = new ByteArrayInputStream(TARGET_POLICY.getBytes());
        //Policy policy = PolicyEngine.getPolicy(bais);
        //policy.setId(UUIDGenerator.getUUID());
        // SerAdmin.removeBindingPolicy(weatherService.getName(),policy.getId(),null); //TODO


         SerAdmin.setPolicy(echoService.getName(),TARGET_POLICY);
         PolicyMetaData[] pmData1 = SerAdmin.getPolicies(echoService.getName());
         assertEquals(1,pmData1.length);


         String[] bindList = SerAdmin.getServiceBindings(echoService.getName());
         assertEquals(4,bindList.length);

    }



    public void createAnAxisService() throws Exception{

        QName serviceName = new QName("weather");
        QName operationName = new QName("c2f");
        weatherService =
                org.apache.axis2.util.Utils.createSimpleService(serviceName,org.wso2.carbon.service.mgt.weather.weather.class.getName(),operationName);

        AxisBinding httpBinding = new AxisBinding();
        httpBinding.setName(new QName("wso2"+ Java2WSDLConstants.HTTP_BINDING));
        httpBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);

        AxisEndpoint httpEndpoint;
        httpEndpoint = new AxisEndpoint();
        String httpEndpointName = ""+ WSDL2Constants.DEFAULT_HTTP_ENDPOINT_NAME;
        httpEndpoint.setName(httpEndpointName);
        httpEndpoint.setBinding(httpBinding);
        httpEndpoint.setParent(weatherService);
        httpEndpoint.setTransportInDescription("http");
        weatherService.addEndpoint(httpEndpointName,httpEndpoint);

        axisConfig.addService(weatherService);

        AxisServiceGroup testGrp = new AxisServiceGroup();
        AxisService testSer = new AxisService();
        testSer.setName("testService");
        testGrp.addService(testSer);

        axisConfig.addServiceGroup(testGrp);

    }


    public void createAnAxisModule() throws Exception{

        newModule = new AxisModule();
        newModule.setName("testNewModule");
        newModule.setModuleDescription("module created for testing");
        newModule.setVersion(new Version("1"));
        axisConfig.addModule(newModule);
        
    }


    public void NewResourcetoRegistryForService(String serviceName, String policyBinding)throws Exception{

        AxisService axic = axisConfig.getServiceForActivation(serviceName);

        String servicePath = RegistryResources.SERVICE_GROUPS
                + axic.getAxisServiceGroup().getServiceGroupName()
                + RegistryResources.SERVICES + axic.getName();

        String bindingResourcePath = servicePath + RegistryResources.ServiceProperties.BINDINGS + policyBinding;
        Resource bindingResource = configRegistry.newResource();
        configRegistry.put(bindingResourcePath, bindingResource);
       
    }

    
    public void testNewResourceRegistryForOperation(String serviceName,String operationName)throws Exception{
        AxisService axisService = axisConfig.getServiceForActivation(serviceName);

        String servicePath = RegistryResources.SERVICE_GROUPS
                + axisService.getAxisServiceGroup().getServiceGroupName()
                + RegistryResources.SERVICES + axisService.getName();

        String OperationResourcePath = servicePath + RegistryResources.ServiceProperties.OPERATIONS +operationName;
        Resource OperationResource = configRegistry.newResource();
        configRegistry.put(OperationResourcePath, OperationResource);
    }
    
    public void NewResourcetoRegistryForOperation(String serviceName,String bindingName,String operationName)throws Exception{
        AxisService axisService = axisConfig.getServiceForActivation(serviceName);

        String servicePath = RegistryResources.SERVICE_GROUPS
                + axisService.getAxisServiceGroup().getServiceGroupName()
                + RegistryResources.SERVICES + axisService.getName();

        String bindingOperationResourcePath =
                servicePath + RegistryResources.ServiceProperties.BINDINGS+ bindingName +
                RegistryResources.ServiceProperties.OPERATIONS +operationName;
        Resource bindingOperationResource =  configRegistry.newResource();
        configRegistry.put(bindingOperationResourcePath, bindingOperationResource);
    }


    public void handleNewModuleAddition(AxisModule axisModule, String moduleName,String moduleVersion) throws Exception {
        try{
            configRegistry.beginTransaction();
            Resource module = configRegistry.newCollection();
            module.addProperty(RegistryResources.ModuleProperties.NAME,moduleName);

            if (!moduleVersion.equals(RegistryResources.ModuleProperties.UNDEFINED)) {
                module.addProperty(RegistryResources.ModuleProperties.VERSION, moduleVersion);
            }
            AxisConfigurator configurator = axisConfig.getConfigurator();
            boolean isGloballyEngaged = false;
            if(configurator instanceof CarbonAxisConfigurator) {
                isGloballyEngaged =
                ((CarbonAxisConfigurator) configurator).isGlobalyEngaged(axisModule);
            } else if (configurator instanceof TenantAxisConfigurator) {
                isGloballyEngaged =
                ((TenantAxisConfigurator) configurator).isGlobalyEngaged(axisModule);
            }
            module.addProperty(RegistryResources.ModuleProperties.GLOBALLY_ENGAGED,
                               String.valueOf(isGloballyEngaged));

            String registryResourcePath = RegistryResources.MODULES + moduleName + "/" + moduleVersion + "/";
            configRegistry.put(registryResourcePath, module);
            
        }catch(Throwable e) {
            configRegistry.rollbackTransaction();
            throw new Exception(e);
        }

    }

}
