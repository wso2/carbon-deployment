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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.protobuf.registry.BinaryServiceRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * This class will remove services from Binary Services Registry when PB wars
 * are undeployed.
 */
public class ProtobufServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

		ServletContext servletContext = servletContextEvent.getServletContext();

		// getting Binary Service Registry from OSGI run time
		BinaryServiceRegistry binaryServiceRegistry = (BinaryServiceRegistry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(BinaryServiceRegistry.class);

		// getting all the services for the corresponding servlet context
		// Please note that, a PB war can contain many PB services
		// Therefore we should remove all of them when the war is undeployed
		ArrayList<PBService> serviceList = (ArrayList<PBService>) servletContext.getAttribute("services");

		for (Iterator iterator = serviceList.iterator(); iterator.hasNext();) {

			// getting service information from PBService bean
			PBService pbService = (PBService) iterator.next();

			String serviceName = pbService.getServiceName();
			String serviceType = pbService.getServiceType();

			// if PB service is a blocking service
			if (serviceType.equals("BlockingService")) {

				binaryServiceRegistry.removeBlockingService(serviceName);

			} else if (serviceType.equals("NonBlockingService")) {

				binaryServiceRegistry.removeService(serviceName);
			}
		}
	}

}