/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.as.monitoring.publisher.connector;

import org.wso2.carbon.as.monitoring.publisher.PublisherBase;
import org.wso2.carbon.as.monitoring.publisher.MonitoringPublisherException;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.config.StreamConfigurationReader;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tomcat connector details are published through this Stream.
 */
public class ConnectorPublisher extends PublisherBase {

    public ConnectorPublisher() throws BAMPublisherConfigurationException {
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
        return StreamConfigurationReader.CONNECTOR_DATA_STREAM_NAME;
    }

    /**
     * Publish connector monitoring event.
     *
     * @param e the connector monitoring event.
     */
    public void publish(ConnectorMonitoringEvent e) throws MonitoringPublisherException {
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
