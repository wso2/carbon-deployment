/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ejbservices.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.rpc.receivers.ejb.EJB3Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.ejbservices.component.xml.EJBAppServerConfig;
import org.wso2.carbon.ejbservices.service.util.EJBAppServerData;
import org.wso2.carbon.ejbservices.service.util.EJBProviderData;
import org.wso2.carbon.ejbservices.service.util.UploadedFileItem;
import org.wso2.carbon.ejbservices.service.util.WrappedAllConfigurations;
import org.wso2.carbon.ejbservices.util.EJBConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("UnusedDeclaration")
public class EJBServicesAdmin extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EJBServicesAdmin.class);

    private static Registry registry;
    private static EJBAppServerConfig[] appServerConfigs;

    public static void setRegistry(Registry registryParam) {
        registry = registryParam;
    }

    public static void setEJBAppServerConfig(EJBAppServerConfig[] serverConfigs) {
        if(serverConfigs == null) {
            appServerConfigs = new EJBAppServerConfig[0];
        } else {
            appServerConfigs = Arrays.copyOf(serverConfigs, serverConfigs.length);
        }
    }

    public void addApplicationServer(String providerURL,
                                     String jndiContextClass,
                                     String userName,
                                     String password,
                                     String appServerType) throws Exception {
        try {
            String configPath = EJBConstants.APP_SERVERS;
            Collection configResourceCollection;
            String appServerID;
            String configResourcePath;
            Resource configResource;

            // Example path of a appServer /carbon/app.servers/1
            // Here numeric value has been  used as primary key, because providerURL cannot be used. providerURL string
            // contains some characters which are not allowed by the registry.
            if (registry.resourceExists(configPath)) {
                configResourceCollection = (Collection) registry.get(configPath);
                String[] configResourcePaths = configResourceCollection.getChildren();
                for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                    configResourcePath = configResourcePaths[i];
                    configResource = registry.get(configResourcePath);
                    if (providerURL.equals(configResource.getProperty(
                            EJBConstants.AppServerProperties.PROVIDER_URL))) {
                            throw new Exception("Application Server already exists");
                    }
                }
                appServerID = String.valueOf(configResourceCollection.getChildCount() + 1);
            } else {
                appServerID = "1";
            }

            Resource appServerResource = registry.newResource();
            appServerResource.addProperty(
                    EJBConstants.AppServerProperties.PROVIDER_URL, providerURL);
            appServerResource.addProperty(
                    EJBConstants.AppServerProperties.JNDI_CONTEXT_CLASS, jndiContextClass);
            appServerResource.addProperty(
                    EJBConstants.AppServerProperties.USER_NAME, userName);
            appServerResource.addProperty(
                    EJBConstants.AppServerProperties.PASSWORD, password);
            appServerResource.addProperty(
                    EJBConstants.AppServerProperties.APP_SERVER_TYPE, appServerType);

            registry.put(EJBConstants.APP_SERVERS + appServerID, appServerResource);
        } catch (RegistryException e) {
            log.error("Unable to add the application server", e);
            throw e;
        }
    }

    public void addEJBConfiguration(String serviceName,
                                    String providerURL,
                                    String jndiContextClass,
                                    String userName,
                                    String password,
                                    String beanJNDIName,
                                    String remoteInterface,
                                    String appServerType) throws Exception {
        try {
            String configPath = EJBConstants.CONFIGURATIONS;
            Collection configResourceCollection;
            String ejbConfigurationID;
            String configResourcePath;
            Resource configResource;

            if (registry.resourceExists(configPath)) {
                configResourceCollection = (Collection) registry.get(configPath);
                String[] configResourcePaths = configResourceCollection.getChildren();
                for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                    configResourcePath = configResourcePaths[i];
                    configResource = registry.get(configResourcePath);
                    if (serviceName.equals(configResource.getProperty(
                            EJBConstants.ConfigProperties.SERVICE_NAME))) {
                        throw new Exception("EBJ Configuration already exists");
                    }
                }
                ejbConfigurationID = String.valueOf(configResourceCollection.getChildCount() + 1);
            } else {
                ejbConfigurationID = "1";
            }

            Resource ejbConfigResource = registry.newResource();
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.SERVICE_NAME, serviceName);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.PROVIDER_URL, providerURL);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.JNDI_CONTEXT_CLASS, jndiContextClass);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.USER_NAME, userName);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.PASSWORD, password);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.BEAN_JNDI_NAME, beanJNDIName);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.REMOTE_INTERFACE, remoteInterface);
            ejbConfigResource.addProperty(
                    EJBConstants.ConfigProperties.APP_SERVER_TYPE, appServerType);

            //TODO use EJBConstants.CONFIGURATIONS + serviceName as reg path
            registry.put(EJBConstants.CONFIGURATIONS + ejbConfigurationID, ejbConfigResource);
        } catch (RegistryException e) {
            log.error("Unable to add EJB Configuration.", e);
            throw e;
        }
    }

    public EJBProviderData[] getEJBConfigurations() throws Exception {
        String configPath = EJBConstants.CONFIGURATIONS;
        Collection configResourceCollection;
        try {
            if (!registry.resourceExists(configPath)) {
                return new EJBProviderData[0];
            }
            configResourceCollection = (Collection) registry.get(configPath);
            String[] configResourcePaths = configResourceCollection.getChildren();
            String configResourcePath;
            Resource configResource;
            EJBProviderData[] ejbProviderDataObjs =
                    new EJBProviderData[configResourceCollection.getChildCount()];

            for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                configResourcePath = configResourcePaths[i];
                configResource = registry.get(configResourcePath);
                ejbProviderDataObjs[i] = getEJBProviderData(configResource);
            }
            return ejbProviderDataObjs;
        } catch (RegistryException e) {
            log.error("Unable to get EJB Configurations", e);
            throw e;
        }
    }

    //todo user servie Name as well for uniquely recognizing ejb-config - done
    public EJBProviderData getEJBConfiguration(String serviceName)
            throws Exception {

        String configPath = EJBConstants.CONFIGURATIONS;
        if (registry.resourceExists(configPath)) {
            Collection configResourceCollection;
            String configResourcePath;
            Resource configResource;

            configResourceCollection = (Collection) registry.get(configPath);
            String[] configResourcePaths = configResourceCollection.getChildren();
            for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                configResourcePath = configResourcePaths[i];
                configResource = registry.get(configResourcePath);
                if (serviceName.equals(configResource.getProperty(
                        EJBConstants.ConfigProperties.PROVIDER_URL))) {

                    EJBProviderData ejbProviderData = new EJBProviderData();
                    ejbProviderData.setProviderURL(configResource.getProperty(
                            EJBConstants.ConfigProperties.PROVIDER_URL));
                    ejbProviderData.setJndiContextClass(configResource.getProperty(
                            EJBConstants.ConfigProperties.JNDI_CONTEXT_CLASS));
                    ejbProviderData.setUserName(configResource.getProperty(
                            EJBConstants.ConfigProperties.USER_NAME));
                    ejbProviderData.setPassword(configResource.getProperty(
                            EJBConstants.ConfigProperties.PASSWORD));
                    ejbProviderData.setBeanJNDIName(configResource.getProperty(
                            EJBConstants.ConfigProperties.BEAN_JNDI_NAME));
                    String remoteInterface = configResource.getProperty(
                            EJBConstants.ConfigProperties.REMOTE_INTERFACE);
                    ejbProviderData.setRemoteInterface(remoteInterface);
                    ejbProviderData.setServiceName(configResource.getProperty(
                            EJBConstants.ConfigProperties.SERVICE_NAME));
                    return ejbProviderData;
                }
            }
        }
        return null;
    }

    public void deleteEJBConfiguration(String serviceName)
            throws Exception {
        String configPath = EJBConstants.CONFIGURATIONS;

        try {
            registry.beginTransaction();

            if (registry.resourceExists(configPath)) {
                Collection configResourceCollection;
                String configResourcePath;
                Resource configResource;

                configResourceCollection = (Collection) registry.get(configPath);
                String[] configResourcePaths = configResourceCollection.getChildren();
                for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                    configResourcePath = configResourcePaths[i];
                    configResource = registry.get(configResourcePath);
                    if (serviceName.equals(configResource.getProperty(
                            EJBConstants.ConfigProperties.SERVICE_NAME))) {
                        registry.delete(configResourcePath);
                    }
                }
            }

            registry.commitTransaction();

            ServiceAdmin serviceAdmin = new ServiceAdmin();
            serviceAdmin.deleteServiceGroups(new String[]{serviceName});
        } catch (Exception e) {
            registry.rollbackTransaction();
            throw (e);
        }
    }

    public EJBAppServerData[] getAppServerNameList() {
        EJBAppServerData[] ejbAppServerList = new EJBAppServerData[appServerConfigs.length];
        int i = 0;
        for (EJBAppServerConfig appServerConfig : appServerConfigs) {
            EJBAppServerData ejbAppServerData = new EJBAppServerData();
            ejbAppServerData.setServerId(appServerConfig.getId());
            ejbAppServerData.setServerName(appServerConfig.getName());
            ejbAppServerData.setProviderURL(appServerConfig.getProviderURL());
            ejbAppServerData.setJndiContextClass(appServerConfig.getJndiContextClass());
            ejbAppServerList[i++] = ejbAppServerData;
        }
        return ejbAppServerList;
    }

    public EJBAppServerData[] getEJBAppServerConfigurations() throws Exception {

        String configPath = EJBConstants.APP_SERVERS;
        Collection configResourceCollection;
        try {
            if (!registry.resourceExists(configPath)) {
                return new EJBAppServerData[0];
            }
            configResourceCollection = (Collection) registry.get(configPath);
            String[] configResourcePaths = configResourceCollection.getChildren();
            String configResourcePath;
            Resource configResource;
            EJBAppServerData[] ejbAppServerDataObjs =
                    new EJBAppServerData[configResourceCollection.getChildCount()];

            for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                configResourcePath = configResourcePaths[i];
                configResource = registry.get(configResourcePath);
                ejbAppServerDataObjs[i] = new EJBAppServerData();
                ejbAppServerDataObjs[i].setAppServerType(configResource.getProperty(
                        EJBConstants.AppServerProperties.APP_SERVER_TYPE));
                ejbAppServerDataObjs[i].setProviderURL(configResource.getProperty(
                        EJBConstants.AppServerProperties.PROVIDER_URL));
            }
            return ejbAppServerDataObjs;
        } catch (RegistryException e) {
            log.error("Unable to get EJB Configurations", e);
            throw e;
        }
    }

    public String[] getClassNames(String archiveId) throws AxisFault {
        String filePath = getFilePathFromArchiveId(archiveId);

        if (filePath != null) {
            try {
                String[] entries = new ArchiveManipulator().check(filePath);
                java.util.Collection<String> classNames = new ArrayList<String>();

                for (String entry : entries) {

                    if (entry.endsWith(".class")) {
                        entry = entry.replace('/', '.').substring(0, entry.lastIndexOf(".class"));
                        classNames.add(entry);
                    }
                }

                return classNames.toArray(new String[classNames.size()]);
            } catch (IOException e) {
                String msg = "Could not read archive";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }

        return new String[]{""};
    }

    public void createAndDeployEJBService(String archiveId, String serviceName,
                                          String[] serviceClasses, String jnpProviderUrl,
                                          String beanJNDIName, String remoteInterface)
            throws Exception {
        // Find details of selected ejb application server configuration
        String password = null;
        EJBAppServerData ejbAppServerData = getEJBAppServerConfiguration(jnpProviderUrl, password);
        if (ejbAppServerData == null) {
            throw new AxisFault("Non-existance Application server configuration");
        }

        // search for existing configuration, if found abort service deployment
        try {
            //todo identify with service name as well : getEJBConfiguration(beanJNDIName, jnpProviderUrl. serviceName)
            EJBProviderData ejbProviderData = getEJBConfiguration(serviceName);
            if (ejbProviderData != null) {
                // configuration found.Throw exception
                throw new AxisFault("A Service exists for the provided Service Name(" +
                        serviceName + ").");
            }
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }

        addEJBConfiguration(serviceName,
                ejbAppServerData.getProviderURL(),
                ejbAppServerData.getJndiContextClass(),
                ejbAppServerData.getUserName(),
                ejbAppServerData.getUserName(),
                beanJNDIName,
                remoteInterface,
                ejbAppServerData.getAppServerType());

        String filePathFromArchiveId = getFilePathFromArchiveId(archiveId);

        if (filePathFromArchiveId == null) {
            String msg = "A non-existent file was requested";
            log.warn(msg);
            throw new AxisFault(msg);
        }

        int endIndex = filePathFromArchiveId.lastIndexOf(File.separator);
        String filePath = filePathFromArchiveId.substring(0, endIndex);
//        String archiveFileName = filePathFromArchiveId.substring(endIndex);
//        archiveFileName = archiveFileName.substring(0, archiveFileName.lastIndexOf("."));

        ArchiveManipulator archiveManipulator = new ArchiveManipulator();

        // ----------------- Unzip the file ------------------------------------
        String unzippeDir = filePath + File.separator + "temp";
        File unzipped = new File(unzippeDir);
        unzipped.mkdirs();

        try {
            archiveManipulator.extract(filePathFromArchiveId, unzippeDir);
        } catch (IOException e) {
            throw new AxisFault("Cannot extract archive", e);
        }

        // ---- Generate the services.xml and place it in META-INF -----
        File file =
                new File(unzippeDir + File.separator + "META-INF" + File.separator +
                         "services.xml");
        file.mkdirs();

        try {
            File absoluteFile = file.getAbsoluteFile();
            if (absoluteFile.exists()) {
                absoluteFile.delete();
            }
            absoluteFile.createNewFile();
            OutputStream os = new FileOutputStream(file);
            OMElement servicesXML = createServicesXMLForEJBService(serviceName,
                    serviceClasses, ejbAppServerData.getProviderURL(),
                    ejbAppServerData.getJndiContextClass(),
                    ejbAppServerData.getUserName(),
                    password,
                    beanJNDIName,
                    remoteInterface);
            servicesXML.build();
            servicesXML.serialize(os);
        } catch (Exception e) {
            String msg = "Cannot write services XML";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        // ----------------- Create the AAR ------------------------------------
        // These are the files to include in the ZIP file
        String outAARFilename = filePath + File.separator + serviceName + ".aar";

        try {
            archiveManipulator.archiveDir(outAARFilename, unzipped.getPath());
        } catch (IOException e) {
            String msg = "Cannot create new AAR archive";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        String servicesDir = getAxisConfig().getParameter(
                DeploymentConstants.SERVICE_DIR_PATH).getValue().toString();

        // ------------- Copy the AAR to the respository/services directory
        String repo = getAxisConfig().getRepository().getPath() + File.separator + servicesDir;

        try {
            File aarInRepo = new File(repo + File.separator + serviceName + ".aar");
            FileManipulator.copyFile(new File(outAARFilename), aarInRepo);

/*
            //If the repo is in the registry, copy the artifact into registry based repo also
            String registryRepoPath = CarbonUtils.getRegistryRepoPath();
            if (registryRepoPath != null) {
                FileInputStream fis = new FileInputStream(aarInRepo);
                RegistryRepoHandler.storeArtifactInRegistry(fis, registryRepoPath +
                        File.separator + servicesDir + File.separator + archiveFileName + ".aar");
            }
*/
        } catch (IOException e) {
            String msg = "Cannot copy AAR file to Repo";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    public String uploadService(UploadedFileItem fileItem) throws AxisFault {
        try {
            ConfigurationContext configurationContext =
                    MessageContext.getCurrentMessageContext().getConfigurationContext();

            String fileName = fileItem.getFileName();

            File uploadedFile;
            String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
            String serviceUploadDir = configurationContext.getProperty(ServerConstants.WORK_DIR) +
                    File.separator + "services" + File.separator + uuid + File.separator;
            File servicesDir = new File(serviceUploadDir);
            servicesDir.mkdirs();
            uploadedFile = new File(servicesDir, fileName);

            Map fileResourceMap =
                    (Map) configurationContext.getProperty(ServerConstants.FILE_RESOURCE_MAP);

            fileResourceMap.put(uuid, uploadedFile.getAbsolutePath());
            FileOutputStream fileOutStream = new FileOutputStream(uploadedFile);

            fileItem.getDataHandler().writeTo(fileOutStream);
            fileOutStream.flush();
            fileOutStream.close();
            return uuid;
        } catch (Exception e) {
            throw new AxisFault("Error occurred while uploading service artifacts", e);
        }
    }

//    public void setServiceParameters(String serviceName,
//                                     String[] parameters) throws AxisFault {
//
//        ServiceAdmin serviceAdmin = getServiceAdmininstance();
//        if (serviceAdmin == null) {
//            log.error("Error updating service parameters. ServiceAdmin instance is null");
//            throw new AxisFault("Error updating service parameters");
//        }
//        serviceAdmin.setServiceParameters(serviceName, parameters);
//
//    }

    public void setServiceParameters(String serviceName,
                                     String[] parameters) throws AxisFault {

        for (String parameter : parameters) {
            setServiceParameter(serviceName, parameter);
        }
    }

//    public String[] getServiceParameters(String serviceName) throws AxisFault{
//        if(serviceName == null){
//            log.error("Invalide Service name");
//            throw new AxisFault("Invalide Service name");
//        }
//
//        ServiceAdmin serviceAdmin = getServiceAdmininstance();
//        if (serviceAdmin == null) {
//            log.error("Error getting service parameters. ServiceAdmin instance is null");
//            throw new AxisFault("Error getting service parameters. ServiceAdmin instance is null");
//        }
//
//        try{
//            return serviceAdmin.getServiceParameters(serviceName);
//        }catch(Exception e){
//            log.error("Error getting service parameters", e);
//            throw new AxisFault("Error getting service parameters", e.getMessage());
//        }
//    }

    public String[] getServiceParameters(String serviceName) throws AxisFault {

        if (serviceName == null) {
            String msg = "Invalid Service name";
            log.error(msg);
            throw new AxisFault(msg);
        }

        AxisService service = getAxisService(serviceName);
        if (service == null) {
            String msg = "Service cannot be found for the name : " + serviceName;
            log.error(msg);
            throw new AxisFault(msg);
        }

        ArrayList<Parameter> parameters = service.getParameters();
        try {
            List<String> params = new ArrayList<String>();
            for (Parameter param : parameters) {
                OMElement paramEle = param.getParameterElement();
                if (paramEle != null) {
                    params.add(paramEle.toString());
                }
            }
            return params.toArray(new String[params.size()]);
        } catch (Exception e) {
            String msg = "Error occurred while getting parameters of service : " + serviceName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    public WrappedAllConfigurations getAllConfigurations() throws Exception {
        WrappedAllConfigurations allConfigurations = new WrappedAllConfigurations();
        allConfigurations.setEjbProviderData(getEJBConfigurations());
        allConfigurations.setAppServerData(getEJBAppServerConfigurations());
        allConfigurations.setAppServerNameList(getAppServerNameList());
        return allConfigurations;
    }

    public boolean testAppServerConnection(String providerURL,
                                        String jndiContextClass, String userName, String password)
            throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.SECURITY_PRINCIPAL, userName);
        properties.setProperty(Context.SECURITY_CREDENTIALS, password);
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, jndiContextClass);
        properties.setProperty(Context.PROVIDER_URL, providerURL);

        //try to get context using these properties
        try {
            InitialContext context = new InitialContext(properties);
            return true;
        } catch (NamingException e) {
            log.info("AppServer Connection Test Failed", e);
            return false;
        } catch (Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void setServiceParameter(String serviceName,
                                     String parameterStr) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);

        if (axisService == null) {
            throw new AxisFault("invalid service name service not found : " + serviceName);
        }

        OMElement paramEle = null;
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(parameterStr.getBytes()));
            paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (XMLStreamException e) {
            String msg = "Cannot create OMElement from parameter: " + parameterStr;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        Parameter parameter = ParameterUtil.createParameter(paramEle);
        if (axisService.getParameter(parameter.getName()) != null) {
            if (!axisService.getParameter(parameter.getName()).isLocked()) {
                axisService.addParameter(parameter);
            }
        } else {
            axisService.addParameter(parameter);
        }

        try {
            PersistenceFactory pf = PersistenceFactory.getInstance(getAxisConfig());
            ServicePersistenceManager spm = pf.getServicePM();
            spm.updateServiceParameter(axisService, parameter);
        } catch (Exception e) {
            String msg = "Cannot persist service parameter change for service " + serviceName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private AxisService getAxisService(String serviceName) throws AxisFault {
        return getAxisConfig().getServiceForActivation(serviceName);
    }

//    private ServiceAdmin getServiceAdmininstance() {
//        ServiceReference sr = bundleContex.getServiceReference(ServiceAdmin.class.getName());
//        if (sr != null)
//            return (ServiceAdmin) bundleContex.getService(sr);
//
//        return null;
//    }

    private OMElement createServicesXMLForEJBService(String serviceName, String[] serviceClasses,
                                                     String providerURL, String jndiContextClass,
                                                     String userName,
                                                     String password, String beanJNDIName,
                                                     String remoteInterface) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace emptyNS = factory.createOMNamespace("", "");
        OMElement serviceGroupEle = factory.createOMElement("serviceGroup", "",
                "");

        // Although we have a loop here, only one class will come
        for (String serviceClass : serviceClasses) {
            OMElement serviceEle = factory.createOMElement("service", "", "");

            OMElement schemaEle = factory.createOMElement("schema", "", "");
            schemaEle.addAttribute(factory.createOMAttribute("elementFormDefaultQualified", emptyNS,
                                                             "false"));

            serviceEle.addAttribute(factory.createOMAttribute("name", emptyNS, serviceName));

            OMElement msgReceiversEle = factory.createOMElement("messageReceivers", "", "");
            OMElement msgReceiverEle1 = factory.createOMElement("messageReceiver", "", "");
            msgReceiverEle1.addAttribute("mep", "http://www.w3.org/ns/wsdl/in-only", emptyNS);
            msgReceiverEle1.addAttribute("class",
                    "org.apache.axis2.rpc.receivers.ejb.EJB3InOnlyMessageReceiver", emptyNS);

            OMElement msgReceiverEle2 = factory.createOMElement("messageReceiver", "", "");
            msgReceiverEle2.addAttribute("mep", "http://www.w3.org/ns/wsdl/in-out", emptyNS);
            msgReceiverEle2.addAttribute("class",
                "org.apache.axis2.rpc.receivers.ejb.EJB3MessageReceiver", emptyNS);
            msgReceiversEle.addChild(msgReceiverEle1);
            msgReceiversEle.addChild(msgReceiverEle2);

            OMElement parameterEle1 = factory.createOMElement("parameter", "", "");
            parameterEle1.addAttribute("name", EJB3Util.EJB_REMOTE_INTERFACE_NAME, emptyNS);
            parameterEle1.setText(remoteInterface);

//            OMElement parameterEle2 = factory.createOMElement("parameter", "", "");
//            parameterEle2.addAttribute("name", EJB3Util.EJB_HOME_INTERFACE_NAME, emptyNS);
//            parameterEle2.setText(homeInterface);

            OMElement parameterEle3 = factory.createOMElement("parameter", "", "");
            parameterEle3.addAttribute("name", EJB3Util.EJB_JNDI_NAME, emptyNS);
            parameterEle3.setText(beanJNDIName);

            OMElement parameterEle4 = factory.createOMElement("parameter", "", "");
            parameterEle4.addAttribute("name", EJB3Util.EJB_PROVIDER_URL, emptyNS);
            parameterEle4.setText(providerURL);

            OMElement parameterEle5 = factory.createOMElement("parameter", "", "");
            parameterEle5.addAttribute("name", EJB3Util.EJB_INITIAL_CONTEXT_FACTORY, emptyNS);
            parameterEle5.setText(jndiContextClass);

            OMElement parameterEle6 = factory.createOMElement("parameter", "", "");
            parameterEle6.addAttribute("name", EJB3Util.EJB_JNDI_USERNAME, emptyNS);
            parameterEle6.setText(userName);

            OMElement parameterEle7 = factory.createOMElement("parameter", "", "");
            parameterEle7.addAttribute("name", EJB3Util.EJB_JNDI_PASSWORD, emptyNS);
            parameterEle7.setText(password);

            OMElement parameterEle8 = factory.createOMElement("parameter", "", "");
            parameterEle8.addAttribute("name", "ServiceClass", emptyNS);
            parameterEle8.setText(serviceClass);

            OMElement parameterEle9 = factory.createOMElement("parameter", "", "");
            parameterEle9.addAttribute("name", ServerConstants.SERVICE_TYPE, emptyNS);
            parameterEle9.setText(ServerConstants.SERVICE_TYPE_EJB);

            serviceEle.addChild(schemaEle);
            serviceEle.addChild(msgReceiversEle);
            serviceEle.addChild(parameterEle1);
//            serviceEle.addChild(parameterEle2);
            serviceEle.addChild(parameterEle3);
            serviceEle.addChild(parameterEle4);
            serviceEle.addChild(parameterEle5);
            serviceEle.addChild(parameterEle6);
            serviceEle.addChild(parameterEle7);
            serviceEle.addChild(parameterEle8);
            serviceEle.addChild(parameterEle9);

            serviceGroupEle.addChild(serviceEle);
        }
        return serviceGroupEle;
    }

    private EJBAppServerData getEJBAppServerConfiguration(String jnpProviderUrl, String password)
            throws Exception {

        String configPath = EJBConstants.APP_SERVERS;
        Collection configResourceCollection;
        EJBAppServerData appServerData = null;

        try {
            if (!registry.resourceExists(configPath)) {
                return appServerData;
            }
            configResourceCollection = (Collection) registry.get(configPath);
            String[] configResourcePaths = configResourceCollection.getChildren();
            String configResourcePath;
            Resource configResource;

            for (int i = 0; i < configResourceCollection.getChildCount(); i++) {
                configResourcePath = configResourcePaths[i];
                configResource = registry.get(configResourcePath);
                if (jnpProviderUrl.equals(configResource.getProperty(
                        EJBConstants.AppServerProperties.PROVIDER_URL))) {
                    appServerData = new EJBAppServerData();
//                    password = configResource.getProperty(EJBConstants.AppServerProperties.PASSWORD);
                    appServerData.setAppServerType(configResource.getProperty(
                            EJBConstants.AppServerProperties.APP_SERVER_TYPE));
                    appServerData.setProviderURL(configResource.getProperty(
                            EJBConstants.AppServerProperties.PROVIDER_URL));
                    appServerData.setJndiContextClass(configResource.getProperty(
                            EJBConstants.AppServerProperties.JNDI_CONTEXT_CLASS));
                    appServerData.setUserName(configResource.getProperty(
                            EJBConstants.AppServerProperties.USER_NAME));
                }
            }
            return appServerData;
        } catch (RegistryException e) {
            log.error("Unable to get EJB Configuration", e);
            throw e;
        }
    }

    private EJBProviderData getEJBProviderData(Resource configResource) {
        EJBProviderData ejbProviderData = new EJBProviderData();
        ejbProviderData.setProviderURL(configResource.getProperty(
                EJBConstants.ConfigProperties.PROVIDER_URL));
        ejbProviderData.setJndiContextClass(configResource.getProperty(
                EJBConstants.ConfigProperties.JNDI_CONTEXT_CLASS));
        ejbProviderData.setUserName(configResource.getProperty(
                EJBConstants.ConfigProperties.USER_NAME));
        ejbProviderData.setPassword(configResource.getProperty(
                EJBConstants.ConfigProperties.PASSWORD));
        ejbProviderData.setBeanJNDIName(configResource.getProperty(
                EJBConstants.ConfigProperties.BEAN_JNDI_NAME));
//        ejbProviderData.setHomeInterface(configResource.getProperty(
//                EJBConstants.ConfigProperties.HOME_INTERFACE));
        ejbProviderData.setRemoteInterface(configResource.getProperty(
                EJBConstants.ConfigProperties.REMOTE_INTERFACE));
        ejbProviderData.setServiceName(configResource.getProperty(
                EJBConstants.ConfigProperties.SERVICE_NAME));

        return ejbProviderData;
    }

    private String getFilePathFromArchiveId(String archiveId) {
        ConfigurationContext configCtx =
                MessageContext.getCurrentMessageContext().getConfigurationContext();
        Map fileResMap = (Map) configCtx.getProperty(ServerConstants.FILE_RESOURCE_MAP);

        return (String) fileResMap.get(archiveId);
    }
}
