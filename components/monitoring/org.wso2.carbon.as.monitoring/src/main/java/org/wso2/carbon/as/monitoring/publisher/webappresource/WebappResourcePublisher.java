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

package org.wso2.carbon.as.monitoring.publisher.webappresource;

import org.wso2.carbon.as.monitoring.publisher.MonitoringPublisherException;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.config.StreamConfigurationReader;
import org.wso2.carbon.as.monitoring.publisher.PublisherBase;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Publisher stream for Webapp Resource Data stream.
 */
public class WebappResourcePublisher extends PublisherBase {

    public WebappResourcePublisher()
            throws BAMPublisherConfigurationException {
        super();
    }

    public void publish(WebappResourceMonitoringEvent e) throws MonitoringPublisherException {
        List<Object> metaData = new ArrayList<Object>();
        metaData.add(mapNull(e.getServerAddress()));
        metaData.add(mapNull(e.getServerName()));
        metaData.add(mapNull(e.getClusterDomain()));
        metaData.add(mapNull(e.getClusterSubDomain()));
        metaData.add(mapNull(e.getHost()));
        metaData.add(mapNull(e.getContext()));

        List<Object> payloadData = new ArrayList<Object>();
        payloadData.add(mapNull(e.getProcessingTime()));
        payloadData.add(mapNull(e.getActiveSessions()));
        payloadData.add(mapNull(e.getRejectedSessions()));
        payloadData.add(mapNull(e.getExpiredSessions()));
        payloadData.add(mapNull(e.getJspCount()));
        payloadData.add(mapNull(e.getJspReloadCount()));
        payloadData.add(mapNull(e.getJspErrorCount()));
        payloadData.add(mapNull(e.getAccessCount()));
        payloadData.add(mapNull(e.getHitsCount()));
        payloadData.add(mapNull(e.getCacheSize()));

        Event event = new Event();
        event.setMetaData(metaData.toArray());
        event.setPayloadData(payloadData.toArray());
        event.setCorrelationData(new Object[0]);

        publish(event);
    }

    @Override
    protected void addPayloadDataAttributes(StreamDefinition definition) {
        definition.addPayloadData("processingTime", AttributeType.LONG);
        definition.addPayloadData("activeSessions", AttributeType.INT);
        definition.addPayloadData("rejectedSessions", AttributeType.INT);
        definition.addPayloadData("expiredSessions", AttributeType.LONG);
        definition.addPayloadData("jspCount", AttributeType.INT);
        definition.addPayloadData("jspReloadCount", AttributeType.INT);
        definition.addPayloadData("jspErrorCount", AttributeType.INT);
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
        return StreamConfigurationReader.WEBAPP_RESOURCE_STREAM_NAME;
    }
}
