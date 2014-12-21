/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.commons;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyServicesWrapper;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

/**
 * This class is for managing all service operations
 */
public class ServiceAdminClient {

    private static final Log log = LogFactory.getLog(ServiceAdminClient.class);
    private final String serviceName = "ServiceAdmin";
    private ServiceAdminStub serviceAdminStub;


    public ServiceAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        serviceAdminStub = new ServiceAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, serviceAdminStub);
    }

    /**
     * This method is to delete one or more services
     * @param serviceGroup - services which has to be deleted
     * @throws RemoteException
     */
    public void deleteService(String[] serviceGroup) throws RemoteException {

        serviceAdminStub.deleteServiceGroups(serviceGroup);

    }


    /**
     * Delete faulty service
     * @param serviceName - Service name to be deleted
     * @return boolean - deleted or not
     * @throws RemoteException
     */
    public boolean deleteFaultyServiceByServiceName(String serviceName) throws RemoteException {
        try {
            return serviceAdminStub.deleteFaultyServiceGroup(getFaultyData(serviceName).getArtifact());
        } catch (RemoteException e) {
            log.error("Faulty service deletion fails", e);
            throw new RemoteException("Faulty service deletion fails", e);
        }
    }


    /**
     * Get all the services for a service name
     * @param serviceName - service name to search
     * @return ServiceMetaDataWrapper - service list
     * @throws RemoteException
     */
    public ServiceMetaDataWrapper listServices(String serviceName)
            throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceName, 0);
        serviceAdminStub.getFaultyServiceArchives(0);
        return serviceMetaDataWrapper;
    }


    /**
     * Get all the faulty services
     * @return FaultyServicesWrapper - all faulty services.
     * @throws RemoteException
     */
    public FaultyServicesWrapper listFaultyServices() throws RemoteException {
        FaultyServicesWrapper faultyServicesWrapper;

        faultyServicesWrapper = serviceAdminStub.getFaultyServiceArchives(0);

        return faultyServicesWrapper;
    }


    /**
     * Check whether service is available or not
     * @param serviceName - service name
     * @return - service is available or not
     * @throws RemoteException
     */
    public boolean isServiceExists(String serviceName)
            throws RemoteException {
        boolean serviceState = false;
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList == null || serviceMetaDataList.length == 0) {
            serviceState = false;
        } else {
            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equalsIgnoreCase(serviceName)) {
                    return true;
                }
            }
        }
        return serviceState;
    }


    /**
     * Get service group by service name
     * @param serviceName - service name
     * @return
     * @throws RemoteException
     */
    public String getServiceGroup(String serviceName) throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {
            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equalsIgnoreCase(serviceName)) {
                    return serviceData.getServiceGroupName();
                }
            }
        }
        return null;
    }


    /**
     * Check this service is faulty or not
     * @param serviceName -  service name
     * @return boolean - faulty or nor
     * @throws RemoteException
     */
    public boolean isServiceFaulty(String serviceName) throws RemoteException {
        boolean serviceState = false;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices();
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList == null || faultyServiceList.length == 0) {
                serviceState = false;
            } else {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }
        return serviceState;
    }

    /**
     * Get a faulty service data by service name
     * @param serviceName - service name
     * @return FaultyService - faulty service data
     * @throws RemoteException
     */
    public FaultyService getFaultyData(String serviceName) throws RemoteException {
        FaultyService faultyService = null;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices();
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList == null || faultyServiceList.length == 0) {
                throw new RuntimeException("Service not found in faulty service list");
            } else {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        faultyService = faultyServiceData;
                    }
                }
            }
        }
        if (faultyService == null) {
            throw new RuntimeException("Service not found in faulty service list " + faultyService);
        }
        return faultyService;
    }
}
