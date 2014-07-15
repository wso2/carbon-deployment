package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;


public class WebModuleMBeanClient extends MBeanClient {

	private static final String WEB_MODULE_NAME = "Catalina:j2eeType=WebModule,name=*,J2EEApplication=*,J2EEServer=*";

	@Override
	protected String getObjectNameQuery() {
		return WEB_MODULE_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return new String[] { "errorCount", "processingTime", "requestCount" };
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		return new AttributeList(0);
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		String name = objectName.getKeyProperty("name");

		// the name is like //localhost/STRATOS_ROOT
		// we need to extract the /STRATOS_ROOT
		int lastSlashIndex = name.lastIndexOf('/');
		return name.substring(lastSlashIndex);

	}
}
