package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * Created by chamil on 7/3/14.
 */
public class GlobalRequestProcessorMBeanClient extends MBeanClient {

	private static final String GLOBAL_CONTEXT_LISTENER_NAME = "Catalina:type=GlobalRequestProcessor,name=*";
	private static final String[] GLOBAL_CONTEXT_LISTENER_ATTRIBUTES = { "bytesSent", "bytesReceived", "errorCount", "processingTime", "requestCount" };

	@Override
	protected String getObjectNameQuery() {
		return GLOBAL_CONTEXT_LISTENER_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return GLOBAL_CONTEXT_LISTENER_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		// No attributes required from the ObjectName
		return new AttributeList();
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		String name = objectName.getKeyProperty("name");

		// the name is like http-nio-9443. This name contains 9443 even when the actual running port is 9444.
		// The ThreadPool & Connector Beans show the same behavior.
		// but the connector is just having a key property called "port" with the value 9443
		// So we will rip off the http-nio part and just get the last part as the key.
		// This key is using for correlation purposes only

		if (name != null) {
			String[] parts = name.split("-");
			if (parts.length > 0) {
				return parts[parts.length - 1].replace("\"","");
			}
		}

		return null;
	}
}
