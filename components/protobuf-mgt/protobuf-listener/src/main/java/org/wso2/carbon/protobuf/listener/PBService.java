/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.protobuf.listener;

/*
 * This bean class keeps information of a PB service.
 * We need these information when we remove a PB service from Binary Service Registry.
 * 
 * serviceName - Name of the service implementation (not service definition)
 * serviceType - whether it is a blocking service or non blocking service
 * 
 */
public class PBService {
    private String serviceName;
    private String serviceType;

    public PBService(String serviceName, String serviceType) {

        this.serviceName = serviceName;
        this.serviceType = serviceType;

    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getServiceType() {
        return serviceType;
    }
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
