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
package org.wso2.carbon.springservices.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.springframework.context.ApplicationContext;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.springservices.ui.fileupload.ServiceUploaderClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Locale;


public class SpringServiceMaker {

    private static final Log log = LogFactory.getLog(SpringServiceMaker.class);

    public static final String FILE_RESOURCE_MAP = "file.resource.map";

    private ConfigurationContext configContext = null;

    private ResourceBundle bundle;

    private static final String BUNDLE = "org.wso2.carbon.springservices.ui.i18n.Resources";

    private static final String SPRING_CONTEXT_SUPPLIER = "org.wso2.carbon.springservices.GenericApplicationContextSupplier";

    public SpringServiceMaker(ConfigurationContext context, Locale locale) {
        configContext = context;
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    public SpringBeansData getSpringBeanNames(String springContextId, String springBeanId,
                                              ClassLoader bundleClassLoader) throws AxisFault {
        // Manipulation of springContext to ${RepositoryLocation}/spring

        String springContextFilePath = getFilePathFromArchiveId(springContextId);
        String springBeanFilePath = getFilePathFromArchiveId(springBeanId);
        SpringBeansData data = new SpringBeansData();
        data.setSpringContext(springContextId);

        File urlFile = new File(springBeanFilePath);

        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
        ClassLoader urlCl;
        try {
            URL url = urlFile.toURL();
            urlCl = URLClassLoader.newInstance(new URL[]{url}, bundleClassLoader);

            // Save the class loader so that you can restore it later
            Thread.currentThread()
                    .setContextClassLoader(urlCl);

            ApplicationContext aCtx = GenericApplicationContextUtil
                    .getSpringApplicationContext(springContextFilePath,
                            springBeanFilePath);
            String[] beanDefintions = aCtx.getBeanDefinitionNames();
            data.setBeans(beanDefintions);
        } catch (Exception e) {
            String msg = bundle.getString("spring.cannot.load.spring.beans");
            handleException(msg, e);
        } finally {
            Thread.currentThread().setContextClassLoader(prevCl);
        }
        return data;
    }


    public void createAndUploadSpringBean(String serviceHierarchy,
                                          String springContextId,
                                          ServiceUploaderClient client,
                                          String springBeanId,
                                          String[] beanClasses) throws Exception {
        String filePathFromArchiveId = getFilePathFromArchiveId(springBeanId);
        String filePathForSpringContext = getFilePathFromArchiveId(springContextId);

        Map fileResMap = (Map) configContext
                .getProperty(FILE_RESOURCE_MAP);
        fileResMap.remove(springContextId);

        if (filePathFromArchiveId == null) {
            String msg = bundle.getString("spring.non.existent.file");
            log.warn(msg);
            throw new AxisFault(msg);
        }

        int endIndex = filePathFromArchiveId.lastIndexOf(File.separator);
        String filePath = filePathFromArchiveId.substring(0, endIndex);
        String archiveFileName = filePathFromArchiveId.substring(endIndex);
        archiveFileName = archiveFileName.substring(1, archiveFileName
                .lastIndexOf("."));

        ArchiveManipulator archiveManipulator = new ArchiveManipulator();

        // ----------------- Unzip the file ------------------------------------
        String unzippeDir = filePath + File.separator + "springTemp";
        File unzipped = new File(unzippeDir);
        if (!unzipped.mkdirs()) {
            log.error("Error while creating directories..");
        }

        try {
            archiveManipulator.extract(filePathFromArchiveId, unzippeDir);
        } catch (IOException e) {
            String msg = bundle.getString("spring.cannot.extract.archive");
            handleException(msg, e);
        }

        // TODO copy the spring xml
        String springContextRelLocation = "spring/context.xml";
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            File springContextRelDir = new File(unzippeDir + File.separator
                                                + "spring");
            if (!springContextRelDir.mkdirs()) {
                log.error("Error while creating directories..");
            }
            File absFile = new File(springContextRelDir, "context.xml");
            if (!absFile.exists() && !absFile.createNewFile()) {
                log.error("Error while creating file..");
            }
            File file = new File(filePathForSpringContext);
            in = new FileInputStream(file);
            out = new FileOutputStream(absFile);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

        } catch (FileNotFoundException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }

        // ---- Generate the services.xml and place it in META-INF -----
        File file = new File(unzippeDir + File.separator + "META-INF"
                + File.separator + "services.xml");
        if (!file.mkdirs()) {
            log.error("Error while creating directories..");
        }

        try {
            File absoluteFile = file.getAbsoluteFile();

            if (absoluteFile.exists() && !absoluteFile.delete()) {
                log.error("Error while deleting file..");
            }

            if (!absoluteFile.createNewFile()) {
                log.error("Error while creating file..");
            }

            OutputStream os = new FileOutputStream(file);
            OMElement servicesXML = createServicesXMLFromSpringBeans(
                    beanClasses, springContextRelLocation);
            servicesXML.build();
            servicesXML.serialize(os);
        } catch (Exception e) {
            String msg = bundle.getString("spring.cannot.write.services.xml");
            handleException(msg, e);
        }

        // ----------------- Create the AAR ------------------------------------
        // These are the files to include in the ZIP file
        String outAARFilename = filePath + File.separator + archiveFileName
                + ".aar";

        try {
            archiveManipulator.archiveDir(outAARFilename, unzipped.getPath());
        } catch (IOException e) {
            String msg = bundle.getString("springcannot.create.new.aar.archive");
            handleException(msg, e);
        }

        File fileToUpload = new File(outAARFilename);

        FileDataSource fileDataSource = new FileDataSource(fileToUpload);
        DataHandler dataHandler = new DataHandler(fileDataSource);

        try {
            client.uploadService(archiveFileName + ".aar", serviceHierarchy, dataHandler);
        } catch (Exception e) {
            String msg = bundle.getString("spring.unable.to.upload");
            handleException(msg, e);
        }

    }


    private OMElement createServicesXMLFromSpringBeans(String[] springBeans,
                                                       String springContextLocation) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace emptyNS = factory.createOMNamespace("", "");
        OMElement serviceGroupEle = factory.createOMElement("serviceGroup", "",
                "");

        for (int i = 0; i < springBeans.length; i++) {
            String serviceBeanName = springBeans[i];

            OMElement serviceEle = factory.createOMElement("service", "", "");

            OMElement schemaEle = factory.createOMElement("schema", "", "");
            schemaEle.addAttribute(factory.createOMAttribute(
                    "elementFormDefaultQualified", emptyNS, "false"));

            serviceEle.addAttribute(factory.createOMAttribute("name", emptyNS,
                    serviceBeanName));

            OMElement msgReceiversEle = factory.createOMElement(
                    "messageReceivers", "", "");
            OMElement msgReceiverEle1 = factory.createOMElement(
                    "messageReceiver", "", "");
            msgReceiverEle1.addAttribute("mep",
                    "http://www.w3.org/ns/wsdl/in-only", emptyNS);
            msgReceiverEle1.addAttribute("class",
                    RPCInOnlyMessageReceiver.class.getName(), emptyNS);

            OMElement msgReceiverEle2 = factory.createOMElement(
                    "messageReceiver", "", "");
            msgReceiverEle2.addAttribute("mep",
                    "http://www.w3.org/ns/wsdl/in-out", emptyNS);
            msgReceiverEle2.addAttribute("class", RPCMessageReceiver.class
                    .getName(), emptyNS);
            msgReceiversEle.addChild(msgReceiverEle1);
            msgReceiversEle.addChild(msgReceiverEle2);

            OMElement parameterEleServiceObjectSupplier = factory
                    .createOMElement("parameter", "", "");
            parameterEleServiceObjectSupplier.addAttribute("locked", "true",
                    emptyNS);
            parameterEleServiceObjectSupplier.addAttribute("name",
                    "ServiceObjectSupplier", emptyNS);
            parameterEleServiceObjectSupplier
                    .setText(SpringServiceMaker.SPRING_CONTEXT_SUPPLIER);

            OMElement parameterEleSpringBeanName = factory.createOMElement(
                    "parameter", "", "");
            parameterEleSpringBeanName.addAttribute("locked", "true", emptyNS);
            parameterEleSpringBeanName.addAttribute("name", "SpringBeanName",
                    emptyNS);
            parameterEleSpringBeanName.setText(serviceBeanName);

            OMElement parameterEleSpringContextLocation = factory
                    .createOMElement("parameter", "", "");
            parameterEleSpringContextLocation.addAttribute("locked", "true",
                    emptyNS);
            parameterEleSpringContextLocation.addAttribute("name",
                    "SpringContextLocation", emptyNS);
            parameterEleSpringContextLocation.setText(springContextLocation);

            OMElement paramEleserviceType = factory.createOMElement("parameter", "", "");
            paramEleserviceType.addAttribute("name", "serviceType", emptyNS);
            paramEleserviceType.setText("spring");

            serviceEle.addChild(schemaEle);
            serviceEle.addChild(msgReceiversEle);
            serviceEle.addChild(parameterEleServiceObjectSupplier);
            serviceEle.addChild(parameterEleSpringBeanName);
            serviceEle.addChild(parameterEleSpringContextLocation);
            serviceEle.addChild(paramEleserviceType);
            serviceGroupEle.addChild(serviceEle);
        }
        return serviceGroupEle;
    }

    private String getFilePathFromArchiveId(String archiveId) {
        Map fileResMap = (Map) configContext
                .getProperty(FILE_RESOURCE_MAP);

        return (String) fileResMap.get(archiveId);
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
