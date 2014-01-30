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

package org.wso2.carbon.operation.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
//import org.wso2.wsas.admin.service.util.OperationMetaData;
//import org.wso2.wsas.persistence.PersistenceManager;
//import org.wso2.wsas.persistence.dataobject.OperationDO;
//import org.wso2.wsas.persistence.dataobject.OperationParameterDO;
//import org.wso2.wsas.persistence.dataobject.ServiceIdentifierDO;
//import org.wso2.wsas.persistence.exception.DuplicateEntityException;
//import org.wso2.wsas.util.ParameterUtil;
//import org.wso2.wsas.util.PolicyUtil;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.OperationPersistenceManager;
import org.wso2.carbon.core.util.ParameterUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.ByteArrayInputStream;


/**
 * Admin service to manage service oprations
 */
public class OperationAdmin extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(OperationAdmin.class);
    private OperationPersistenceManager opm;

    public OperationAdmin() throws Exception {
        opm = new OperationPersistenceManager(getAxisConfiguration());
    }

    private AxisConfiguration getAxisConfiguration() {
        return getConfigurationContext().getAxisConfiguration();
    }

    private ConfigurationContext getConfigurationContext() {
        return MessageContext.getCurrentMessageContext().getConfigurationContext();
    }
    
    /**
     * list all the operations in the service including both control and published operations
     *
     * @param serviceName
     * @return list of all operations
     * @throws AxisFault
     */
    public OperationMetaDataWrapper listAllOperations(String serviceName) throws AxisFault {
        OperationMetaDataWrapper operationMetaDataWrapper = new OperationMetaDataWrapper();
        operationMetaDataWrapper.setPublishedOperations(listPublishedOperations(serviceName));
        operationMetaDataWrapper.setControlOperations(listControlOperations(serviceName));
        return operationMetaDataWrapper;
    }

    /**
     * list only the control operations added by modules and some other way
     *
     * @param serviceName
     * @return list of control operations
     * @throws AxisFault
     */
    public OperationMetaData[] listControlOperations(String serviceName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        List<OperationMetaData> opMetaDataList = new ArrayList<OperationMetaData>();
        for (Object o : axisService.getControlOperations()) {
            AxisOperation axisOperation = (AxisOperation) o;
            opMetaDataList.add(getOperationMetaData(serviceName,
                                                    axisOperation.getName().getLocalPart()));
        }
        return opMetaDataList.toArray(new OperationMetaData[opMetaDataList.size()]);
    }

    /**
     * list all the published operations (come from servics.xml and wsdl)
     *
     * @param serviceName
     * @return list of published operations
     * @throws AxisFault
     */
    public OperationMetaData[] listPublishedOperations(String serviceName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        List<OperationMetaData> opList = new ArrayList<OperationMetaData>();
        for (Object o : axisService.getPublishedOperations()) {
            AxisOperation axisOperation = (AxisOperation) o;
            opList.add(getOperationMetaData(serviceName,
                                            axisOperation.getName().getLocalPart()));
        }

        return opList.toArray(new OperationMetaData[opList.size()]);
    }

    /**
     * return all accumulated data about this operation
     *
     * @param serviceName
     * @param operationName
     * @return operation stats
     * @throws AxisFault
     */
    public OperationMetaData getOperationStatistics(String serviceName,
                                                    String operationName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        if (axisService != null) {
            return getOperationMetaData(serviceName, operationName);
        }
        return null;
    }

    /**
     * Configuring MTOM
     *
     * @param flag
     * @param serviceName
     * @param operationName
     * @throws AxisFault
     */
    public void configureMTOM(String flag, String serviceName, String operationName)
            throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        AxisOperation operation;
        if (axisService != null) {
            operation = axisService.getOperation(new QName(operationName));
        } else {
            throw new AxisFault("Service " + serviceName + " cannot be found");
        }
        if (operation == null) {
            throw new AxisFault("Operation " + operationName + " in service " + serviceName
                                + " cannot be found");
        }

        ArrayList<Parameter> parameters = operation.getParameters();

        boolean found = false;
        for (Parameter parameter : parameters) {
            if (parameter.getParameterType() == Parameter.TEXT_PARAMETER &&
                parameter.getValue().toString().equals(Constants.Configuration.ENABLE_MTOM)) {
                parameter.setValue(flag.trim());
                found = true;
                break;
            }

        }
        if (!found) {
            Parameter parameter =
                    ParameterUtil.createParameter(Constants.Configuration.ENABLE_MTOM, flag.trim());
            operation.addParameter(parameter);
        }

        Parameter parameter = operation.getParameter(Constants.Configuration.ENABLE_MTOM);
        //At this point parameter will not be null;

        //Persisting the parameter
        //TODO: Handle persistence
        /*OperationParameterDO paramDO =
                opm.getOperationParameter(serviceName,
                                         ServiceIdentifierDO.EMPTY_SERVICE_VERSION,
                                         operationName,
                                         parameter.getName());
        if (paramDO != null) {
            paramDO.setValue(parameter.getParameterElement().toString());
            opm.updateEntity(paramDO);
        } else {
            paramDO = new OperationParameterDO();
            paramDO.setName(parameter.getName());
            paramDO.setValue(parameter.getParameterElement().toString());
            OperationDO opDO = opm.getOperation(serviceName,
                                               ServiceIdentifierDO.EMPTY_SERVICE_VERSION,
                                               operationName);
            paramDO.setOperation(opDO);

            try {
                opm.addEntity(paramDO);
            } catch (DuplicateEntityException e) {
                log.error("Operation Parameter already exists", e);
            }

        }*/

    }

    /**
     * return all parameters for this operation (including inherited ones),
     * where each parameter is an XML fragment representing the "parameter" element
     *
     * @param serviceId
     * @param operationId
     * @return operation params
     * @throws AxisFault
     */
    public String[] getOperationParameters(String serviceId,
                                           String operationId) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceId);
        AxisOperation op = axisService.getOperation(new QName(operationId));

        if (op == null) {
            throw new AxisFault("Invalid operation : " + operationId +
                                " not available in service : " + serviceId);
        }

        ArrayList<String> allParameters = new ArrayList<String>();
        ArrayList globalParameters = getAxisConfig().getParameters();

        for (Object globalParameter : globalParameters) {
            Parameter parameter = (Parameter) globalParameter;
            allParameters.add(parameter.getParameterElement().toString());
        }

        AxisService service = getAxisConfig().getServiceForActivation(serviceId);

        if (service == null) {
            throw new AxisFault("invalid service name");
        }

        ArrayList serviceParams = service.getParameters();

        for (Object serviceParam : serviceParams) {
            Parameter parameter = (Parameter) serviceParam;
            allParameters.add(parameter.getParameterElement().toString());
        }

        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) service.getParent();
        ArrayList serviceGroupParams = axisServiceGroup.getParameters();

        for (Object serviceGroupParam : serviceGroupParams) {
            Parameter parameter = (Parameter) serviceGroupParam;
            allParameters.add(parameter.getParameterElement().toString());
        }

        ArrayList opParams = op.getParameters();

        for (Object opParam : opParams) {
            Parameter parameter = (Parameter) opParam;
            allParameters.add(parameter.getParameterElement().toString());
        }

        Collections.sort(allParameters, new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                return arg0.compareToIgnoreCase(arg1);
            }
        });
        return allParameters.toArray(new String[allParameters.size()]);
    }

    /**
     * return only the parameters for explicitly set for this operation
     * (not including inherited ones), where each parameter is an XML fragment
     * representing the "parameter" element
     *
     * @param serviceName
     * @param operationName
     * @return declared operation params
     * @throws AxisFault
     */
    public String[] getDeclaredOperationParameters(String serviceName,
                                                   String operationName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        AxisOperation op = axisService.getOperation(new QName(operationName));

        if (op == null) {
            throw new AxisFault("Invalid operation : " + operationName +
                                " not available in service : " + serviceName);
        }

        ArrayList<String> parameters = new ArrayList<String>();
        ArrayList opParams = op.getParameters();

        for (Object opParam : opParams) {
            Parameter parameter = (Parameter) opParam;
            OMElement element = parameter.getParameterElement();
            if (element != null) {
                parameters.add(element.toString());
            }
        }

        Collections.sort(parameters, new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                return arg0.compareToIgnoreCase(arg1);
            }
        });
        return parameters.toArray(new String[parameters.size()]);
    }

    public void setOperationParameters(String serviceId,
                                       String operationId,
                                       String[] parameters) throws AxisFault {

        for (String parameter : parameters) {
            setOperationParameter(serviceId, operationId, parameter);
        }
    }

    private void setOperationParameter(String serviceName,
                                       String operationName,
                                       String parameterStr) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);

        if (axisService == null) {
            throw new AxisFault("invalid service name service not found : " + serviceName);
        }

        AxisOperation axisOp = axisService.getOperation(new QName(operationName));

        if (axisOp == null) {
            throw new AxisFault("Invalid operation : " + operationName +
                                " not available in service : " + serviceName);
        }

        OMElement paramEle = null;
        try {
            XMLStreamReader xmlSR =
                    StAXUtils.createXMLStreamReader(new ByteArrayInputStream(parameterStr.getBytes()));
            paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
        } catch (XMLStreamException e) {
            handleException("Cannot create OMElement from parameter: " + parameterStr, e);
        }

        Parameter parameter = ParameterUtil.createParameter(paramEle);
        axisOp.addParameter(parameter);

        try {
            opm.updateOperationParameter(axisOp, parameter);
        } catch (Exception e) {
            String msg = "Cannot persist operation parameter for operation " + axisOp.getName();
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    public void removeOperationParameter(String serviceName,
                                         String operationName,
                                         String parameterName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);

        if (axisService == null) {
            throw new AxisFault("invalid service name service not found : " +
                                serviceName);
        }

        AxisOperation axisOp = axisService.getOperation(new QName(operationName));

        if (axisOp == null) {
            throw new AxisFault("Invalid operation : " + operationName +
                                " not available in service : " + serviceName);
        }

        Parameter parameter = ParameterUtil.createParameter(parameterName, null);
        axisOp.removeParameter(parameter);
        try {
            opm.removeOperationParameter(axisOp, parameter);
        } catch (Exception e) {
            String msg = "Cannot persist operation parameter removal. Operation " +
                         serviceName + ":" + operationName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    /**
     * list all the operation phases
     *
     * @param serviceName
     * @param operationName
     * @param flow
     * @return operation phases
     * @throws AxisFault
     */
    public String[] listOperationPhases(String serviceName,
                                        String operationName,
                                        int flow) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        AxisOperation op = axisService.getOperation(new QName(operationName));

        if (op == null) {
            throw new AxisFault("Invalid operation : " + operationName +
                                " not available in service : " + serviceName);
        }

        String[] phaseNames = null;

        switch (flow) {
            case PhaseMetadata.IN_FLOW: {
                ArrayList inflow = op.getRemainingPhasesInFlow();
                phaseNames = new String[inflow.size()];

                for (int i = 0; i < inflow.size(); i++) {
                    Phase phase = (Phase) inflow.get(i);
                    phaseNames[i] = phase.getPhaseName();
                }

                break;
            }

            case PhaseMetadata.OUT_FLOW: {
                ArrayList inflow = op.getPhasesOutFlow();
                phaseNames = new String[inflow.size()];

                for (int i = 0; i < inflow.size(); i++) {
                    Phase phase = (Phase) inflow.get(i);
                    phaseNames[i] = phase.getPhaseName();
                }

                break;
            }

            case PhaseMetadata.FAULT_IN_FLOW: {
                ArrayList inflow = op.getPhasesInFaultFlow();
                phaseNames = new String[inflow.size()];

                for (int i = 0; i < inflow.size(); i++) {
                    Phase phase = (Phase) inflow.get(i);
                    phaseNames[i] = phase.getPhaseName();
                }

                break;
            }

            case PhaseMetadata.FAULT_OUT_FLOW: {
                ArrayList inflow = op.getPhasesOutFaultFlow();
                phaseNames = new String[inflow.size()];

                for (int i = 0; i < inflow.size(); i++) {
                    Phase phase = (Phase) inflow.get(i);
                    phaseNames[i] = phase.getPhaseName();
                }

                break;
            }
        }

        return phaseNames;
    }

    /**
     * To list handlers in a given operation phases
     *
     * @param serviceName
     * @param operationName
     * @param flow
     * @param phaseName
     * @return operation phase handlers
     * @throws AxisFault
     */
    public String[] listOperationPhaseHandlers(String serviceName,
                                               String operationName,
                                               int flow,
                                               String phaseName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        AxisOperation op = axisService.getOperation(new QName(operationName));

        if (op == null) {
            throw new AxisFault("Invalid operation : " + operationName +
                                " not available in service : " + serviceName);
        }

        String[] handlers = null;

        switch (flow) {
            case PhaseMetadata.IN_FLOW: {
                ArrayList inflow = op.getRemainingPhasesInFlow();

                for (Object anInflow : inflow) {
                    Phase phase = (Phase) anInflow;

                    if (phase.getPhaseName().equals(phaseName)) {
                        handlers = new String[phase.getHandlerCount()];

                        List hands = phase.getHandlers();

                        for (int j = 0; j < hands.size(); j++) {
                            Handler handler = (Handler) hands.get(j);
                            handlers[j] = handler.getName();
                        }
                    }
                }

                break;
            }

            case PhaseMetadata.OUT_FLOW: {
                ArrayList inflow = op.getPhasesOutFlow();

                for (Object anInflow : inflow) {
                    Phase phase = (Phase) anInflow;

                    if (phase.getPhaseName().equals(phaseName)) {
                        handlers = new String[phase.getHandlerCount()];

                        List hands = phase.getHandlers();

                        for (int j = 0; j < hands.size(); j++) {
                            Handler handler = (Handler) hands.get(j);
                            handlers[j] = handler.getName();
                        }
                    }
                }

                break;
            }

            case PhaseMetadata.FAULT_IN_FLOW: {
                ArrayList inflow = op.getPhasesInFaultFlow();

                for (Object anInflow : inflow) {
                    Phase phase = (Phase) anInflow;

                    if (phase.getPhaseName().equals(phaseName)) {
                        handlers = new String[phase.getHandlerCount()];

                        List hands = phase.getHandlers();

                        for (int j = 0; j < hands.size(); j++) {
                            Handler handler = (Handler) hands.get(j);
                            handlers[j] = handler.getName();
                        }
                    }
                }

                break;
            }

            case PhaseMetadata.FAULT_OUT_FLOW: {
                ArrayList inflow = op.getPhasesOutFaultFlow();

                for (Object anInflow : inflow) {
                    Phase phase = (Phase) anInflow;

                    if (phase.getPhaseName().equals(phaseName)) {
                        handlers = new String[phase.getHandlerCount()];

                        List hands = phase.getHandlers();

                        for (int j = 0; j < hands.size(); j++) {
                            Handler handler = (Handler) hands.get(j);
                            handlers[j] = handler.getName();
                        }
                    }
                }

                break;
            }
        }

        return handlers;
    }
    
    public OperationMetaData getOperationMetaData(String serviceName,
                                                  String operationName) throws AxisFault {
        AxisService axisService = getAxisConfig().getServiceForActivation(serviceName);
        AxisOperation axisOperation = null;
        if (axisService != null) {
            axisOperation = axisService.getOperation(new QName(operationName));
        }
        if (axisOperation == null) {
            return null;
        }
        OperationMetaData opMetaData = new OperationMetaData();
        opMetaData.setName(axisOperation.getName().getLocalPart());
        opMetaData.setControlOperation(axisOperation.isControlOperation());
        Parameter parameter = axisOperation.getParameter(Constants.Configuration.ENABLE_MTOM);
        if (parameter != null) {
            opMetaData.setEnableMTOM((String) parameter.getValue());
        } else {
            opMetaData.setEnableMTOM("false");
        }
        return opMetaData;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
