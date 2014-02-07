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
package org.wso2.carbon.jarservices.ui.fileupload;

import org.wso2.carbon.jarservices.stub.types.Service;
import org.wso2.carbon.jarservices.stub.types.Operation;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 */
public class Utils {
    
    public static Service[] getServices2(HttpServletRequest request, String paramName) {
        Map<String, Service> services = new HashMap<String, Service>();
        String[] methods = request.getParameterValues(paramName);
        for (String method : methods) { // service_class#service_name#deployment_scope#use_original_wsdl#operation_name
            String[] strings = method.split("#");
            String className = strings[0];
            Service service = services.get(className);
            if (service == null) {
                service = new Service();
                service.setClassName(strings[0]);
                service.setDeploymentScope(strings[2]);
                service.setServiceName(strings[1]);
                service.setUseOriginalWsdl(Boolean.valueOf(strings[3]));
                services.put(className, service);
            }
            if (strings.length == 5) {
                Operation operation = new Operation();
                operation.setOperationName(strings[4]);
                Operation[] operations = service.getOperations();
                if (operations != null && operations.length > 0) {
                    Operation[] newOperations = new Operation[operations.length + 1];
                    System.arraycopy(operations, 0, newOperations, 0, operations.length);
                    Operation newOperation = new Operation();
                    newOperation.setOperationName(strings[4]);
                    newOperations[newOperations.length -1] = newOperation;
                    operations = newOperations;
                } else {
                    operations = new Operation[1];
                    operations[0] = new Operation();
                    operations[0].setOperationName(strings[4]);
                }
                service.setOperations(operations);
            }
        }
        return services.values().toArray(new Service[services.size()]);
    }

    public static Service[] getServices(HttpServletRequest request, String paramName) {
        List<Service> dataList = new ArrayList<Service>();
        String[] parameterValues = request.getParameterValues(paramName);
        if (parameterValues != null) {
            for (String param : parameterValues) {
                if(param.equals("")){
                    continue;
                }
                String[] strings = param.split("#");
                Service service = new Service();
                service.setClassName(strings[0]);
                service.setServiceName(strings[1]);
                service.setDeploymentScope(strings[2]);
                service.setUseOriginalWsdl(Boolean.valueOf(strings[3]));
                dataList.add(service);
            }
        }
        return dataList.toArray(new Service[dataList.size()]);
    }
}
