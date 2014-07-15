package org.wso2.carbon.monitoring.publisher.bam;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.monitoring.core.publisher.api.Publisher;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.monitoring.publisher.bam.BAMPublisherActivator"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContext"
 * unbind="unsetConfigurationContext"
 */
public class BAMPublisherActivator {
	private ServiceRegistration<?> registration;

	private ConfigurationContextService configCtx;

	protected void setConfigurationContext(ConfigurationContextService config) {
		this.configCtx = config;
	}

	protected void unsetConfigurationContext(ConfigurationContextService config) {
		this.configCtx = null;
	}

	protected void activate(ComponentContext component) {
		BundleContext bundleContext = component.getBundleContext();
		registration = bundleContext.registerService(Publisher.class.getName(), new BAMPublisher(), null);
	}

	protected void deactivate(BundleContext bundleContext) {

	}
}
