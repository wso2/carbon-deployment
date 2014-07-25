/*
 * Copyright 2004,2013 The Apache Software Foundation.
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

package org.wso2.carbon.monitoring.stat.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringEvent;
import org.wso2.carbon.monitoring.stat.jmx.Result;

import javax.management.Attribute;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * The Utility class that contains some of the functions that are used accross the entire module
 */
public class CollectorUtil {

	private Log log;

	/**
	 * Create a Collector Util with its own Logger
	 */
	public CollectorUtil() {
		log = LogFactory.getLog(CollectorUtil.class);
	}

	/**
	 * Create CollectorUtil and set a Logger
	 * @param log The Logger to be used
	 */
	public CollectorUtil(Log log) {
		this.log = log;
	}

	/**
	 * Get a particular Result object which maps to the given correlator
	 * @param results the list of results
	 * @param correlator the correlator
	 * @return the Result matching to the given correlator
	 */
	public Result getResultByCorrelator(List<Result> results, String correlator) {
		for (Result result : results) {
			if (correlator.equals(result.getCorrelator())) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Map the Attributes on the result to the given event Object using reflection
	 * @param result The Result object with the attribute values
	 * @param event The POJO to be updated with the attribute values.
	 */
	public void mapResultAttributesToPoJo(Result result, Object event) {
		List<Attribute> attributeList = result.getAttributes().asList();
		for (Attribute attribute : attributeList) {
			setFieldValue(event, attribute);
		}
	}

	/**
	 * Feed the serverAddress, serverName, clusterDomain and ClusterSubDomain values to the event.
	 * @param event
	 */
	public void mapMetaData(MonitoringEvent event) {
		String serverAddress = "-";
		String serverName = "-";

		try {
			InetAddress ip = InetAddress.getLocalHost();
			serverAddress = ip.getHostAddress();
			serverName = ip.getHostName();
		} catch (UnknownHostException ignored) {

		}

		event.setServerAddress(serverAddress);
		event.setServerName(serverName);
		event.setClusterDomain(getClusterDomain());
		event.setClusterSubDomain(getClusterSubDomain());
	}

	/**
	 * Set the value of the attribute to the matching property of the event Object.
	 * @param event
	 * @param attribute
	 */
	public void setFieldValue(Object event, Attribute attribute) {
		Class<?> clazz = event.getClass();
		Field field;
		try {
			field = clazz.getDeclaredField(attribute.getName());
		} catch (NoSuchFieldException e) {
			log.warn(attribute.getName() + " not found as a field", e);
			return;
		}

		try {
			field.setAccessible(true);
			if (field.getType().equals(attribute.getValue().getClass())) {
				field.set(event, attribute.getValue());
			} else {
				log.warn("Type mismatch occurred. field = " + field.getName() + " expected = " + field.getType() + ", found = " + attribute.getValue().getClass());
			}
		} catch (IllegalAccessException e) {
			log.warn(field.getName() + " accessing failed.", e);
		}
	}

	private String getClusterDomain() {
		return "-";
	}

	private String getClusterSubDomain() {
		return "-";
	}
}
