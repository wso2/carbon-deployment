package org.wso2.carbon.monitoring.http.util;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.monitoring.core.publisher.api.Publisher;
import org.wso2.carbon.monitoring.core.publisher.api.WebappMonitoringEvent;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.monitoring.http.ServiceReferenceHolder"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.Publisher" cardinality="1..n" policy="dynamic" bind="setPublisher"
 * unbind="removePublisher"
 */
public class ServiceReferenceHolder {

	private static ConfigurationContextService contextService;

	private static List<Publisher> publishers = new ArrayList<Publisher>();

	public ConfigurationContext getServerConfigContext() {
		return contextService.getServerConfigContext();
	}

	public void publish(WebappMonitoringEvent e){
		for (Publisher publisher : publishers) {
			publisher.publish(e);
		}
	}

	protected void setConfigurationContext(ConfigurationContextService contextService) {
		ServiceReferenceHolder.contextService = contextService;
	}

	protected void unsetConfigurationContext(ConfigurationContextService config) {
		ServiceReferenceHolder.contextService = null;
	}

	protected void setPublisher(Publisher publisher) {
		publishers.add(publisher);
	}

	protected void removePublisher(Publisher publisher) {
		publishers.remove(publisher);
	}
}
