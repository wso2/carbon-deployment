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

package org.wso2.carbon.as.monitoring.publisher.http;

import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.config.StreamConfigurationReader;
import org.wso2.carbon.as.monitoring.publisher.MonitoringPublisherException;
import org.wso2.carbon.as.monitoring.publisher.PublisherBase;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * The Http Webapp Call stat publisher
 */
public class HttpStatPublisher extends PublisherBase {

    public HttpStatPublisher() throws BAMPublisherConfigurationException {
        super();
    }

    /**
     * Publish a WebappMonitoringEvent
     *
     * @param monitoringEvent
     * @throws MonitoringPublisherException when publish failure
     */
    public void publish(WebappMonitoringEvent monitoringEvent) throws MonitoringPublisherException {

        List<Object> payload = new ArrayList<Object>();
        payload.add(mapNull(monitoringEvent.getWebappName()));
        payload.add(mapNull(monitoringEvent.getWebappVersion()));
        payload.add(mapNull(monitoringEvent.getUserId()));
        payload.add(mapNull(monitoringEvent.getResourcePath()));
        payload.add(mapNull(monitoringEvent.getWebappType()));
        payload.add(mapNull(monitoringEvent.getWebappDisplayName()));
        payload.add(mapNull(monitoringEvent.getWebappContext()));
        payload.add(mapNull(monitoringEvent.getSessionId()));
        payload.add(mapNull(monitoringEvent.getHttpMethod()));
        payload.add(mapNull(monitoringEvent.getContentType()));
        payload.add(mapNull(monitoringEvent.getResponseContentType()));
        payload.add(mapNull(monitoringEvent.getRemoteAddress()));
        payload.add(mapNull(monitoringEvent.getReferrer()));
        payload.add(mapNull(monitoringEvent.getRemoteUser()));
        payload.add(mapNull(monitoringEvent.getAuthType()));
        payload.add(mapNull(monitoringEvent.getUserAgentFamily()));
        payload.add(mapNull(monitoringEvent.getUserAgentVersion()));
        payload.add(mapNull(monitoringEvent.getOperatingSystem()));
        payload.add(mapNull(monitoringEvent.getOperatingSystemVersion()));
        payload.add(mapNull(monitoringEvent.getDeviceCategory()));
        payload.add(mapNull(monitoringEvent.getCountry()));
        payload.add(mapNull(monitoringEvent.getTimestamp()));
        payload.add(mapNull(monitoringEvent.getResponseHttpStatusCode()));
        payload.add(mapNull(monitoringEvent.getResponseTime()));
        payload.add(mapNull(monitoringEvent.getLanguage()));
        payload.add(mapNull(monitoringEvent.getRequestSizeBytes()));
        payload.add(mapNull(monitoringEvent.getResponseSizeBytes()));
        payload.add(mapNull(monitoringEvent.getRequestHeader()));
        payload.add(mapNull(monitoringEvent.getResponseHeader()));
        payload.add(mapNull(monitoringEvent.getRequestPayload()));
        payload.add(mapNull(monitoringEvent.getResponsePayload()));

        List<Object> metaData = new ArrayList<Object>();
        metaData.add(mapNull(monitoringEvent.getServerAddress()));
        metaData.add(mapNull(monitoringEvent.getServerName()));
        metaData.add(mapNull(monitoringEvent.getClusterDomain()));
        metaData.add(mapNull(monitoringEvent.getClusterSubDomain()));
        metaData.add(mapNull(monitoringEvent.getTenantId()));
        metaData.add(mapNull(monitoringEvent.getWebappOwnerTenant()));
        metaData.add(mapNull(monitoringEvent.getUserTenant()));

        Event event = new Event();
        event.setPayloadData(payload.toArray());
        event.setMetaData(metaData.toArray());
        publish(event);
    }

    @Override
    protected void addMetaDataAttributes(StreamDefinition definition) {
        definition.addMetaData("serverAddress", AttributeType.STRING);
        definition.addMetaData("serverName", AttributeType.STRING);
        definition.addMetaData("clusterDomain", AttributeType.STRING);
        definition.addMetaData("clusterSubDomain", AttributeType.STRING);
        definition.addMetaData("tenantId", AttributeType.INT);
        definition.addMetaData("webappOwnerTenant", AttributeType.STRING);
        definition.addMetaData("userTenant", AttributeType.STRING);
    }

    @Override
    protected void addPayloadDataAttributes(StreamDefinition definition) {

        definition.addPayloadData("webappName", AttributeType.STRING);
        definition.addPayloadData("webappVersion", AttributeType.STRING);
        definition.addPayloadData("userId", AttributeType.STRING);
        definition.addPayloadData("resourcePath", AttributeType.STRING);
        definition.addPayloadData("webappType", AttributeType.STRING);
        definition.addPayloadData("webappDisplayName", AttributeType.STRING);
        definition.addPayloadData("webappContext", AttributeType.STRING);
        definition.addPayloadData("sessionId", AttributeType.STRING);
        definition.addPayloadData("httpMethod", AttributeType.STRING);
        definition.addPayloadData("contentType", AttributeType.STRING);
        definition.addPayloadData("responseContentType", AttributeType.STRING);
        definition.addPayloadData("remoteAddress", AttributeType.STRING);
        definition.addPayloadData("referrer", AttributeType.STRING);
        definition.addPayloadData("remoteUser", AttributeType.STRING);
        definition.addPayloadData("authType", AttributeType.STRING);
        definition.addPayloadData("userAgentFamily", AttributeType.STRING);
        definition.addPayloadData("agentVersion", AttributeType.STRING);
        definition.addPayloadData("operatingSystem", AttributeType.STRING);
        definition.addPayloadData("operatingSystemVersion", AttributeType.STRING);
        definition.addPayloadData("deviceCategory", AttributeType.STRING);
        definition.addPayloadData("country", AttributeType.STRING);
        definition.addPayloadData("timestamp", AttributeType.LONG);
        definition.addPayloadData("responseHttpStatusCode", AttributeType.INT);
        definition.addPayloadData("responseTime", AttributeType.LONG);
        definition.addPayloadData("language", AttributeType.STRING);
        definition.addPayloadData("requestSizeBytes", AttributeType.LONG);
        definition.addPayloadData("responseSizeBytes", AttributeType.LONG);
        definition.addPayloadData("requestHeaders", AttributeType.STRING);
        definition.addPayloadData("responseHeaders", AttributeType.STRING);
        definition.addPayloadData("requestPayload", AttributeType.STRING);
        definition.addPayloadData("responsePayload", AttributeType.STRING);
    }

    @Override
    protected String getDataStreamName() {
        return StreamConfigurationReader.HTTP_DATA_STREAM_NAME;
    }
}
