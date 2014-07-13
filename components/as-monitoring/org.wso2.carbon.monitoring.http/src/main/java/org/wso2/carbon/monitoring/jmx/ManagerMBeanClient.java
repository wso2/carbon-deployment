package org.wso2.carbon.monitoring.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * Created by chamil on 7/11/14.
 */
public class ManagerMBeanClient extends MBeanClient {

	private static final String MANAGER_NAME = "Catalina:type=Manager,context=*,host=*";

	private static final String[] MANAGER_ATTRIBUTES = { "activeSessions", "rejectedSessions", "expiredSessions" };

	@Override
	protected String getObjectNameQuery() {
		return MANAGER_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return MANAGER_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		AttributeList list = new AttributeList();
		list.add(new Attribute("host", objectName.getKeyProperty("host")));
		return list;
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		return objectName.getKeyProperty("context");
	}
}
