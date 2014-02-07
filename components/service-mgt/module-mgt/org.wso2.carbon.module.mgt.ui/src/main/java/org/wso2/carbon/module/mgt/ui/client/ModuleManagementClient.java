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
package org.wso2.carbon.module.mgt.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceStub;
import org.wso2.carbon.module.mgt.stub.types.ModuleMetaData;

import org.wso2.carbon.module.mgt.stub.types.ModuleUploadData;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ModuleManagementClient {

    private static final Log log = LogFactory.getLog(ModuleManagementClient.class);
    public ModuleAdminServiceStub stub;

    public ModuleManagementClient(ConfigurationContext configCtx, String backendServerURL,
                                  String cookie, boolean mtom) throws AxisFault {
        String serviceURL = backendServerURL + "ModuleAdminService";
        stub = new ModuleAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        if (mtom) {
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        }
    }

    public ModuleMetaData getModuleInfo(String moduleName, String moduleVersion)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.getModuleInfo(moduleName, moduleVersion);
        } catch (RemoteException e) {
            String msg = "Cannot get info about " + moduleName
                    + " . Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public void uploadService(ModuleUploadData[] moduleUploadData)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            stub.uploadModule(moduleUploadData);
        } catch (RemoteException e) {
            String msg = "Cannot upload the modules "
                         + " . Backend service may be unvailable";
            handleException(msg, e);
        }
    }

    public ModuleMetaData[] listModules() throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.listModules();
        } catch (RemoteException e) {
            String msg = "Cannot list modules. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public ModuleMetaData[] listModulesForOperation(String serviceName, String operationName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.listModulesForOperation(serviceName, operationName);
        } catch (RemoteException e) {
            String msg = "Cannot list modules for service. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public ModuleMetaData[] listModulesForService(String serviceName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.listModulesForService(serviceName);
        } catch (RemoteException e) {
            String msg = "Cannot list modules for service. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public ModuleMetaData[] listModulesForServiceGroup(String serviceGroupName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.listModulesForServiceGroup(serviceGroupName);
        } catch (RemoteException e) {
            String msg = "Cannot list modules for service group. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public ModuleMetaData[] listGloballyEngagedModules()
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.listGloballyEngagedModules();
        } catch (RemoteException e) {
            String msg = "Cannot list modules. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public boolean globallyEngageModule(String moduleId)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.globallyEngageModule(moduleId);
        } catch (RemoteException e) {
            String msg = "Cannot globally engage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean globallyDisengageModule(String moduleId)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.globallyDisengageModule(moduleId);
        } catch (RemoteException e) {
            String msg = "Cannot globally disengage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean engageModuleForOperation(String moduleId, String serviceName, String operationName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.engageModuleForOperation(moduleId, serviceName, operationName);
        } catch (RemoteException e) {
            String msg = "Cannot engage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean disengageModuleForOperation(String moduleId, String serviceName, String operationName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.disengageModuleForOperation(moduleId, serviceName, operationName);
        } catch (RemoteException e) {
            String msg = "Cannot disengage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean engageModuleForService(String moduleId, String serviceName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.engageModuleForService(moduleId, serviceName);
        } catch (RemoteException e) {
            String msg = "Cannot engage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean disengageModuleForService(String moduleId, String serviceName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.disengageModuleForService(moduleId, serviceName);
        } catch (RemoteException e) {
            String msg = "Cannot disengage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean engageModuleForServiceGroup(String moduleId, String serviceGroup)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.engageModuleForServiceGroup(moduleId, serviceGroup);
        } catch (RemoteException e) {
            String msg = "Cannot engage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public boolean disengageModuleForServiceGroup(String moduleId, String serviceGroup)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.disengageModuleForServiceGroup(moduleId, serviceGroup);
        } catch (RemoteException e) {
            String msg = "Cannot disengage module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return false;
    }

    public void setModuleParameters(String moduleName, String moduleVersion, List<String> params)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            stub.setModuleParameters(moduleName, moduleVersion, params.toArray(new String[params.size()]));
        } catch (RemoteException e) {
            String msg = "Cannot set module parameters. Backend service may be unvailable";
            handleException(msg, e);
        }
    }

    public List<Parameter> getModuleParameters(String moduleName, String moduleVersion)
            throws ModuleAdminServiceModuleMgtExceptionException {

        List<Parameter> parameters = new ArrayList<Parameter>();

        try {
            String[] moduleParameters = stub.getModuleParameters(moduleName, moduleVersion);
            if (moduleParameters != null && moduleParameters.length != 0) {
                for (String moduleParameter : moduleParameters) {
                    XMLStreamReader xmlSR = StAXUtils
                            .createXMLStreamReader(new ByteArrayInputStream(moduleParameter
                                    .getBytes()));
                    OMElement paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
                    String paramName = paramEle.getAttribute(new QName("name")).getAttributeValue();

                    Parameter parameter = new Parameter(paramName, moduleParameter);

                    String locked = paramEle.getAttributeValue(new QName("locked"));
                    if(Boolean.TRUE.toString().equals(locked)) {
                       parameter.setLocked(true);
                    }

                    parameter.setParamValue(paramEle.getText());
                    parameters.add(parameter);
                }
            }
        } catch (Exception e) {
            String msg = "Could not get module parameters for service: " + moduleName + ":" + moduleVersion;
            handleException(msg, e);
        }

        return parameters;
    }

    public String removeModule(String moduleId) throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.removeModule(moduleId);
        } catch (RemoteException e) {
            String msg = "Cannot remove module. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    public String removeModuleParameter(String moduleName, String moduleVersion, String paramName)
            throws ModuleAdminServiceModuleMgtExceptionException {
        try {
            return stub.removeModuleParameter(moduleName,moduleVersion,paramName);
        } catch (RemoteException e) {
            String msg = "Cannot get module parameters. Backend service may be unvailable";
            handleException(msg, e);
        }
        return null;
    }

    private void handleException(String msg, Exception e)
            throws ModuleAdminServiceModuleMgtExceptionException {
        log.error(msg, e);
        throw new ModuleAdminServiceModuleMgtExceptionException(msg, e);
    }

}