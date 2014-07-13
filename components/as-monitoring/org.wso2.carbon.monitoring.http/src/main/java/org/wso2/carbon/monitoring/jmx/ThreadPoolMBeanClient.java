package org.wso2.carbon.monitoring.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;

public class ThreadPoolMBeanClient extends MBeanClient {

	private static final String THREAD_POOL_NAME = "Catalina:type=ThreadPool,name=*";

	private static final String[] THREAD_POOL_ATTRIBUTES = { "connectionCount", "currentThreadCount", "currentThreadsBusy", "keepAliveCount" };

	@Override
	protected String getObjectNameQuery() {
		return THREAD_POOL_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return THREAD_POOL_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		return new AttributeList(0);
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		String name = objectName.getKeyProperty("name");

		// the name is like "http-nio-9443". This name contains 9443 even when the actual running port is 9444.
		// The GlobalRequestProcessor & Connector Beans show the same behavior.
		// but the connector is just having a key property called "port" with the value 9443
		// So we will rip off the http-nio- part and just get the last part as the key.
		// This key is using for correlation purposes only

		if (name != null) {
			String[] parts = name.split("-");
			if (parts.length > 0) {
				//remove the last " too.
				return parts[parts.length - 1].replace("\"", "");
			}
		}

		return null;
	}
}
