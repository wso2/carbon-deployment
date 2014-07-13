package org.wso2.carbon.monitoring.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.api.PeriodicStatCollector;
import org.wso2.carbon.monitoring.api.Publisher;
import org.wso2.carbon.monitoring.jmx.*;

import java.util.ArrayList;
import java.util.List;

public class ConnectorStatCollector extends PeriodicStatCollector {

	private Publisher publisher;

	private static Log log = LogFactory.getLog(ConnectorStatCollector.class);
	private static CollectorUtil collectorUtil = new CollectorUtil(log);

	@Override
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

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
				publisher.publish(event);
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
