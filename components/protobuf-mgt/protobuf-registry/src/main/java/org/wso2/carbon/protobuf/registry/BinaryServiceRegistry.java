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

package org.wso2.carbon.protobuf.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Service;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;

/*
 * This class provides APIs to register and remove services from Binary Service Registry.
 * 
 * Any class can get an instance of this class from OSGI run time and use it to rgister/remove services
 * 
 */
public class BinaryServiceRegistry {
	
	private static final Log log = LogFactory.getLog(BinaryServiceRegistry.class);

	private DuplexTcpServerPipelineFactory serverFactory;

	BinaryServiceRegistry(DuplexTcpServerPipelineFactory serverFactory) {

		this.serverFactory = serverFactory;
	}

	public String registerBlockingService(BlockingService blockingService) {
		
		String serviceName = blockingService.getDescriptorForType().getFullName();
		
		if (serverFactory.getRpcServiceRegistry().resolveService(serviceName) == null) {
			
			serverFactory.getRpcServiceRegistry().registerService(blockingService);
			
			return blockingService.getDescriptorForType().getFullName();
			
        } else {
        	
        	String msg = "Duplicate Service " + serviceName;
        	log.info(msg);
        	return null;
        }


	}

	public String registerService(Service service) {
		
		String serviceName = service.getDescriptorForType().getFullName();
		
		if (serverFactory.getRpcServiceRegistry().resolveService(serviceName) == null) {
	        
			serverFactory.getRpcServiceRegistry().registerService(service);
			
			return service.getDescriptorForType().getFullName();
			
        } else {
        	
        	String msg = "Duplicate Service " + serviceName;
        	log.info(msg);
        	return null;
        }


	}
	
	public String removeBlockingService(String serviceName) {
		
		try {
			
			BlockingService blockingService = serverFactory.getRpcServiceRegistry().resolveService(serviceName).getBlockingService();
			
			serverFactory.getRpcServiceRegistry().removeService(blockingService);
			
			return blockingService.getDescriptorForType().getFullName();
			
        } catch (NullPointerException e) {
        	
        	String msg = serviceName + " not found";
        	log.info(msg);
        }
		
		return null;

	}

	public String removeService(String serviceName) {
		
		try {
			
			Service service = serverFactory.getRpcServiceRegistry().resolveService(serviceName).getService();

			serverFactory.getRpcServiceRegistry().removeService(service);
			
			return service.getDescriptorForType().getFullName();
	        
        } catch (NullPointerException e) {
        	
        	String msg = serviceName + " not found";
        	log.info(msg);
        }
		
		return null;
		
	}


}