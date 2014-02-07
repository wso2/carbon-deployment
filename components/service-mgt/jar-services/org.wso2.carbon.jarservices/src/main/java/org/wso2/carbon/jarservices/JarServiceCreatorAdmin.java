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
package org.wso2.carbon.jarservices;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JarServiceCreatorAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(JarServiceCreatorAdmin.class);
    public static BundleContext bundleContext;

    public UploadArtifactsResponse upload(String serviceGroupName,
                                          Resource wsdl,
                                          Resource[] resources)
            throws DuplicateServiceGroupException, JarUploadException {

        AxisConfiguration axisConfig = getAxisConfig();

        // First lets filter for jar resources
        AxisServiceGroup serviceGroup = axisConfig.getServiceGroup(serviceGroupName);
        if (serviceGroup != null) {
            String msg = "Service group " + serviceGroupName + " already exists";
            log.error(msg);
            throw new DuplicateServiceGroupException(msg);
        }


        String repo = axisConfig.getRepository().getPath();

        if (CarbonUtils.isURL(repo)) {
            throw new JarUploadException("Uploading services to URL repo is not supported ");
        }
        String tempDir = getTempDir();
        String libDir = tempDir + File.separator + "lib";
        new File(libDir).mkdirs();
        List<Service> services = new ArrayList<Service>();
        try {
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                writeToFileSystem(libDir,
                                  resource.getFileName(),
                                  resource.getDataHandler());
                services.addAll(getClasses(libDir + File.separator + resource.getFileName()));
            }
            if(wsdl != null){
                String metaInfDir = tempDir + File.separator + "META-INF";
                new File(metaInfDir).mkdirs();
                writeToFileSystem(metaInfDir,
                                  wsdl.getFileName(),
                                  wsdl.getDataHandler());
            }
        } catch (IOException e) {
            String msg = "Error occured while uploading jar service ";
            log.error(msg, e);
            throw new JarUploadException(msg, e);
        }
        UploadArtifactsResponse response = new UploadArtifactsResponse();
        response.setResourcesDirPath(tempDir);
        response.setWsdlProvided(wsdl != null);
        Collections.sort(services, new Comparator<Service>() {
            public int compare(Service o1, Service o2) {
                return o1.getClassName().compareTo(o2.getClassName());
            }
        });
        response.setServices(services.toArray(new Service[services.size()]));
        return response;
    }

    private String getTempDir() {
        String workDir =
                (String) MessageContext.getCurrentMessageContext().getProperty(ServerConstants.WORK_DIR);
        String tempDir = workDir + File.separator + (System.currentTimeMillis() + Math.random());
        return tempDir;
    }

    /**
     * Get all the fully qualified class names of all the classes in this
     * archive
     *
     * @param filePath the File path of the jar file
     * @return all the fully qualified class names of all the classes in this
     *         archive
     * @throws AxisFault If an error occurs while reading the artifact
     */
    private List<Service> getClasses(String filePath) throws AxisFault {
        List<Service> services = new ArrayList<Service>();
        if (filePath != null) {
            try {
                String[] entries = new ArchiveManipulator().check(filePath);
                for (int i = 0; i < entries.length; i++) {
                    String entry = entries[i];
                    if (entry.endsWith(".class")) {
                        entry = entry.replace('/', '.').substring(0,
                                                                  entry.lastIndexOf(".class"));
                        Service service = new Service();
                        service.setClassName(entry);

                        AxisConfiguration axisConfig = getAxisConfig();
                        String serviceName = entry.substring(entry.lastIndexOf(".") + 1);
                        String newServiceName = serviceName;
                        int x = 1;
                        while (axisConfig.getService(newServiceName) != null) {
                            newServiceName = serviceName + x;
                            x++;
                        }
                        service.setServiceName(newServiceName);
                        services.add(service);
                    }
                }
            } catch (IOException e) {
                String msg = "Could not read archive";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }
        return services;
    }

    /**
     * This method will list all the public methods for the given classes.
     *
     * @param directoryPath Temp dir innto which all jars were uploaded
     * @param services      Selected classes
     * @return All methods in the selected classes
     * @throws AxisFault Error
     * @throws DuplicateServiceException If  a service  with a given name already exists
     */
    public Service[] getClassMethods(String directoryPath,
                                     Service[] services) throws AxisFault,
                                                                DuplicateServiceException {
        if (services == null || services.length == 0) {
            String msg = "Cannot find services";
            log.error(msg);
            throw new AxisFault(msg);
        }
        List<String> methodExcludeList = new ArrayList<String>();
        methodExcludeList.add("hashCode");
        methodExcludeList.add("getClass");
        methodExcludeList.add("equals");
        methodExcludeList.add("notify");
        methodExcludeList.add("notifyAll");
        methodExcludeList.add("toString");
        methodExcludeList.add("wait");
        List<URL> resourcesList = new ArrayList<URL>();

        File[] files = new File(directoryPath + File.separator + "lib").listFiles();
        for (File file : files) {
            try {
                resourcesList.add(file.toURI().toURL());
            } catch (MalformedURLException ignored) {
                // This exception will not occur
            }
        }

        AxisConfiguration axisConfig = getAxisConfig();
        URL[] urls = resourcesList.toArray(new URL[resourcesList.size()]);
        ClassLoader classLoader =
                URLClassLoader.newInstance(urls, axisConfig.getServiceClassLoader());

        for (int i = 0; i < services.length; i++) {
            Service service = services[i];
            String className = service.getClassName();
            if(axisConfig.getService(service.getServiceName()) != null){
                String msg = "Axis service " + service.getServiceName() + " already exists";
                log.warn(msg);
                throw new DuplicateServiceException(msg);
            }
            try {
                Class clazz = classLoader.loadClass(className);
                java.lang.reflect.Method[] methods = clazz.getMethods();
                List<Operation> operationList = new ArrayList<Operation>();
                for (int j = 0; j < methods.length; j++) {
                    java.lang.reflect.Method method = methods[j];
                    String methodName = method.getName();
                    int modifiers = method.getModifiers();
                    if (Modifier.isPublic(modifiers)
                        && !methodExcludeList.contains(methodName)) {
                        Operation operation = new Operation();
                        operation.setOperationName(methodName);
                        operationList.add(operation);
                    }
                }
                findOverloadedMethods(operationList);
                service.setOperations(operationList.toArray(new Operation[operationList.size()]));
            } catch (ClassNotFoundException e) {
                String msg = "The class " + className + " cannot be loaded";
                log.error(msg);
            }
        }
        return services;
    }

    private void findOverloadedMethods(List methodList) {
        Map finalMap = new HashMap();
        int size = methodList.size();
        for (int k = 0; k < size - 1; k++) {
            Operation startPointer = (Operation) methodList.get(k);
            if (!finalMap.containsKey(startPointer.getOperationName())) {
                int count = 0;
                for (Iterator iterator = methodList.iterator(); iterator
                        .hasNext();) {
                    Operation operation = (Operation) iterator.next();
                    if (operation.getOperationName().equals(
                            startPointer.getOperationName())) {
                        count++;
                    }
                }
                for (Iterator iterator = methodList.iterator(); iterator
                        .hasNext();) {
                    Operation operation = (Operation) iterator.next();
                    if (operation.getOperationName().equals(
                            startPointer.getOperationName())) {
                        if (count > 1) {
                            operation.setOverloaded(true);
                        }
                    }
                }
            }
        }

    }

    /**
     * Creates and deploys a service. This AAR will contain all the classe from
     * the jar file corresponding to <code>archiveId</code>. In addition,
     * a services.xml will be created, and all of the
     * <code>serviceClasses</code> will be added as services.
     *
     * @param directoryPath    archive id
     * @param serviceHierarchy  hierarchical part of the service
     * @param serviceGroupName the serviceGroupName
     * @param data             info array. data contains the excluded method names.
     * @throws AxisFault                      will be thrown
     * @throws DuplicateServiceException      If a service which already exists is trying to be created
     * @throws DuplicateServiceGroupException If the sepcified service group already exists
     */
    public void createAndDeployService(String directoryPath,
                                       String serviceHierarchy,
                                       String serviceGroupName,
                                       Service[] data) throws AxisFault,
                                                              DuplicateServiceException,
                                                              DuplicateServiceGroupException {
        AxisConfiguration axisConfig = getAxisConfig();
        if (serviceGroupName == null || serviceGroupName.trim().length() == 0) {
            serviceGroupName = String.valueOf(System.currentTimeMillis() + Math.random());
        } else {
            if (axisConfig.getServiceGroup(serviceGroupName) != null) {
                String msg = "Service group " + serviceGroupName + " already exists";
                log.error(msg);
                throw new DuplicateServiceGroupException(msg);
            }
        }

        // ---- Generate the services.xml and place it in META-INF -----
        File file = new File(directoryPath + File.separator + "META-INF" + File.separator +
                             "services.xml");
        file.mkdirs();

        try {
            File absoluteFile = file.getAbsoluteFile();

            if (absoluteFile.exists()) {
                absoluteFile.delete();
            }

            absoluteFile.createNewFile();

            OutputStream os = new FileOutputStream(file);
            OMElement servicesXML = createServicesXML(data);
            servicesXML.build();
            servicesXML.serialize(os);
        } catch (DuplicateServiceException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Cannot write services XML";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        // ----------------- Create the AAR ------------------------------------
        String tempDir = getTempDir();
        new File(tempDir).mkdirs();
        String outAARFilename = tempDir + File.separator + serviceGroupName + ".aar";

        try {
            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            archiveManipulator.archiveDir(outAARFilename, directoryPath);
        } catch (IOException e) {
            String msg = "Cannot create new AAR archive";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        String servicesDir = axisConfig.getParameter(DeploymentConstants.SERVICE_DIR_PATH)
                .getValue().toString();

        // ------------- Copy the AAR to the repository/services directory
        String destDir = axisConfig.getRepository().getPath() + File.separator + servicesDir;

        // create the hierarchical folders before deploying
        if (serviceHierarchy != null) {
            String[] hierarchyParts = serviceHierarchy.split("/");
            for (String part : hierarchyParts) {
                destDir += File.separator + part;
                File hierarchyFolder = new File(destDir);
                if (!hierarchyFolder.exists()) {
                    hierarchyFolder.mkdir();
                }
            }
        }

        try {
            String fileName = serviceGroupName + ".aar";
            File aarInRepo = new File(destDir + File.separator + fileName);
            FileManipulator.copyFile(new File(outAARFilename), aarInRepo);
        } catch (Exception e) {
            String msg = "Cannot copy AAR file to Repo";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private String prepareHierarchy(String serviceHierarchy) {
        if (serviceHierarchy.startsWith("/")) {
            serviceHierarchy = serviceHierarchy.substring(1);
        }
        if (serviceHierarchy.endsWith("/")) {
            serviceHierarchy = serviceHierarchy.substring(0, serviceHierarchy.length() - 1);
        }
        return serviceHierarchy;
    }

    private OMElement createServicesXML(Service[] services) throws DuplicateServiceException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace emptyNS = factory.createOMNamespace("", "");
        OMElement serviceGroupEle = factory.createOMElement("serviceGroup", "", "");

        for (int i = 0; i < services.length; i++) {
            Service service = services[i];
            String serviceClass = service.getClassName();
            String serviceName = service.getServiceName();
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                if (axisConfig.getService(serviceName) != null) {
                    String msg = "An Axis2 service  with name " + serviceName + " already exists";
                    log.warn(msg);
                    throw new DuplicateServiceException(msg);
                }
            } catch (AxisFault ignored) {
            }

            OMElement serviceEle = factory.createOMElement("service", "", "");

            OMElement schemaEle = factory.createOMElement("schema", "", "");
            schemaEle.addAttribute(factory.createOMAttribute(
                    "elementFormDefaultQualified", emptyNS, "true"));

            serviceEle.addAttribute(factory.createOMAttribute("name", emptyNS, serviceName));
            serviceEle.addAttribute(factory.createOMAttribute("scope", emptyNS,
                                                              service.getDeploymentScope()));

            OMElement msgReceiversEle = factory.createOMElement(
                    "messageReceivers", "", "");
            OMElement msgReceiverEle1 = factory.createOMElement(
                    "messageReceiver", "", "");
            msgReceiverEle1.addAttribute("mep",
                                         "http://www.w3.org/ns/wsdl/in-only", emptyNS);
            msgReceiverEle1.addAttribute("class",
                                         "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver",
                                         emptyNS);

            OMElement msgReceiverEle2 = factory.createOMElement(
                    "messageReceiver", "", "");
            msgReceiverEle2.addAttribute("mep",
                                         "http://www.w3.org/ns/wsdl/in-out", emptyNS);
            msgReceiverEle2.addAttribute("class",
                                         "org.apache.axis2.rpc.receivers.RPCMessageReceiver",
                                         emptyNS);
            msgReceiversEle.addChild(msgReceiverEle1);
            msgReceiversEle.addChild(msgReceiverEle2);

            OMElement parameterEle = factory.createOMElement("parameter", "", "");
            parameterEle.addAttribute("name", "ServiceClass", emptyNS);
            parameterEle.setText(serviceClass);

            OMElement serviceTypeParamEle = factory.createOMElement("parameter", "", "");
            serviceTypeParamEle.addAttribute("name", "serviceType", emptyNS);
            serviceTypeParamEle.setText("jarservice");

            if (service.isUseOriginalWsdl()) {
                OMElement useOriginalWsdlParamEle = factory.createOMElement("parameter", "", "");
                useOriginalWsdlParamEle.addAttribute("name", "useOriginalwsdl", emptyNS);
                useOriginalWsdlParamEle.setText("true");
                serviceEle.addChild(useOriginalWsdlParamEle);

                OMElement modifyUserWSDLPortAddressParamEle = factory.createOMElement("parameter", "", "");
                modifyUserWSDLPortAddressParamEle.addAttribute("name", "modifyUserWSDLPortAddress", emptyNS);
                modifyUserWSDLPortAddressParamEle.setText("true");
                serviceEle.addChild(modifyUserWSDLPortAddressParamEle);
            }

            serviceEle.addChild(schemaEle);
            serviceEle.addChild(msgReceiversEle);
            serviceEle.addChild(parameterEle);
            serviceEle.addChild(serviceTypeParamEle);

            // Operations
            Operation[] operation = service.getOperations();
            if (operation != null) {
                if (operation.length > 0) {
                    OMElement excludesEle = factory.createOMElement(
                            "excludeOperations", "", "");
                    serviceEle.addChild(excludesEle);
                    List<String> operationDuplicationList = new ArrayList<String>();
                    for (int j = 0; j < operation.length; j++) {
                        String methodName = operation[j].getOperationName();
                        if (!operationDuplicationList.contains(methodName)) {
                            OMElement operationEle =
                                    factory.createOMElement("operation", "", "");
                            operationDuplicationList.add(methodName);
                            operationEle.setText(methodName);
                            excludesEle.addChild(operationEle);
                        }
                    }
                }
            }
            serviceGroupEle.addChild(serviceEle);
        }

        return serviceGroupEle;
    }

    private void writeToFileSystem(String path, String fileName, DataHandler dataHandler)
            throws IOException {
        File destFile = new File(path, fileName);
        FileOutputStream fos = new FileOutputStream(destFile);
        dataHandler.writeTo(fos);
        fos.flush();
        fos.close();
    }

}
