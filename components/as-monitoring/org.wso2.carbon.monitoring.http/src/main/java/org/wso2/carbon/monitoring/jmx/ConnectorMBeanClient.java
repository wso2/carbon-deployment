package org.wso2.carbon.monitoring.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;

public class ConnectorMBeanClient extends MBeanClient {

	private static final String CONNECTOR_NAME = "Catalina:type=Connector,port=*";
	private static final String[] CONNECTOR_ATTRIBUTES = { "port", "scheme" };

	@Override
	protected String getObjectNameQuery() {
		return CONNECTOR_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return CONNECTOR_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		//Nothing to extract
		return new AttributeList();
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		return objectName.getKeyProperty("port");
	}
}