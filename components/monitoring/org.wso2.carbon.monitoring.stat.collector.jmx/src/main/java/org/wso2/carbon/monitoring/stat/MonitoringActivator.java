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

package org.wso2.carbon.monitoring.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.stat.collector.ConnectorStatCollector;
import org.wso2.carbon.monitoring.stat.collector.WebappResourceStatCollector;

/**
 * This is a service component which listen to Monitoring Publisher and start the jmx clients.
 *
 * @scr.component immediate="true"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher" cardinality="1..1" policy="dynamic" bind="setPublisher"
 * unbind="removePublisher"
 */
public class MonitoringActivator{

	private static Log log = LogFactory.getLog(MonitoringActivator.class);

	private ConnectorStatCollector connectorStatCollector;
	private WebappResourceStatCollector webappResourceStatCollector;


	private static MonitoringPublisher monitoringPublisher;

	protected void setPublisher(MonitoringPublisher monitoringPublisher) {
		log.debug("Setting publisher " + monitoringPublisher);
		this.monitoringPublisher = monitoringPublisher;
	}

	protected void removePublisher(MonitoringPublisher monitoringPublisher) {
		log.debug("Removing publisher " + monitoringPublisher);
		this.monitoringPublisher = monitoringPublisher;
	}


	protected void activate(ComponentContext context) {
		log.debug("Starting Periodic Monitoring Stat Collector.");
		connectorStatCollector = new ConnectorStatCollector(monitoringPublisher);
		connectorStatCollector.start();

		webappResourceStatCollector = new WebappResourceStatCollector(monitoringPublisher);
		webappResourceStatCollector.start();
	}

	protected void deactivate(ComponentContext context) {
		log.debug("Stopping Periodic Monitoring collector.");
		connectorStatCollector.stop();
		webappResourceStatCollector.stop();
	}
}
