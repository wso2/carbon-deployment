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
 * This is the base class for all the other MBean attribute readers.
 */
public abstract class MBeanClient {

	private static Log log = LogFactory.getLog(MBeanClient.class);

	private static final String JSP_MONITOR_NAME = "Catalina:type=JspMonitor,name=jsp,WebModule=*,J2EEApplication=*,J2EEServer=*";

	private static final String SERVLET_NAME = "Catalina:j2eeType=Servlet,name=*,WebModule=*,J2EEApplication=*,J2EEServer=*";

	private static final String[] JSP_MONITOR_ATTRIBUTES = { "jspCount", "jspReloadCount", "jspUnloadCount" };

	private static MBeanServer server;

	protected abstract String getObjectNameQuery();

	protected abstract String[] getAttributeNames();

	protected abstract AttributeList getPropertiesFromKey(ObjectName objectName);

	public abstract String getCorrelationKey(ObjectName objectName);

	public MBeanClient() {
		server = ManagementFactory.getPlatformMBeanServer();
	}

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
