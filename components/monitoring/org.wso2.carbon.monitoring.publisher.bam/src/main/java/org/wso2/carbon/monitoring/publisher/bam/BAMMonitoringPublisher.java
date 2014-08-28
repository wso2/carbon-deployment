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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.*;

/**
 * Publishes monitoring events to BAM. This is the Facade for all the Monitoring streams.
 */
public class BAMMonitoringPublisher implements MonitoringPublisher {

	private static final Log log = LogFactory.getLog(BAMMonitoringPublisher.class);

	ConnectorPublisher connectorPublisher = new ConnectorPublisher();
	WebappResourcePublisher webResourcePublisher = new WebappResourcePublisher();
	HttpStatPublisher httpStatPublisher = new HttpStatPublisher();

	@Override
	public void publish(MonitoringEvent e) {
		if (e instanceof WebappMonitoringEvent) {
			httpStatPublisher.publish((WebappMonitoringEvent) e);
		} else if (e instanceof WebappResourceMonitoringEvent) {
			webResourcePublisher.publish((WebappResourceMonitoringEvent) e);
		} else if (e instanceof ConnectorMonitoringEvent) {
			connectorPublisher.publish((ConnectorMonitoringEvent) e);
		}else{
			log.warn("The MonitoringEvent Type " + e.getClass() + " not supported." );
		}
	}
}
