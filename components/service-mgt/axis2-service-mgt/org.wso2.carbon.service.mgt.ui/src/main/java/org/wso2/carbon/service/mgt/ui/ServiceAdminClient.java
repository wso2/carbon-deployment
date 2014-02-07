/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.service.mgt.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyServicesWrapper;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceDownloadData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;
import java.text.MessageFormat;

/**
 *
 */
public class ServiceAdminClient {
    private static final Log log = LogFactory.getLog(ServiceAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.service.mgt.ui.i18n.Resources";
    private ResourceBundle bundle;
    private ServiceAdminStub stub;

    public ServiceAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx,
                              Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ServiceAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ServiceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public ServiceMetaData getServiceData(String serviceName) throws AxisFault {
        try {
            return stub.getServiceData(serviceName);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }

    public ServiceMetaDataWrapper getAllServices(String serviceTypeFilter,
                                                 String serviceSearchString,
                                                 int pageNumber) throws RemoteException {
        try {
            return stub.listServices(serviceTypeFilter, serviceSearchString, pageNumber);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.all.services"), e);
        }
        return null;
    }

    public FaultyServicesWrapper getAllFaultyServices(int pageNumber) throws RemoteException {
        try {
            return stub.getFaultyServiceArchives(pageNumber);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.faulty.services"), e);
        }
        return null;
    }

    public int getNoOfFaultyServices() throws java.lang.Exception {
        try {
            return stub.getNumberOfFaultyServices();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.the.number.of.faulty.services"), e);
        }
        return 0;
    }

    public List<Parameter> getServiceParameters(String serviceName) throws AxisFault {
        List<Parameter> parameters = new ArrayList<Parameter>();
        try {
            String[] serviceParameters = stub.getServiceParameters(serviceName);
            if (serviceParameters != null && serviceParameters.length != 0) {
                for (String serviceParameter : serviceParameters) {
                    XMLStreamReader xmlSR =
                            StAXUtils.createXMLStreamReader(
                                    new ByteArrayInputStream(serviceParameter.getBytes()));
                    OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
                    String paramName = paramEle.getAttribute(new QName("name")).getAttributeValue();
                    InputStream xmlIn = new ByteArrayInputStream(serviceParameter.getBytes());
                    XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn, null);
                    Parameter parameter = new Parameter(paramName,
                                                        xmlPrettyPrinter.xmlFormat());
                    boolean isLocked = false;
                    OMAttribute lockedAttrib = paramEle.getAttribute(new QName("locked"));
                    if(lockedAttrib != null) {
                        isLocked = "true".equals(lockedAttrib.getAttributeValue());
                    }
                    parameter.setLocked(isLocked);
                    parameters.add(parameter);
                }
            }
        } catch (Exception e) {
            handleException(
                    MessageFormat.format(bundle.getString("could.not.get.service.parameters"),
                                         serviceName), e);
        }
        return parameters;
    }

    public void setServiceParameters(String serviceName, List<String> parameters) throws AxisFault {
        try {
            stub.setServiceParameters(serviceName,
                                      parameters.toArray(new String[parameters.size()]));
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.update.service.parameters"), e);
        }
    }

    public void removeServiceParameter(String serviceName,
                                       String parameterName) throws AxisFault {
        try {
            stub.removeServiceParameter(serviceName, parameterName);
        } catch (RemoteException e) {
            handleException("Could not remove Service parameter. Service: " + serviceName +
                    ", parameterName=" + parameterName, e);
        }
    }

    public boolean checkForGroupedServices(String[] serviceGroupsList) throws AxisFault {
        boolean foundGroupedServices = false;
        try {
            foundGroupedServices = stub.checkForGroupedServices(serviceGroupsList);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.check.grouped.services"), e);
        }
        return foundGroupedServices;
    }

    public void deleteServiceGroups(String[] serviceGroups) throws java.lang.Exception {
        try {
            stub.deleteServiceGroups(serviceGroups);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.delete.service.groups"), e);
        }
    }

    public void deleteAllNonAdminServiceGroups() throws RemoteException {
        try {
            stub.deleteAllNonAdminServiceGroups();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.delete.all.non.admin.service.groups"), e);
        }
    }

    public void deleteAllFaultyServiceGroups() throws RemoteException {
        try {
            stub.deleteAllFaultyServiceGroups();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.delete.all.faulty.service.groups"), e);
        }
    }

    public void deleteFaultyServiceGroups(String[] fileNames) throws RemoteException {
        try {
            stub.deleteFaultyServiceGroups(fileNames);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.delete.faulty.service.group"), e);
        }
    }

    public void changeServiceState(String serviceName, boolean isActive) throws RemoteException {
        try {
            stub.changeServiceState(serviceName, isActive);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.delete.faulty.service.group"), e);
        }
    }

    public OMElement getPolicy(String serviceName) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(stub.getPolicy(serviceName).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            handleException(bundle.getString("cannot.get.service.policy"), e);
        }
        return null;
    }

    public OMElement getOperationPolicy(String serviceName, String operationName) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(stub.getOperationPolicy(serviceName,
                                                                             operationName).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            handleException(bundle.getString("cannot.get.operation.policy"), e);
        }
        return null;
    }

    public OMElement getOperationMessagePolicy(String serviceName, String operationName,
                                               String messageType) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(stub.getOperationMessagePolicy(serviceName,
                                                                                    operationName,
                                                                                    messageType).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            handleException(bundle.getString("cannot.get.operation.message.policy"), e);
        }
        return null;
    }

    public OMElement getBindingPolicy(String serviceName, String bindingName) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(
                                    stub.getBindingPolicy(serviceName, bindingName).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            String msg = bundle.getString("cannot.get.binding.policy");
            handleException(msg, e);
        }
        return null;
    }

    public OMElement getBindingOperationPolicy(String serviceName, String bindingName,
                                               String operationName) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(
                                    stub.getBindingOperationPolicy(serviceName, bindingName,
                                                                   operationName).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            String msg = bundle.getString("cannot.get.binding.operation.policy");
            handleException(msg, e);
        }
        return null;
    }

    public OMElement getBindingOperationMessagePolicy(String serviceName, String bindingName,
                                                      String operationName, String messageType)
            throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(
                                    stub.getBindingOperationMessagePolicy(serviceName, bindingName,
                                                                          operationName,
                                                                          messageType).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            String msg = bundle.getString("cannot.get.binding.operation.message.policy");
            handleException(msg, e);
        }
        return null;
    }

    public void setPolicy(String serviceName, String policyString) throws AxisFault {
        try {
            stub.setPolicy(serviceName, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.policy.to.axis.service");
            handleException(msg, e);
        }
    }

    public void setServicePolicy(String serviceName, String policyString) throws AxisFault {
        try {
            stub.setServicePolicy(serviceName, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.policy.to.axis.service");
            handleException(msg, e);
        }
    }

    public void setServiceOperationPolicy(String serviceName, String operationName,
                                          String policyString) throws AxisFault {
        try {
            stub.setServiceOperationPolicy(serviceName, operationName, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.service.operation.policy");
            handleException(msg, e);
        }
    }

    public void setServiceOperationMessagePolicy(String serviceName, String operationName,
                                                 String messageType,
                                                 String policyString) throws AxisFault {
        try {
            stub.setServiceOperationMessagePolicy(serviceName, operationName, messageType,
                                                  policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.service.operation.message.policy");
            handleException(msg, e);
        }
    }

    public void setBindingPolicy(String serviceName, String bindingName,
                                 String policyString) throws AxisFault {
        try {
            stub.setBindingPolicy(serviceName, bindingName, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.binding.policy");
            handleException(msg, e);
        }
    }

    public void setBindingOperationPolicy(String serviceName, String bindingName,
                                          String operationName,
                                          String policyString) throws AxisFault {
        try {
            stub.setBindingOperationPolicy(serviceName, bindingName, operationName, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.binding.operation.policy");
            handleException(msg, e);
        }
    }

    public void setBindingOperationMessagePolicy(String serviceName, String bindingName,
                                                 String operationName, String messageType,
                                                 String policyString) throws AxisFault {
        try {
            stub.setBindingOperationMessagePolicy(serviceName, bindingName, operationName,
                                                  messageType, policyString);
        } catch (Exception e) {
            String msg =
                    bundle.getString("cannot.set.binding.operation.message.policy");
            handleException(msg, e);
        }
    }

    public void setModulePolicy(String moduleName, String moduleVersion, String policyString)
            throws AxisFault {
        try {
            stub.setModulePolicy(moduleName, moduleVersion, policyString);
        } catch (Exception e) {
            String msg = bundle.getString("can.not.set.module.policy");
            handleException(msg, e);
        }
    }

    public OMElement getModulePolicy(String moduleName, String moduleVersion) throws AxisFault {
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(
                            new ByteArrayInputStream(
                                    stub.getModulePolicy(moduleName, moduleVersion).getBytes()));
            return new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (Exception e) {
            String msg = bundle.getString("can.not.get.module.policy");
            handleException(msg, e);
        }
        return null;
    }

    public String[] getServiceBindings(String serviceName) throws AxisFault {
        try {
            return stub.getServiceBindings(serviceName);
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.get.service.binding");
            handleException(msg, e);
        }
        return null;
    }

    public void configureMTOM(String mtomState, String serviceName) throws AxisFault {
        try {
            stub.configureMTOM(mtomState, serviceName);
        } catch (Exception e) {
            String msg = MessageFormat
                    .format(bundle.getString("cannot.change.mtom.state.of.service."), serviceName);
            handleException(msg, e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void downloadServiceArchive(String serviceGroupName, String serviceType, HttpServletResponse response) throws AxisFault{
        try {
            ServletOutputStream out = response.getOutputStream();
            ServiceDownloadData downloadData = stub.downloadServiceArchive(serviceGroupName);
            if (downloadData != null) {
                DataHandler handler = downloadData.getServiceFileData();
                response.setHeader("Content-Disposition", "fileName=" + downloadData.getFileName());
                response.setContentType(handler.getContentType());
                InputStream in = handler.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested service archive was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            handleException("error.downloading.service", e);
        } catch (IOException e) {
            handleException("error.downloading.service", e);
        }
    }


}
