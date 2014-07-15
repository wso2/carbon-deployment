package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;


public class CacheMBeanClient extends MBeanClient {

	private static final String CACHE_NAME = "Catalina:type=Cache,host=*,context=*";

	private static final String[] CACHE_ATTRIBUTES = { "accessCount", "cacheSize", "hitsCount" };

	@Override
	protected String getObjectNameQuery() {
		return CACHE_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return CACHE_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		return new AttributeList();
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		return objectName.getKeyProperty("context");
	}
}
