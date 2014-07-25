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
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.core.publisher.api.WebappMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.WebappResourceMonitoringEvent;

/**
 * Publishes monitoring events to BAM
 */
public class BAMMonitoringPublisher implements MonitoringPublisher {

	private static final Log log = LogFactory.getLog(BAMMonitoringPublisher.class);
	ConnectorPublisher connectorPublisher = new ConnectorPublisher();
	WebappResourcePublisher webResourcePublisher = new WebappResourcePublisher();
	HttpStatPublisher httpStatPublisher = new HttpStatPublisher();


	@Override
	public void publish(WebappMonitoringEvent e) {
		httpStatPublisher.publish(e);
	}

	@Override
	public void publish(ConnectorMonitoringEvent e) {
		connectorPublisher.publish(e);
	}


	@Override
	public void publish(WebappResourceMonitoringEvent e) {
		webResourcePublisher.publish(e);
	}
}
