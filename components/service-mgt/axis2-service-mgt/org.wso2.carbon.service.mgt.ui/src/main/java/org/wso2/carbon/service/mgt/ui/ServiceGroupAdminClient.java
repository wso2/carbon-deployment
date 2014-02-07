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
import org.wso2.carbon.service.mgt.stub.ServiceGroupAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaDataWrapper;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.Exception;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

public class ServiceGroupAdminClient {

    private static final Log log = LogFactory.getLog(ServiceGroupAdminClient.class);
    private ServiceGroupAdminStub stub;

    private static final String BUNDLE = "org.wso2.carbon.service.mgt.ui.i18n.Resources";
    private ResourceBundle bundle;

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @param locale
     * @throws AxisFault
     */
    public ServiceGroupAdminClient(String cookie,
                                   String backendServerURL,
                                   ConfigurationContext configCtx,
                                   Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ServiceGroupAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        stub = new ServiceGroupAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public ServiceGroupMetaDataWrapper getAllServiceGroups(String serviceTypeFilter,
                                                           String serviceGroupSearchString,
                                                           int pageNumber) throws RemoteException {
        try {
            return stub.listServiceGroups(serviceTypeFilter, serviceGroupSearchString, pageNumber);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.all.services"), e);
        }
        return null;
    }

    /**
     * @param serviceGroupName
     * @return
     * @throws AxisFault
     */
    public List<Parameter> getServiceGroupParameters(String serviceGroupName) throws AxisFault {
        try {

            List<Parameter> parameters = new ArrayList<Parameter>();
            try {
                String[] groupParameters = stub.getServiceGroupParameters(serviceGroupName);
                if (groupParameters != null && groupParameters.length != 0) {
                    for (String groupParameter : groupParameters) {
                        if (groupParameter != null) {
                            XMLStreamReader xmlSR = StAXUtils
                                    .createXMLStreamReader(new ByteArrayInputStream(groupParameter
                                            .getBytes()));
                            OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
                            String paramName = paramEle.getAttribute(new QName("name"))
                                    .getAttributeValue();
                            InputStream xmlIn = new ByteArrayInputStream(groupParameter.getBytes());
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
                }
            } catch (Exception e) {
                handleException("Could not get parameters for service group: " + serviceGroupName,
                                e);
            }
            return parameters;

        } catch (java.lang.Exception e) {
            String msg = "Cannot get service group parameters.  Error is "+ e.getMessage();
            handleException(msg, e);
        }

        return null;
    }

    /**
     * @param mtomState
     * @param serviceGroupName
     * @return
     * @throws AxisFault
     */
    public ServiceGroupMetaData configureServiceGroupMTOM(String mtomState, String serviceGroupName)
            throws AxisFault {
        try {
            return stub.configureServiceGroupMTOM(mtomState, serviceGroupName);
        } catch (java.lang.Exception e) {
            String msg = "Cannot change MOM state of Axis service group" + serviceGroupName
                         + " .  Error is "+ e.getMessage();
            handleException(msg, e);
        }

        return null;
    }

    /**
     * @param serviceGroupName
     * @return
     * @throws AxisFault
     */
    public ServiceGroupMetaData listServiceGroup(String serviceGroupName) throws AxisFault {
        try {
            return stub.listServiceGroup(serviceGroupName);
        } catch (java.lang.Exception e) {
            String msg = "Cannot get service group data. Error is "+ e.getMessage();
            handleException(msg, e);
        }
        return null;
    }

    /**
     * @param serviceGroupName
     * @param params
     * @throws AxisFault
     */
    public void setServiceGroupParamters(String serviceGroupName, List<String> params)
            throws AxisFault {
        try {
            stub.setServiceGroupParameters(serviceGroupName, params.toArray(new String[params
                    .size()]));
        } catch (java.lang.Exception e) {
            String msg = "Cannot get service group data.  Error is "+ e.getMessage();
            handleException(msg, e);
        }
    }

    /**
     * @param serviceGroupName
     * @param parameterName
     * @throws AxisFault
     */
    public void removeServiceGroupParameter(String serviceGroupName, String parameterName)
            throws AxisFault {
        try {
            stub.removeServiceGroupParameter(serviceGroupName, parameterName);
        } catch (java.lang.Exception e) {
            handleException("Could not remove service group parameter. Service group: "
                            + serviceGroupName + ", parameterName=" + parameterName, e);
        }
    }

    /**
     * @param serviceGroupName
     * @return
     * @throws AxisFault
     */
    public String dumpAAR(String serviceGroupName) throws AxisFault {
        try {
            String dumpValue = stub.dumpAAR(serviceGroupName);
            if (dumpValue != null) {
                return dumpValue;
            } else {
                handleException("Service archive creation is not supported by this service type");
            }
        } catch (java.lang.Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @param msg
     * @param e
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

}
