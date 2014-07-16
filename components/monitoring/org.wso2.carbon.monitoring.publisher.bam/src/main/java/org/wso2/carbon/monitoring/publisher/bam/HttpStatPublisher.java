package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.monitoring.core.publisher.api.WebappMonitoringEvent;

import java.util.ArrayList;
import java.util.List;


public class HttpStatPublisher extends PublisherBase {

	private static final Log log = LogFactory.getLog(HttpStatPublisher.class);

	public void publish(WebappMonitoringEvent e) {

		List<Object> payload = new ArrayList<Object>();
		payload.add(e.getWebappName());
		payload.add(e.getWebappVersion());
		payload.add(e.getUserId());
		payload.add(e.getResourcePath());
		payload.add(e.getWebappType());
		payload.add(e.getWebappDisplayName());
		payload.add(e.getWebappContext());
		payload.add(e.getSessionId());
		payload.add(e.getHttpMethod());
		payload.add(e.getContentType());
		payload.add(e.getResponseContentType());
		payload.add(e.getRemoteAddress());
		payload.add(e.getReferrer());
		payload.add(e.getRemoteUser());
		payload.add(e.getAuthType());
		payload.add(e.getUserAgentFamily());
		payload.add(e.getUserAgentVersion());
		payload.add(e.getOperatingSystem());
		payload.add(e.getOperatingSystemVersion());
		payload.add(e.getDeviceCategory());
		payload.add(e.getCountry());
		payload.add(e.getTimestamp());
		payload.add(e.getResponseHttpStatusCode());
		payload.add(e.getResponseTime());
		payload.add(e.getLanguage());
		payload.add(e.getRequestSizeBytes());
		payload.add(e.getResponseSizeBytes());
		payload.add(e.getRequestHeader());
		payload.add(e.getResponseHeader());
		payload.add(e.getRequestPayload());
		payload.add(e.getResponsePayload());

		List<Object> metaData = new ArrayList<Object>();
		metaData.add(e.getServerAddress());
		metaData.add(e.getServerName());
		metaData.add(e.getClusterDomain());
		metaData.add(e.getClusterSubDomain());
		metaData.add(e.getTenantId());
		metaData.add(e.getWebappOwnerTenant());
		metaData.add(e.getUserTenant());

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
		definition.addMetaData("clusterDomain", AttributeType.STRING);
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
		return StreamConfigurationFactory.HTTP_DATA_STREAM_NAME;
	}
}
