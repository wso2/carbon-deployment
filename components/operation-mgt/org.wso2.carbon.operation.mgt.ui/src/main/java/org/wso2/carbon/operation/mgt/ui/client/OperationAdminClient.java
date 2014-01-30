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
package org.wso2.carbon.operation.mgt.ui.client;

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
import org.wso2.carbon.operation.mgt.stub.OperationAdminStub;
import org.wso2.carbon.operation.mgt.stub.types.OperationMetaData;
import org.wso2.carbon.operation.mgt.stub.types.OperationMetaDataWrapper;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OperationAdminClient {
    private static final Log log = LogFactory.getLog(OperationAdminClient.class);
    private OperationAdminStub stub;

    public OperationAdminClient(String cookie,
                                String backendServerURL,
                                ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "OperationAdmin";
        stub = new OperationAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public OperationMetaDataWrapper listAllOperations(String serviceName) throws AxisFault {
        if (serviceName == null) {
            handleException("Service name cannot be null");
        }
        OperationMetaDataWrapper metaDataWrapper = null;
        try {
            metaDataWrapper = stub.listAllOperations(serviceName);
        } catch (Exception e) {
            handleException("Cannot get operation list", e);
        }
        return metaDataWrapper;
    }

    public OperationMetaData getOperationMetaData(String serviceName,
                                                  String opName) throws AxisFault {
        try {
            return stub.getOperationMetaData(serviceName, opName);
        } catch (Exception e) {
            handleException("Cannot get operation metadata for service: " +
                            serviceName + "operation " + opName, e);
        }
        return null;
    }

    public void configureMTOM(String flag, String serviceName, String operationName)
            throws AxisFault {
        try {
            stub.configureMTOM(flag, serviceName, operationName);
        } catch (Exception e) {
            handleException("Could not change MTOM processing to " + flag + " for service " +
                            serviceName + ", operation=" + operationName, e);
        }
    }

    public List<Parameter> getOperationParameters(String serviceName,
                                                  String operationName) throws AxisFault {
        List<Parameter> parameters = new ArrayList<Parameter>();
        try {
            String[] operationParameters =
                    stub.getDeclaredOperationParameters(serviceName, operationName);
            if (operationParameters != null && operationParameters.length != 0) {
                for (String operationParameter : operationParameters) {
                    XMLStreamReader xmlSR =
                            StAXUtils.createXMLStreamReader(
                                    new ByteArrayInputStream(operationParameter.getBytes()));
                    OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
                    String paramName = paramEle.getAttribute(new QName("name")).getAttributeValue();
                    InputStream xmlIn = new ByteArrayInputStream(operationParameter.getBytes());
                    XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn, null);
                    Parameter parameter = new Parameter(paramName,
                                                        xmlPrettyPrinter.xmlFormat());
                    boolean isLocked = false;
                    OMAttribute lockedAttrib = paramEle.getAttribute(new QName("locked"));
                    if (lockedAttrib != null) {
                        isLocked = "true".equals(lockedAttrib.getAttributeValue());
                    }
                    parameter.setLocked(isLocked);
                    parameters.add(parameter);
                }
            }
        } catch (Exception e) {
            handleException("Could not get operation parameters for service: " + serviceName +
                            " & operation:" + operationName, e);
        }
        return parameters;
    }

    public void removeOperationParameter(String serviceName,
                                         String operation,
                                         String parameterName) throws AxisFault {
        try {
            stub.removeOperationParameter(serviceName, operation, parameterName);
        } catch (RemoteException e) {
            handleException("Could not remove operation parameter. Service: " + serviceName +
                            ", operation: " + operation + ", parameterName=" + parameterName, e);
        }
    }

    public void setOperationParameters(String serviceName,
                                       String operation,
                                       List<String> parameters) throws AxisFault {
        try {
            stub.setOperationParameters(serviceName, operation,
                                        parameters.toArray(new String[parameters.size()]));
        } catch (RemoteException e) {
            handleException("Cannot add operation parameters", e);
        }
    }

    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
