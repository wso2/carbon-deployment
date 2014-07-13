package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.api.Publisher;
import org.wso2.carbon.monitoring.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.api.WebappMonitoringEvent;

/**
 * Created by chamil on 7/7/14.
 */
public class BAMPublisher implements Publisher {

	private static final Log log = LogFactory.getLog(BAMPublisher.class);
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
