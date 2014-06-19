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

import com.google.protobuf.BlockingService;
import com.google.protobuf.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.protobuf.annotation.ProtoBufService;
import org.wso2.carbon.protobuf.listener.servlet.ProtoBufServlet;
import org.wso2.carbon.protobuf.registry.BinaryServiceRegistry;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * This class registers PB services into Binary Services Registry.
 * It will listen for an annotation (@Protobuf) and register services when
 * corresponding wars are deployed
 */

@HandlesTypes({ ProtoBufService.class })
public class ProtoBufServletContextInitializer implements ServletContainerInitializer {

	private static final Log log = LogFactory.getLog(ProtoBufServletContextInitializer.class);

	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {

		if (classes == null || classes.size() == 0) {
			return;
		}

		// adding a listener to remove services when wars are undeployed
		servletContext.addListener(new ProtobufServletContextListener());

		// keeps track of PB services in a PB war
		// Please note that, a PB war can contain many PB services
		List<PBService> serviceList = new ArrayList<PBService>();

		// servlet to display proto files (like WSDL files)
		ServletRegistration.Dynamic dynamic = servletContext.addServlet("ProtoBufServlet", ProtoBufServlet.class);

		for (Class<?> clazz : classes) {
			
			// Getting binary service registry
			BinaryServiceRegistry binaryServiceRegistry = (BinaryServiceRegistry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(BinaryServiceRegistry.class);
			
			// Is it a blocking service or not
			boolean blocking = clazz.getAnnotation(ProtoBufService.class).blocking();
			
			Method myMethod = null;
			Object obj = null;

			try {

				if (blocking) {

					//getting newReflectiveBlocking method which will return a blocking service
					myMethod = clazz.getInterfaces()[0].getDeclaringClass().getMethod("newReflectiveBlockingService", clazz.getInterfaces()[0]);

					// Since it is a static method, we pass null
					obj = myMethod.invoke(null, clazz.newInstance());

					BlockingService blockingService = (BlockingService) obj;

					//register service into Binary Service Registry
					String serviceName = binaryServiceRegistry.registerBlockingService(blockingService);
					String serviceType = "BlockingService";

					//keeps PB service information in a bean
					//we need these when removing the services from Binary Service Registry
					//we are using these beans instances inside our destroyer
					serviceList.add(new PBService(serviceName, serviceType));
					servletContext.setAttribute("services", serviceList);

					dynamic.addMapping("/");

				} else {

					//getting newReflectiveService which will return a non blocking service
					myMethod = clazz.getInterfaces()[0].getDeclaringClass().getMethod("newReflectiveService", clazz.getInterfaces()[0]);

					// Since it is a static method, we pass null
					obj = myMethod.invoke(null, clazz.newInstance());

					Service service = (Service) obj;

					//register service into Binary Service Registry
					String serviceName = binaryServiceRegistry.registerService(service);
					String serviceType = "NonBlockingService";

					//keeps PB service information in a bean
					//we need these information to remove the service from Binary Service Registry later
					//we are using these bean instances in our destroyer
					serviceList.add(new PBService(serviceName, serviceType));
					servletContext.setAttribute("services", serviceList);

					dynamic.addMapping("/");

				}

			} catch (InvocationTargetException e) {
				String msg = "InvocationTargetException" + e.getLocalizedMessage();
				log.info(msg);
			} catch (NoSuchMethodException e) {
				String msg = "NoSuchMethodException" + e.getLocalizedMessage();
				log.info(msg);
			} catch (InstantiationException e) {
				String msg = "InstantiationException" + e.getLocalizedMessage();
				log.info(msg);
			} catch (IllegalAccessException e) {
				String msg = "IllegalAccessException" + e.getLocalizedMessage();
				log.info(msg);
			}

		}
	}
}