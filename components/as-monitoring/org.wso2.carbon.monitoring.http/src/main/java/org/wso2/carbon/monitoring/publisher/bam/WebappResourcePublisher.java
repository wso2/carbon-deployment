package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.monitoring.api.WebappResourceMonitoringEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WebappResourcePublisher extends PublisherBase {

	private static Log log = LogFactory.getLog(WebappResourcePublisher.class);

	public void publish(WebappResourceMonitoringEvent e) {
		List<Object> metaData = new ArrayList<Object>();
		metaData.add(mapNull(e.getServerAddress()));
		metaData.add(mapNull(e.getServerName()));
		metaData.add(mapNull(e.getClusterDomain()));
		metaData.add(mapNull(e.getClusterSubDomain()));
		metaData.add(mapNull(e.getHost()));
		metaData.add(mapNull(e.getContext()));

		List<Object> payloadData = new ArrayList<Object>();
		payloadData.add(mapNull(e.getErrorCount()));
		payloadData.add(mapNull(e.getProcessingTime()));
		payloadData.add(mapNull(e.getRequestCount()));
		payloadData.add(mapNull(e.getActiveSessions()));
		payloadData.add(mapNull(e.getRejectedSessions()));
		payloadData.add(mapNull(e.getExpiredSessions()));
		payloadData.add(mapNull(e.getJspCount()));
		payloadData.add(mapNull(e.getJspReloadCount()));
		payloadData.add(mapNull(e.getJspUnloadCount()));
		payloadData.add(mapNull(e.getAccessCount()));
		payloadData.add(mapNull(e.getHitsCount()));
		payloadData.add(mapNull(e.getCacheSize()));

		Event event = new Event();
		event.setMetaData(metaData.toArray());
		event.setPayloadData(payloadData.toArray());
		event.setCorrelationData(Collections.EMPTY_LIST.toArray());

		log.debug(event);
		publish(event);
	}

	@Override
	protected void addPayloadDataAttributes(StreamDefinition definition) {
		definition.addPayloadData("errorCount", AttributeType.INT);
		definition.addPayloadData("processingTime", AttributeType.LONG);
		definition.addPayloadData("requestCount", AttributeType.INT);
		definition.addPayloadData("activeSessions", AttributeType.INT);
		definition.addPayloadData("rejectedSessions", AttributeType.INT);
		definition.addPayloadData("expiredSessions", AttributeType.LONG);
		definition.addPayloadData("jspCount", AttributeType.INT);
		definition.addPayloadData("jspReloadCount", AttributeType.INT);
		definition.addPayloadData("jspUnloadCount", AttributeType.INT);
		definition.addPayloadData("cacheAccessCount", AttributeType.LONG);
		definition.addPayloadData("cacheHitsCount", AttributeType.LONG);
		definition.addPayloadData("cacheSize", AttributeType.INT);
	}

	@Override
	protected void addMetaDataAttributes(StreamDefinition definition) {
		definition.addMetaData("serverAddress", AttributeType.STRING);
		definition.addMetaData("serverName", AttributeType.STRING);
		definition.addMetaData("clusterDomain", AttributeType.STRING);
		definition.addMetaData("clusterSubDomain", AttributeType.STRING);
		definition.addMetaData("host", AttributeType.STRING);
		definition.addMetaData("context", AttributeType.STRING);
	}

	@Override
	protected String getDataStreamName() {
		return StreamConfigurationFactory.WEBAPP_RESOURCE_STREAM_NAME;
	}
}
