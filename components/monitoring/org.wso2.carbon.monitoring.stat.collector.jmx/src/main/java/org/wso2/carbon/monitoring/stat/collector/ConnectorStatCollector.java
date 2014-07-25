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

package org.wso2.carbon.monitoring.stat.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.stat.jmx.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to retrieve Catalina Connector stats and publish to the publisher.
 */
public class ConnectorStatCollector extends PeriodicStatCollector {

	private MonitoringPublisher monitoringPublisher;

	private static Log log = LogFactory.getLog(ConnectorStatCollector.class);
	private static CollectorUtil collectorUtil = new CollectorUtil(log);

	public ConnectorStatCollector(MonitoringPublisher monitoringPublisher) {
		super();
		this.monitoringPublisher = monitoringPublisher;
	}

	public void setMonitoringPublisher(MonitoringPublisher monitoringPublisher) {
		this.monitoringPublisher = monitoringPublisher;
	}

	/**
	 * This method runs periodically
	 */
	@Override
	public void run() {
		try {
			MBeanClient connectorClient = new ConnectorMBeanClient();
			List<Result> connectors = connectorClient.readAttributeValues();

			MBeanClient threadPoolClient = new ThreadPoolMBeanClient();
			List<Result> threadPools = threadPoolClient.readAttributeValues();

			MBeanClient grpClient = new GlobalRequestProcessorMBeanClient();
			List<Result> globalRequestProcessors = grpClient.readAttributeValues();

			List<ConnectorMonitoringEvent> connectorMonitoringEvents = createConnectorMonitoringEvents(connectors, globalRequestProcessors, threadPools);
			for (ConnectorMonitoringEvent event : connectorMonitoringEvents) {
				monitoringPublisher.publish(event);
			}
		} catch (Exception e) {
			log.error("Exception occurred while publishing connector stats", e);
		}
	}

	private List<ConnectorMonitoringEvent> createConnectorMonitoringEvents(List<Result> connectors, List<Result> globalRequestProcessors, List<Result> threadPools) {
		List<ConnectorMonitoringEvent> events = new ArrayList<ConnectorMonitoringEvent>();

		for (Result connector : connectors) {
			ConnectorMonitoringEvent event = new ConnectorMonitoringEvent();

			collectorUtil.mapResultAttributesToPoJo(connector, event);
			String correlator = connector.getCorrelator();

			Result globalRequestProcessor = collectorUtil.getResultByCorrelator(globalRequestProcessors, correlator);
			collectorUtil.mapResultAttributesToPoJo(globalRequestProcessor, event);

			Result threadPool = collectorUtil.getResultByCorrelator(threadPools, correlator);
			collectorUtil.mapResultAttributesToPoJo(threadPool, event);

			collectorUtil.mapMetaData(event);
			events.add(event);
		}

		return events;
	}
}
