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

package org.wso2.carbon.protobuf.esb.mediator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.protobuf.client.BinaryServiceClient;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.format.XmlFormat;
import com.googlecode.protobuf.format.XmlFormat.ParseException;

/*
 * This mediator,
 * accepts a standard XML message
 * load corresponding PB stub (for the very 1st request only)
 * convert XML to PB
 * build the request
 * communicate with Binary Services Server running on WSO2 AS
 * get the response
 * convert PB to XML
 * hands over the standard XML message to the next mediator.
 */

public class ProtoBufMediator extends AbstractMediator {

	private static Logger log = LoggerFactory.getLogger(ProtoBufMediator.class);

	// hold all the proto stubs which are loaded from components/lib directory
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Class>> serviceStubs = new ConcurrentHashMap<String, ConcurrentHashMap<String, Class>>();

	// These three properties are must in order to find a correct PB method to
	// call.
	private String stub;
	private String service;
	private String method;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String m) {
		method = m;
	}

	public String getStub() {
		return stub;
	}

	public void setStub(String stub) {
		this.stub = stub;
	}

	public boolean mediate(MessageContext mc) {

		// get binary service client which provides TCP channel to connect with
		// back end RPC server
		BinaryServiceClient binaryServiceClient = null;
		try {
			binaryServiceClient = (BinaryServiceClient) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(BinaryServiceClient.class);
		} catch (NullPointerException e) {
			String msg = "Binary Service Client is not running. Make sure back end Binary Server is running and corresponding ports in your pbs xml";
			log.debug(msg);
			handleException(msg, mc);
		}

		// in which stub service resides?
		String stubName = this.getStub();

		if (stubName == null || stubName.isEmpty()) {
			String msg = "Property stub is not set in synapse configuration";
			log.debug(msg);
			handleException(msg, mc);
		}

		// what service?
		String serviceName = this.getService();

		if (serviceName == null || serviceName.isEmpty()) {
			String msg = "Property service is not set in synapse configuration";
			log.debug(msg);
			handleException(msg, mc);
		}

		// what action?
		String action = this.getMethod();

		if (action == null || action.isEmpty()) {
			String msg = "Property SOAPAction is not set in synapse configuration";
			log.debug(msg);
			handleException(msg, mc);
		}

		// what is the message name (in proto def) ?
		String messageName = mc.getEnvelope().getBody().getFirstElement().getLocalName();

		if (messageName == null || messageName.isEmpty()) {
			String msg = "Message element not found";
			log.debug(msg);
			handleException(msg, mc);
		}

		// what is the message in xml format?
		String xmlMessage = mc.getEnvelope().getBody().getFirstElement().toString();

		if (xmlMessage == null || messageName.isEmpty()) {
			String msg = "Message not found";
			log.debug(msg);
			handleException(msg, mc);
		}

		try {

			// load stub if not already loaded
			if (!serviceStubs.containsKey(stubName)) {

				// load stub
				Class stub = this.getClass().getClassLoader().loadClass(stubName);
				Class[] classes = stub.getDeclaredClasses();

				// store all declared classes with class names
				ConcurrentHashMap<String, Class> stubClasses = new ConcurrentHashMap<String, Class>();

				for (Class clazz : classes) {
					stubClasses.put(clazz.getSimpleName(), clazz);
				}

				// store stub with all declared classes
				serviceStubs.put(stubName, stubClasses);
			}

			// get current request's stub
			ConcurrentHashMap<String, Class> currentStub = serviceStubs.get(stubName);

			// which service?
			Class serviceClass = currentStub.get(serviceName);
			// which message (in proto jargon) ?
			Class messageClass = currentStub.get(messageName);

			// get newBuilder() which will return a builder to build requests
			Method newBuilder = messageClass.getDeclaredMethod("newBuilder", null);
			Builder obj = (Builder) newBuilder.invoke(null, null);
			Message.Builder builder = obj;

			// xml to pb
			XmlFormat.merge(xmlMessage, builder);
			Message request = builder.build();

			// get newBlockingStub method which will create blocking services
			Method newBlockingStub = serviceClass.getMethod("newBlockingStub", BlockingRpcChannel.class);

			newBlockingStub.setAccessible(true);

			// get a blocking service version of the service
			Object blockingService = newBlockingStub.getReturnType().cast(newBlockingStub.invoke(null, binaryServiceClient.getRpcChannel()));

			// invoke the required method on the service
			Method method = blockingService.getClass().getDeclaredMethod(action, RpcController.class, messageClass);

			method.setAccessible(true);

			Object response = method.getReturnType().cast(method.invoke(blockingService, binaryServiceClient.getRpcController(), method.getParameterTypes()[1].cast(request)));

			// pb to xml
			OMElement element = AXIOMUtil.stringToOM(XmlFormat.printToString((Message) response));

			// removing request
			mc.getEnvelope().getBody().getFirstOMChild().detach();

			// adding response
			mc.getEnvelope().getBody().addChild(element);

		} catch (InvocationTargetException e) {
			String msg = "InvocationTargetException";
			handleException(msg, e, mc);
		} catch (NoSuchMethodException e) {
			String msg = "NoSuchMethodException";
			handleException(msg, e, mc);
		} catch (SecurityException e) {
			String msg = "SecurityException";
			handleException(msg, e, mc);
		} catch (ParseException e) {
			String msg = "ParseException";
			handleException(msg, e, mc);
		} catch (IllegalAccessException e) {
			String msg = "IllegalAccessException";
			handleException(msg, e, mc);
		} catch (IllegalArgumentException e) {
			String msg = "IllegalArgumentException";
			handleException(msg, e, mc);
		} catch (ClassNotFoundException e) {
			String msg = "ClassNotFoundException";
			handleException(msg, e, mc);
		} catch (XMLStreamException e) {
			String msg = "XMLStreamException";
			handleException(msg, e, mc);;
		}

		return true;
	}
}