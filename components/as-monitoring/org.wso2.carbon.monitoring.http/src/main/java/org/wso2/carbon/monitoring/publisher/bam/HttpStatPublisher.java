package org.wso2.carbon.monitoring.publisher.bam;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.monitoring.api.WebappMonitoringEvent;

/**
 * Created by chamil on 7/13/14.
 */
public class HttpStatPublisher extends PublisherBase {
	public void publish(WebappMonitoringEvent e) {

	}

	@Override
	protected void addPayloadDataAttributes(StreamDefinition definition) {

	}

	@Override
	protected void addMetaDataAttributes(StreamDefinition definition) {

	}

	@Override
	protected String getDataStreamName() {
		return null;
	}
}
