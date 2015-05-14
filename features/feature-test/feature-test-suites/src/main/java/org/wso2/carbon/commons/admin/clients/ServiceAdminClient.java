/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.commons.admin.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.commons.utils.AuthenticateStubUtil;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyServicesWrapper;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

/**
 * Provides client to invoke ServiceAdmin admin service.
 * Can be used to manage axis2 services.
 */
public class ServiceAdminClient {

    private static final Log log = LogFactory.getLog(ServiceAdminClient.class);
    private ServiceAdminStub serviceAdminStub;
    private int pageNumber = 0;

    public ServiceAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String serviceName = "ServiceAdmin";
        String endPoint = backEndUrl + serviceName;
        serviceAdminStub = new ServiceAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, serviceAdminStub);
    }

    /**
     * This method is to delete one or more services
     *
     * @param serviceGroups - services which has to be deleted
     * @throws RemoteException - Error when deleting service group
     */
    public void deleteService(String[] serviceGroups) throws RemoteException {
        serviceAdminStub.deleteServiceGroups(serviceGroups);
    }

    /**
     * Delete faulty service
     *
     * @param serviceName - Service name to be deleted
     * @return boolean - deleted or not
     * @throws RemoteException - Error when deleting faulty service group
     */
    public boolean deleteFaultyServiceByServiceName(String serviceName) throws RemoteException {
        boolean isFaultyServiceDeleted = false;
        FaultyService faultyService =  getFaultyData(serviceName);
        if(faultyService != null){
            isFaultyServiceDeleted =
                    serviceAdminStub.deleteFaultyServiceGroup(faultyService.getArtifact());
        }
        return isFaultyServiceDeleted;
    }

    /**
     * Get all the services for given service name
     *
     * @param serviceName - service name to search
     * @return ServiceMetaDataWrapper - service list
     * @throws RemoteException - Error when list services.
     */
    public ServiceMetaDataWrapper listServices(String serviceName) throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceName, pageNumber);
        serviceAdminStub.getFaultyServiceArchives(pageNumber);
        return serviceMetaDataWrapper;
    }

    /**
     * Get all the faulty services
     *
     * @return FaultyServicesWrapper - FaultyServicesWrapper contain fault metadata
     * @throws RemoteException - Error when getting faulty service archives
     */
    public FaultyServicesWrapper listFaultyServices() throws RemoteException {
        FaultyServicesWrapper faultyServicesWrapper;
        faultyServicesWrapper = serviceAdminStub.getFaultyServiceArchives(pageNumber);
        return faultyServicesWrapper;
    }

    /**
     * Check whether service is available or not
     *
     * @param serviceName - service name
     * @return boolean - service is available or not
     * @throws RemoteException - Error when list services.
     */
    public boolean isServiceExists(String serviceName) throws RemoteException {
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
                    serviceState = true;
                    break;
                }
            }
        }
        return serviceState;
    }

    /**
     * Get service group by service name
     *
     * @param serviceName - service name
     * @return - service group
     * @throws RemoteException - Error when list services.
     */
    public String getServiceGroup(String serviceName) throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList != null) {
            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equals(serviceName)) {
                    return serviceData.getServiceGroupName();
                }
            }
        }
        return null;
    }

    /**
     * Check this service is faulty or not
     *
     * @param serviceName -  service name
     * @return boolean - faulty or not
     * @throws RemoteException - Error when getting all faulty services
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
                        serviceState = true;
                        break;
                    }
                }
            }
        }
        return serviceState;
    }

    /**
     * Get a faulty service data by service name
     *
     * @param serviceName - service name
     * @return FaultyService - faulty service data
     * @throws RemoteException - Error when getting all faulty services
     */
    public FaultyService getFaultyData(String serviceName) throws RemoteException {
        FaultyService faultyService = null;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices();
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList != null ) {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        faultyService = faultyServiceData;
                    }
                }
            }
        }
        return faultyService;
    }
}
