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
package org.wso2.carbon.jarservices;

/**
 * This util class holds the information of Service
 */
public class Service {
    private String className;
    private String serviceName;
    private String deploymentScope;
    private boolean useOriginalWsdl;
    private Operation[] operations;

    public Service() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDeploymentScope() {
        return deploymentScope;
    }

    public void setDeploymentScope(String deploymentScope) {
        this.deploymentScope = deploymentScope;
    }

    public boolean isUseOriginalWsdl() {
        return useOriginalWsdl;
    }

    public void setUseOriginalWsdl(boolean useOriginalWsdl) {
        this.useOriginalWsdl = useOriginalWsdl;
    }
}
