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
package org.wso2.carbon.module.mgt.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.Version;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.module.mgt.ModuleMetaData;
import org.wso2.carbon.module.mgt.ModuleMgtException;
import org.wso2.carbon.module.mgt.ModuleMgtMessageKeys;
import org.wso2.carbon.module.mgt.ModuleUploadData;
import org.wso2.carbon.module.mgt.internal.DataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Admin service for managing Axis2 Modules
 */
@SuppressWarnings("unused")
public class ModuleAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ModuleAdminService.class);

    public final static String THROTTLE_MODULE = "wso2throttle";
    public final static String CACHING_MODULE = "wso2caching";
    public final static String ADDRESSING_MODULE = "addressing";
    private static final String GLOBALLY_ENGAGED_PARAM_NAME = "globallyEngaged";

    private static final String RAMPART_MODULE_NAME = "rampart";
    private static final String RAHAS_MODULE_NAME = "rahas";
    
    private PersistenceFactory pf;
    
    public ModuleAdminService() throws Exception {
        this.axisConfig = getAxisConfig();
        pf = PersistenceFactory.getInstance(axisConfig);
    }

    public ModuleAdminService(AxisConfiguration ac) throws Exception {
        this.axisConfig = ac;
        pf = PersistenceFactory.getInstance(axisConfig);
    }

    /**
     * List all the available modules
     *
     * @return list of modules as a ModuleMetadata[].
     */
    public ModuleMetaData[] listModules() {

        List<ModuleMetaData> moduleList = new ArrayList<ModuleMetaData>();
        for (Iterator moduleIter = this.axisConfig.getModules().values().iterator(); moduleIter
                .hasNext();) {
            AxisModule axisModule = (AxisModule) moduleIter.next();

            if (SystemFilter.isFilteredOutModule(axisModule)) {
                continue;
            }

            if (axisModule.getName() != null) {
                ModuleMetaData moduleMetaData = populateModuleMetaData(axisModule);
                moduleList.add(moduleMetaData);
            }
        }

        return moduleList.toArray(new ModuleMetaData[moduleList.size()]);
    }

    /**
     * List all the globally engaged modules
     *
     * @return list of globally engaged modules
     */
    public ModuleMetaData[] listGloballyEngagedModules() {
        List<ModuleMetaData> emodules = new ArrayList<ModuleMetaData>();

        for (Iterator engagedModules = this.axisConfig.getEngagedModules().iterator();
             engagedModules.hasNext();) {

            AxisModule axisModule = (AxisModule) engagedModules.next();
            String moduleVersion = "";
            if (axisModule.getVersion() != null) {
                moduleVersion = axisModule.getVersion().toString();
            }
            String moduleId = getModuleId(axisModule.getName(), axisModule.getVersion());

            //Filtering out Admin modules 
            if (SystemFilter.isFilteredOutModule(axisModule)) {
                continue;
            }

            if (axisConfig.isEngaged(moduleId)) {
                emodules.add(new ModuleMetaData(axisModule.getName(), moduleVersion));
                continue;
            }

            Parameter param = axisModule.getParameter(GLOBALLY_ENGAGED_PARAM_NAME);
            if (param != null) {
                String globallyEngaged = (String) param.getValue();
                if (globallyEngaged != null && globallyEngaged.length() != 0
                        && Boolean.parseBoolean(globallyEngaged.trim())) {
                    emodules.add(new ModuleMetaData(axisModule.getName(), moduleVersion));
                }
            }
        }

        return emodules.toArray(new ModuleMetaData[emodules.size()]);
    }

    /**
     * List modules medatadata about given operation
     * @param serviceName  Axis service
     * @param operationName Axis Operation
     * @return Array of module metadata
     * @throws ModuleMgtException when we can't get module metadata
     */
    public ModuleMetaData[] listModulesForOperation(String serviceName, String operationName)
            throws ModuleMgtException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisOperation operation = service.getOperation(new QName(operationName));
        if (operation == null) {
            log.error("Operation " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.OPERATION_NOT_FOUND);
        }

        AxisServiceGroup serviceGroup = service.getAxisServiceGroup();
        ModuleMetaData[] modules = listModules();

        for (ModuleMetaData module : modules) {
            String moduleId = module.getModuleId();
            module.setEngagedServiceGroupLevel(serviceGroup.isEngaged(moduleId));
            module.setEngagedServiceLevel(service.isEngaged(moduleId));
            module.setEngagedOperationLevel(operation.isEngaged(moduleId));
        }

        return modules;
    }

    public ModuleMetaData[] listModulesForService(String serviceName) throws ModuleMgtException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisServiceGroup serviceGroup = service.getAxisServiceGroup();
        ModuleMetaData[] modules = listModules();

        for (ModuleMetaData module : modules) {
            String moduleId = module.getModuleId();
            module.setEngagedServiceGroupLevel(serviceGroup.isEngaged(moduleId));
            module.setEngagedServiceLevel(service.isEngaged(moduleId));
        }

        return modules;

    }

    public ModuleMetaData[] listModulesForServiceGroup(String serviceGroupName) throws ModuleMgtException {

        AxisServiceGroup serviceGroup = axisConfig.getServiceGroup(serviceGroupName);
        if (serviceGroup == null) {
            log.error("Service group " + serviceGroupName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_GROUP_NOT_FOUND);
        }

        ModuleMetaData[] modules = listModules();

        for (ModuleMetaData module : modules) {
            String moduleId = module.getModuleId();
            module.setEngagedServiceGroupLevel(serviceGroup.isEngaged(moduleId));
        }

        return modules;
    }

    /**
     * Return all available module meta-data (not counts)
     *
     * @param moduleName    -
     *                      moduleName
     * @param moduleVersion -
     *                      moduleVersion
     * @return moduleMetaData info of the module
     * @throws ModuleMgtException -
     *                   error accessing axis config
     */
    public ModuleMetaData getModuleInfo(String moduleName, String moduleVersion) throws ModuleMgtException {

        AxisModule axisModule = getAxisModule(moduleName, moduleVersion);
        if (axisModule == null) {
            log.error("Module " + moduleName + "-" + moduleVersion + " cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        return populateModuleMetaData(axisModule);
    }

    public boolean globallyEngageModule(String moduleId) throws ModuleMgtException {

        try {
            AxisModule axisModule = getAxisModule(moduleId);
            if (axisModule == null) {
                log.error("Module " + moduleId + " cannnot be found!");
                throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
            }

            if (axisConfig.isEngaged(axisModule)) {
                throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_GLOBALLY);
            }

            // Check for rahas engagement. Rahas requires Rampart also to be engaged
            if (RAHAS_MODULE_NAME.equalsIgnoreCase(axisModule.getName())) {
                AxisModule rampartModule = axisConfig.getModule(RAMPART_MODULE_NAME);
                if (rampartModule == null) {
                    log.error("Rampart module not found when engaging Rampart");
                    throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.RAHAS_RAMPART_NOT_FOUND);
                }
                if (!axisConfig.isEngaged(rampartModule)) {
                    globallyEngageModule(getModuleId(rampartModule.getName(),
                            rampartModule.getVersion()));
                }
            }

            try {
                axisConfig.engageModule(axisModule);
                persistGloballyEngagedStatus(axisModule, true);
            } catch (AxisFault axisFault) {
                log.error("Error occured while globally engaging the module " + moduleId, axisFault);
                throw new ModuleMgtException(axisFault, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_GLOBAL_ENGAGE);
            }

//            pf.getModulePM().globallyEngageModule(axisModule);

        } catch (Exception e) {
            log.error("Error occured while globally engaging the module " + moduleId, e);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_GLOBAL_ENGAGE);
        }

        return true;

    }

    /**
     * This method persists the globally engaged status of a module in  the registry
     * @param axisModule
     * @param globallyEngagedStatus
     */
    private void persistGloballyEngagedStatus(AxisModule axisModule, boolean globallyEngagedStatus) {
        if (DataHolder.getRegistryService() != null) {
            try {
                Registry configSystemRegistry = DataHolder.getRegistryService().getConfigSystemRegistry(
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

                String moduleResourcePath = getModuleResourcePath(axisModule);
                Resource moduleResource;
                if (configSystemRegistry.resourceExists(moduleResourcePath)) {
                    moduleResource = configSystemRegistry.get(moduleResourcePath);
                } else {
                    moduleResource = configSystemRegistry.newCollection();
                }
                moduleResource.setProperty(RegistryResources.ModuleProperties.GLOBALLY_ENGAGED,
                                           Boolean.toString(globallyEngagedStatus));
                configSystemRegistry.put(moduleResourcePath, moduleResource);
            } catch (RegistryException e) {
                log.error("Failed to persist globally engaged status of the module: " + axisModule.getName(), e);

            }
        }
    }

    private String getModuleResourcePath(AxisModule axisModule) {
        return RegistryResources.MODULES + axisModule.getName() + "/" + axisModule.getVersion();
    }

    public boolean engageModuleForServiceGroup(String moduleID, String serviceGroupName)
            throws ModuleMgtException {

        AxisServiceGroup serviceGroup = axisConfig.getServiceGroup(serviceGroupName);
        if (serviceGroup == null) {
            log.error("Service group " + serviceGroupName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_GROUP_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleID);
        if (module == null) {
            log.error("Module " + moduleID + " cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        if (axisConfig.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_GLOBALLY);
        }

        if (serviceGroup.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_SERVICE_GROUP);
        }

        try {
            if (RAHAS_MODULE_NAME.equals(module.getName())) {
                AxisModule rampartModule = axisConfig.getModule(RAMPART_MODULE_NAME);
                if (rampartModule == null) {
                    log.error("Rampart module not found when engaging Rampart");
                    throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.RAHAS_RAMPART_NOT_FOUND);
                }
                if (!serviceGroup.isEngaged(rampartModule)) {
                    pf.getServiceGroupPM().engageModuleForServiceGroup(rampartModule, serviceGroup);
                    serviceGroup.disengageModule(rampartModule);
                    serviceGroup.engageModule(rampartModule);
                }
            }

            pf.getServiceGroupPM().engageModuleForServiceGroup(module, serviceGroup);
            serviceGroup.disengageModule(module);
            serviceGroup.engageModule(module);

            return true;

        } catch (Exception e) {
            log.error("Error occured while engaging the module " + module, e);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_ENGAGE);
        }

    }

    public boolean disengageModuleForServiceGroup(String moduleId, String serviceGroupName)
            throws ModuleMgtException {

        AxisServiceGroup serviceGroup = axisConfig.getServiceGroup(serviceGroupName);
        if (serviceGroup == null) {
            log.error("Service group " + serviceGroupName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_GROUP_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module " + moduleId + " cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        if (serviceGroup.isEngaged(module)) {
            if (RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                // Check whether it is possible to disengage Rampart
                AxisModule rahasModule = axisConfig.getModule(RAHAS_MODULE_NAME);
                if (serviceGroup.isEngaged(rahasModule)) {
                    throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.RAHAS_RAMPART_DISENGAGE);
                }
            }

            if (RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName()) || RAHAS_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                // Check any of the services in the service group has a security scenario applied
                // if there is a security scenario give a warning saying cannot disengage rampart
                Iterator<AxisService> servicesIterator = serviceGroup.getServices();
                while (servicesIterator.hasNext()) {
                    AxisService service = servicesIterator.next();
                    if (isRequiredForSecurityScenario(service.getName(),module.getName())) {
                        throw new ModuleMgtException(ModuleMgtException.WARNING,
                                ModuleMgtMessageKeys.SERVICES_WITH_SECURITY_SCENARIOS);
                    }
                }
            }

            try {
                pf.getServiceGroupPM().disengageModuleForServiceGroup(module, serviceGroup);
                serviceGroup.disengageModule(module);
            } catch (Exception e) {
                log.error("Error occured while disengaging the module " + module, e);
                throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_DISENGAGE);
            }

            return true;
        }

        throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.NOT_ENGAGED);
    }

    public boolean engageModuleForService(String moduleId, String serviceName) throws ModuleMgtException {
        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service  " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module " + moduleId + " cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        if (axisConfig.isEngaged(module)) {
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_GLOBALLY);
        }

        AxisServiceGroup axisServiceGroup = service.getAxisServiceGroup();
        if (axisServiceGroup.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_SERVICE_GROUP);
        }

        if (service.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_SERVICE);
        }

        try {
            if (RAHAS_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                AxisModule rampartModule = axisConfig.getModule(RAMPART_MODULE_NAME);
                if (rampartModule == null) {
                    throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.RAHAS_RAMPART_NOT_FOUND);
                }
                if (!service.isEngaged(rampartModule)) {
                    pf.getServicePM().engageModuleForService(rampartModule, service);
                    service.disengageModule(rampartModule);
                    service.engageModule(rampartModule);
                }
            }

            pf.getServicePM().engageModuleForService(module, service);
            service.disengageModule(module);
            service.engageModule(module);

            return true;

        } catch (Exception e) {
            String msg = "Error occured while engaging the module " + module;
            log.error(msg, e);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_ENGAGE);
        }

    }

    public boolean disengageModuleForService(String moduleId, String serviceName) throws ModuleMgtException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service  " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module " + moduleId + " cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        if (service.isEngaged(module)) {
            if (RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                // Check whether it is possible to disengage Rampart
                AxisModule rahasModule = axisConfig.getModule(RAHAS_MODULE_NAME);
                if (service.isEngaged(rahasModule)) {
                    throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.RAHAS_RAMPART_DISENGAGE);
                }
            }

            if (isRequiredForSecurityScenario(serviceName, moduleId)) {
                throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.SEC_SCENARIO_ENGAGED);
            }

            try {
                pf.getServicePM().disengageModuleForService(module, service);
                service.disengageModule(module);
            } catch (Exception e) {
                String msg = "Error occured while disengaging the module " + moduleId
                        + " from service " + serviceName;
                log.error(msg, e);
                throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_DISENGAGE);
            }
        }

        return true;

    }

    public boolean engageModuleForOperation(String moduleId, String serviceName, String operationName)
            throws ModuleMgtException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service  " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisOperation operation = service.getOperation(new QName(operationName));
        if (operation == null) {
            log.error("Operation  " + operationName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.OPERATION_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module  " + moduleId + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.OPERATION_NOT_FOUND);
        }

        if (axisConfig.isEngaged(module)) {
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_GLOBALLY);
        }

        AxisServiceGroup axisServiceGroup = service.getAxisServiceGroup();
        if (axisServiceGroup.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_SERVICE_GROUP);
        }

        if (service.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_SERVICE);
        }

        if (operation.isEngaged(module)) {
            // Module is already engaged so just return the value;
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ALREADY_ENGAGED_OPERATION);
        }

        try {
            if (RAHAS_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                AxisModule rampartModule = axisConfig.getModule(RAMPART_MODULE_NAME);
                if (rampartModule == null) {
                    throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.RAHAS_RAMPART_NOT_FOUND);
                }
                if (!operation.isEngaged(rampartModule)) {
                    pf.getOperationPM().engageModuleForOperation(rampartModule, operation);
                    operation.disengageModule(rampartModule);
                    operation.engageModule(rampartModule);
                }
            }

            pf.getOperationPM().engageModuleForOperation(module, operation);
            operation.disengageModule(module);
            operation.engageModule(module);

        } catch (Exception e) {
            String msg = "Error occured while engaging the module " + moduleId + " to "
                    + operationName;
            log.error(msg, e);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_ENGAGE);
        }

        return true;
    }

    public boolean disengageModuleForOperation(String moduleId, String serviceName,
                                               String operationName) throws ModuleMgtException {

        AxisService service = axisConfig.getServiceForActivation(serviceName);
        if (service == null) {
            log.error("Service  " + serviceName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.SERVICE_NOT_FOUND);
        }

        AxisOperation operation = service.getOperation(new QName(operationName));
        if (operation == null) {
            log.error("Operation  " + operationName + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.OPERATION_NOT_FOUND);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module  " + moduleId + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.OPERATION_NOT_FOUND);
        }

        if (operation.isEngaged(module)) {
            if (RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                // Check whether it is possible to disengage Rampart
                AxisModule rahasModule = axisConfig.getModule(RAHAS_MODULE_NAME);
                if (operation.isEngaged(rahasModule)) {
                    throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.RAHAS_RAMPART_DISENGAGE);
                }
            }

            try {
                pf.getOperationPM().disengageModuleForOperation(module, operation);
                operation.disengageModule(module);

            } catch (Exception e) {
                String msg = "Error occured while disengaging the module " + moduleId
                        + " from operation " + operationName;
                log.error(msg, e);
                throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_DISENGAGE);
            }
        }

        return true;
    }

    public boolean globallyDisengageModule(String moduleId) throws ModuleMgtException {

        if (moduleId.startsWith("addressing")) {
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.DISENGAGE_ADDRESSING_GLOBALLY);
        }

        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module  " + moduleId + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        try {
            disengageModuleFromSystem(moduleId);
            persistGloballyEngagedStatus(module, false);
        } catch (ModuleMgtException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error occured while globally disengaging the module " + moduleId;
            log.error(msg, e);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_DISENGAGE);
        }

        return true;
    }

    private AxisModule getAxisModule(String moduleId, String moduleVersion) throws ModuleMgtException {
        AxisModule module = this.axisConfig.getModule(moduleId, moduleVersion);

        if (module == null) {
            log.error("Module  " + moduleId + "-" + moduleVersion + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        return module;
    }

    private AxisModule getAxisModule(String moduleId) throws ModuleMgtException {
        AxisModule module = this.axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module  " + moduleId + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }
        return module;
    }

    public String uploadModule(ModuleUploadData[] moduleUploadData) throws AxisFault {

        String fileName = "";
        try {

            String repo = getAxisConfig().getRepository().getPath();

            if (CarbonUtils.isURL(repo)) {
                throw new AxisFault("Uploading modules to URL repository is not allowed");
            }

            // Composing the proper location to copy the artifact
            // TODO we need to get this from AxisConfiguration as we can set
            // directory name can be
            // changed
            File modulesDir = new File(repo, "axis2modules");
            if (!modulesDir.exists() && !modulesDir.mkdir()) {
                log.warn("Cannot create " + modulesDir.getAbsolutePath());
            }

            // deploy module by module.
            for (ModuleUploadData uploadData : moduleUploadData) {
                fileName = uploadData.getFileName();
                writeToRepository(modulesDir.getAbsolutePath(), fileName, uploadData.getDataHandler());    
            }


        } catch (Exception e) {
            String msg = "Error occured while uploading the module " + fileName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        return "successful";
    }

    /**
     * return all parameters for this module (including inherited ones), where
     * each parameter is an XML fragment representing the "parameter" element
     *
     * @param moduleName module name of the module of which parameters are required
     * @param moduleVersion module version of the module of which parameters are required
     * @return params module parameter array
     * @throws ModuleMgtException is thrown in case of error
     */
    public String[] getModuleParameters(String moduleName, String moduleVersion) throws ModuleMgtException {

        AxisModule module = getAxisModule(moduleName, moduleVersion);
        if (module == null) {
            log.error("Module  " + module + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        ArrayList<String> parameters = new ArrayList<String>();

        ArrayList moduleParams = module.getParameters();

        for (Object serviceParam : moduleParams) {
            Parameter parameter = (Parameter) serviceParam;
            if (parameter.getParameterElement() != null) {
                parameters.add(parameter.getParameterElement().toString());
            }
        }

        Collections.sort(parameters, new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                return arg0.compareToIgnoreCase(arg1);
            }
        });

        return parameters.toArray(new String[parameters.size()]);
    }

    public void setModuleParameters(String moduleName, String moduleVersion, String[] parameters) throws ModuleMgtException {

        for (String parameter : parameters) {
            setModuleParameter(moduleName,moduleVersion, parameter);
        }
    }

    private String setModuleParameter(String moduleName, String moduleVersion, String parameterStr) throws ModuleMgtException {

        AxisModule module = getAxisModule(moduleName, moduleVersion);
        if (module == null) {
            log.error("Module  " + module + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        OMElement paramEle;
        try {
            XMLStreamReader xmlSR = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                    parameterStr.getBytes()));
            paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (XMLStreamException e) {
            String msg = "Cannot create OMElement from parameter: " + parameterStr;
            log.error(msg, e);
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_PARAM_REMOVE);
        }

        try {
            Parameter parameter = ParameterUtil.createParameter(paramEle);
            if (module.getParameter(parameter.getName()) == null || !module.getParameter(parameter.getName()).isLocked()) {
                module.addParameter(parameter);
                pf.getModulePM().updateModuleParameter(module, parameter);
            }
        } catch (Exception e) {
            String msg = "Cannot persist module parameter for operation " + module.getName();
            log.error(msg, e);
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_PARAM_REMOVE);
        }

        return "Succesfully updated service parameters";
    }

    public String removeModuleParameter(String moduleName, String moduleVersion, String parameterName) throws ModuleMgtException {

        AxisModule module = getAxisModule(moduleName, moduleVersion);
        if (module == null) {
            log.error("Module  " + module + "cannot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

       try {
            Parameter parameter = ParameterUtil.createParameter(parameterName, null);
            module.removeParameter(parameter);
            pf.getModulePM().removeModuleParameter(module, parameter);
        } catch (Exception e) {
            String msg = "Cannot persist parameter removal from module  " + module.getName();
            log.error(msg, e);
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_PARAM_REMOVE);
        }

        return "Successfully removed parameter " + parameterName + " from " + moduleName + ":" + moduleVersion + " module";
    }

    /**
     * Delete a module
     *
     * @param moduleId module id of the module to be removed
     * @return status of the operation
     * @throws ModuleMgtException if we can't remove the module
     */
    public String removeModule(String moduleId) throws ModuleMgtException {

        // We cannot delete items from a URL repo
        // First lets filter for jar resources
        String repo = getAxisConfig().getRepository().getPath();

        if (CarbonUtils.isURL(repo)) {
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.URL_REPO);
        }

        // We cannot remove the addressing module if at least one service is
        // deployed in soapsession scope
        if (moduleId.startsWith("addressing")) {
            throw new ModuleMgtException(ModuleMgtException.WARNING,
                                         ModuleMgtMessageKeys.DISENGAGE_ADDRESSING_GLOBALLY);
        }

        // Check whether this file can be deleted.
        // We should proceed only if this file can be deleted.
        AxisModule module = axisConfig.getModule(moduleId);
        if (module == null) {
            log.error("Module  " + moduleId + "cannnot be found!");
            throw new ModuleMgtException(ModuleMgtException.ERROR,
                                         ModuleMgtMessageKeys.MODULE_NOT_FOUND);
        }

        if (module.getFileName() != null) {
            String fileName = module.getFileName().getPath();
            File file = new File(fileName);
            if (!file.canWrite()) {
                throw new ModuleMgtException(ModuleMgtException.WARNING,
                                             ModuleMgtMessageKeys.MODULE_DELETE_ERROR);
            }

            if (isEngaged(module)) {
                throw new ModuleMgtException(ModuleMgtException.WARNING,
                                             ModuleMgtMessageKeys.ENGAGED_MODULE_REMOVE);
            }
            
            disengageModuleFromSystem(moduleId);

            // Delete the MAR file
            if (file.exists()) {
                if (!(file.isDirectory() && FileManipulator.deleteDir(file))) {
                    if (!file.delete()) {
                        throw new ModuleMgtException(ModuleMgtException.WARNING,
                                                     ModuleMgtMessageKeys.MODULE_DELETE_ERROR);
                    }
                }
            } else {
                throw new ModuleMgtException(ModuleMgtException.WARNING,
                                             ModuleMgtMessageKeys.MODULE_FILE_NOT_FOUND);
            }
        } else {
            throw new ModuleMgtException(ModuleMgtException.WARNING,
                                         ModuleMgtMessageKeys.SYSTEM_MODULE_DELETE);
        }

        try {
            pf.getModulePM().removeModule(module);
            axisConfig.removeModule(module.getName(), module.getVersion());
        } catch (Exception e) {
            log.error("Error while removing module : " + moduleId, e);
            throw new ModuleMgtException(ModuleMgtException.WARNING, ModuleMgtMessageKeys.ERROR_MODULE_REMOVE);
        }

        return "Module " + moduleId + " was successfully removed from system";
    }

    /**
     * @param moduleId module to be globally disenaged
     * @return True if the module the engagement was successful, false
     *         otherwise.
     * @throws ModuleMgtException if we can't disengage the module globally
     */
    public boolean disengageModuleFromSystem(String moduleId)
            throws ModuleMgtException {
        if (moduleId.startsWith("addressing")) {
            throw new ModuleMgtException(ModuleMgtException.WARNING,
                                         ModuleMgtMessageKeys.DISENGAGE_ADDRESSING_GLOBALLY);
        }
        AxisModule module = getAxisModule(moduleId);
        try {
            if (axisConfig.isEngaged(module)) {
                if (RAHAS_MODULE_NAME.equalsIgnoreCase(module.getName()) || RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                    Map services = axisConfig.getServices();
                    for (Iterator iter = services.values().iterator(); iter.hasNext();) {
                        AxisService service = (AxisService) iter.next();

                        if (isRequiredForSecurityScenario(service.getName(), moduleId)) {
                            throw new ModuleMgtException(ModuleMgtException.WARNING,
                                    ModuleMgtMessageKeys.SERVICES_WITH_SECURITY_SCENARIOS);
                        }
                    }
                }

                if (RAMPART_MODULE_NAME.equalsIgnoreCase(module.getName())) {
                    // Check whether it is possible to disengage Rampart
                    AxisModule rahasModule = axisConfig.getModule(RAHAS_MODULE_NAME);
                    if (axisConfig.isEngaged(rahasModule)) {
                        throw new ModuleMgtException(ModuleMgtException.WARNING,
                                                     ModuleMgtMessageKeys.RAHAS_RAMPART_DISENGAGE);
                    }
                }

                // store the global engagement status
//                pf.getModulePM().globallyDisengageModule(module);
                axisConfig.disengageModule(module);
                Parameter param = new Parameter(GLOBALLY_ENGAGED_PARAM_NAME, Boolean.FALSE.toString());
                module.addParameter(param);
            }
        } catch (ModuleMgtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while disengaging module " + module.getName(), e);
            throw new ModuleMgtException(ModuleMgtException.ERROR, ModuleMgtMessageKeys.ERROR_GLOBAL_DISENGAGE);
        }

        return true;
    }

    private void writeToRepository(String path, String fileName, DataHandler dataHandler)
            throws Exception {
        File destFile = new File(path, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            dataHandler.writeTo(fos);
            fos.flush();
            fos.close();
        } finally {
            if(fos != null) {
                fos.close();
            }
        }
    }

    private boolean isRequiredForSecurityScenario(String serviceName, String moduleId) throws ModuleMgtException {

        String[] requiredModules;
        try {
            SecurityConfigAdmin secConfAdmin = new SecurityConfigAdmin(getUserRealm(), getConfigSystemRegistry()
                    ,axisConfig);
            requiredModules = secConfAdmin.getRequiredModules(serviceName, moduleId);
        } catch (Exception e) {
            String msg = "Error occured while getting the security scenarions for the service";
            log.error(msg);
            throw new ModuleMgtException(e, ModuleMgtException.ERROR, ModuleMgtMessageKeys.SEC_SCENARIO_ERROR);
        }

        if (requiredModules == null) {
            return false;
        }

        for (String module : requiredModules) {
            if (moduleId.startsWith(module)) {
                return true;
            }
        }

        return false;

    }

    private ModuleMetaData populateModuleMetaData(AxisModule axisModule) {

        ModuleMetaData moduleMetaData = new ModuleMetaData();

        String moduleVersion = "";
        if (axisModule.getVersion() != null) {
            moduleVersion = axisModule.getVersion().toString();
        }

        moduleMetaData.setModulename(axisModule.getName());
        moduleMetaData.setModuleVersion(moduleVersion);
        moduleMetaData.setModuleId(getModuleId(axisModule.getName(), axisModule.getVersion()));
        moduleMetaData.setEngagedGlobalLevel(axisConfig.isEngaged(axisModule));

        //Some moduels like caching, throtteling use "globallyEngaged" parameter, to engage themself globally without
        //affecting the admin services 
        Parameter param = axisModule.getParameter(GLOBALLY_ENGAGED_PARAM_NAME);
        if (param != null) {
            String globallyEngaged = (String) param.getValue();
            if (globallyEngaged != null && globallyEngaged.length() != 0
                        && Boolean.parseBoolean(globallyEngaged.trim())) {
                    moduleMetaData.setEngagedGlobalLevel(true);
            }
        }

        // Set whether this is a system managed module, like throtelling, caching
        moduleMetaData.setManagedModule(SystemFilter.isManagedModule(axisModule));

        String description = axisModule.getModuleDescription();

        //TODO this logic needs to go in to UI
        if (description != null) {
            moduleMetaData.setDescription(description);
        } else {
            moduleMetaData.setDescription("No description found");
        }

        return moduleMetaData;
    }

    /**
     * Checks whether module is engaged globally or to any of the services, service groups or
     * operations
     * @param module module to be checked for engagement
     * @return true if engaged at any level
     */
    private boolean isEngaged(AxisModule module) {
        if(axisConfig.isEngaged(module)) {
            return true;
        }
        for(Iterator serviceGroups = axisConfig.getServiceGroups(); serviceGroups.hasNext(); ){
            AxisServiceGroup serviceGroup = (AxisServiceGroup) serviceGroups.next();
            if(serviceGroup.isEngaged(module)) {
                return true;
            }
        }
        Map services = axisConfig.getServices();
        for (Iterator iter = services.values().iterator(); iter.hasNext();) {
            AxisService service = (AxisService) iter.next();
            if (service.isEngaged(module)) {
                return true;
            }
            for (Iterator ops = service.getOperations(); ops.hasNext();) {
                AxisOperation op = (AxisOperation) ops.next();
                if (op.isEngaged(module)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getModuleId(String moduleName, Version moduleVersion) {
        if (moduleVersion != null) {
            String version = moduleVersion.toString();
            if (version != null && version.length() != 0) {
                moduleName = moduleName + "-" + moduleVersion;
            }
        }
        return moduleName;
    }

}
