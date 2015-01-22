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

package org.wso2.carbon.monitoring.publisher.bam;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * This service component activates when carbon ConfigurationContextService is created.
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
		registration = bundleContext.registerService(MonitoringPublisher.class.getName(), new BAMMonitoringPublisher(), null);
	}

	protected void deactivate(BundleContext bundleContext) {

	}
}
