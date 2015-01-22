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
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectorPublisher extends PublisherBase {

	private static final Log log = LogFactory.getLog(BAMMonitoringPublisher.class);

	public ConnectorPublisher() {
		super();
	}

	@Override
	protected void addPayloadDataAttributes(StreamDefinition definition) {

		definition.addPayloadData("timestamp", AttributeType.LONG);
		definition.addPayloadData("connectorName", AttributeType.STRING);
		definition.addPayloadData("port", AttributeType.INT);
		definition.addPayloadData("scheme", AttributeType.STRING);
		definition.addPayloadData("bytesSent", AttributeType.LONG);
		definition.addPayloadData("bytesReceived", AttributeType.LONG);
		definition.addPayloadData("errorCount", AttributeType.INT);
		definition.addPayloadData("processingTime", AttributeType.LONG);
		definition.addPayloadData("requestCount", AttributeType.INT);
		definition.addPayloadData("connectionCount", AttributeType.LONG);
		definition.addPayloadData("currentThreadCount", AttributeType.INT);
		definition.addPayloadData("currentThreadsBusy", AttributeType.INT);
		definition.addPayloadData("keepAliveCount", AttributeType.INT);

	}

	@Override
	protected void addMetaDataAttributes(StreamDefinition definition) {
		definition.addMetaData("serverAddress", AttributeType.STRING);
		definition.addMetaData("serverName", AttributeType.STRING);
		definition.addMetaData("clusterDomain", AttributeType.STRING);
		definition.addMetaData("clusterSubDomain", AttributeType.STRING);
	}


	@Override
	protected String getDataStreamName() {
		return StreamConfigurationFactory.CONNECTOR_DATA_STREAM_NAME;
	}

	public void publish(ConnectorMonitoringEvent e) {
		List<Object> metaData = new ArrayList<Object>(4);
		metaData.add(e.getServerAddress());
		metaData.add(e.getServerName());
		metaData.add(e.getClusterDomain());
		metaData.add(e.getClusterSubDomain());

		List<Object> payloadData = new ArrayList<Object>(13);
		payloadData.add(e.getTimestamp());
		payloadData.add(e.getConnectorName());
		payloadData.add(e.getPort());
		payloadData.add(e.getScheme());
		payloadData.add(e.getBytesSent());
		payloadData.add(e.getBytesReceived());
		payloadData.add(e.getErrorCount());
		payloadData.add(e.getProcessingTime());
		payloadData.add(e.getRequestCount());
		payloadData.add(e.getConnectionCount());
		payloadData.add(e.getCurrentThreadCount());
		payloadData.add(e.getCurrentThreadsBusy());
		payloadData.add(e.getKeepAliveCount());

		Event event = new Event();

		event.setCorrelationData(Collections.emptyList().toArray());
		event.setMetaData(metaData.toArray());
		event.setPayloadData(payloadData.toArray());

		publish(event);
	}
}
