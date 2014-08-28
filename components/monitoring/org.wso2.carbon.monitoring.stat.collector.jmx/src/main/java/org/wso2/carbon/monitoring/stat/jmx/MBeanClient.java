/*
 * Copyright 2004,2014 The Apache Software Foundation.
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

package org.wso2.carbon.monitoring.stat.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This is the base class for all the other MBean attribute readers. The subclasses can simply define
 * the attribute list, MBean name query & write the logic to extract any specific values from the
 * ObjectName.
 */
public abstract class MBeanClient {

	private static Log log = LogFactory.getLog(MBeanClient.class);

	private static final String JSP_MONITOR_NAME = "Catalina:type=JspMonitor,name=jsp,WebModule=*,J2EEApplication=*,J2EEServer=*";

	private static final String SERVLET_NAME = "Catalina:j2eeType=Servlet,name=*,WebModule=*,J2EEApplication=*,J2EEServer=*";

	private static final String[] JSP_MONITOR_ATTRIBUTES = { "jspCount", "jspReloadCount", "jspUnloadCount" };

	private static MBeanServer server;

	/**
	 * Get the ObjectName query which may contains wildcards which will be used to query all the MBeans
	 * That matches to that query.
	 *
	 * @return the ObjectName query.
	 */
	protected abstract String getObjectNameQuery();

	/**
	 * List of attribute names that should be read from the MBeans
	 *
	 * @return Array of attribute names
	 */
	protected abstract String[] getAttributeNames();

	/**
	 * get the Attributes from the ObjectName of the MBean
	 *
	 * @param objectName The ObjectName of the MBean
	 * @return List of Attributes
	 */
	protected abstract AttributeList getPropertiesFromKey(ObjectName objectName);

	/**
	 * generate a correlation key to correlate these data with the other MBean data
	 *
	 * @param objectName
	 * @return
	 */
	public abstract String getCorrelationKey(ObjectName objectName);

	/**
	 * Constructs MBean Client
	 */
	public MBeanClient() {
		server = ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Read the attribute values from the MBean
	 *
	 * @return List of Result objects
	 */
	public List<Result> readAttributeValues() {
		final Set<ObjectInstance> instances = getObjectInstancesFor(getObjectNameQuery());
		List<Result> results = new ArrayList<Result>();
		for (ObjectInstance instance : instances) {
			ObjectName objectName = instance.getObjectName();

			AttributeList attributes = getAttributeValues(objectName, getAttributeNames());
			attributes.addAll(getPropertiesFromKey(objectName));
			Result result = new Result(getCorrelationKey(objectName), attributes);
			results.add(result);
		}

		return results;
	}

	private Set<ObjectInstance> getObjectInstancesFor(String objectName) {
		ObjectName name;
		try {
			name = new ObjectName(objectName);
		} catch (MalformedObjectNameException e) {
			log.warn("The ObjectName [" + objectName + "] is wrong - ", e);
			return Collections.emptySet();
		}
		return server.queryMBeans(name, null);
	}

	private AttributeList getAttributeValues(ObjectName objectName, String[] attributes) {
		assert attributes != null && objectName != null;

		try {
			return server.getAttributes(objectName, attributes);
		} catch (InstanceNotFoundException e) {
			log.warn("The instance [" + objectName + "] not found.", e);
		} catch (ReflectionException e) {
			log.warn("Exception occurred while reading the attributes from " + objectName, e);
		}

		return new AttributeList(0);

	}
}
