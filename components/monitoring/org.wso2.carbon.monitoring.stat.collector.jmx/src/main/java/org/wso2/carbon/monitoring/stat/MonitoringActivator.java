package org.wso2.carbon.monitoring.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.Publisher;
import org.wso2.carbon.monitoring.core.publisher.api.WebappMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.stat.collector.ConnectorStatCollector;
import org.wso2.carbon.monitoring.stat.collector.WebappResourceStatCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component immediate="true"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.Publisher" cardinality="1..n" policy="dynamic" bind="setPublisher"
 * unbind="removePublisher"
 */
public class MonitoringActivator implements Publisher {

	private static Log log = LogFactory.getLog(MonitoringActivator.class);

	private ConnectorStatCollector connectorStatCollector;
	private WebappResourceStatCollector webappResourceStatCollector;


	private List<Publisher> publishers = new ArrayList<Publisher>();

	protected void setPublisher(Publisher publisher) {
		log.debug("Setting publisher " + publisher);
		this.publishers.add(publisher);
	}

	protected void removePublisher(Publisher publisher) {
		log.debug("Removing publisher " + publisher);
		publishers.remove(publisher);
	}


	protected void activate(ComponentContext context) {
		log.debug("Starting Periodic Monitoring Stat Collector.");
		connectorStatCollector = new ConnectorStatCollector(this);
		connectorStatCollector.start();

		webappResourceStatCollector = new WebappResourceStatCollector(this);
		webappResourceStatCollector.start();
	}

	protected void deactivate(ComponentContext context) {
		log.debug("Stopping Periodic Monitoring collector.");
		connectorStatCollector.stop();
		webappResourceStatCollector.stop();
	}

	@Override
	public void publish(WebappMonitoringEvent e) {
		for (Publisher publisher : publishers) {
			publisher.publish(e);
		}
	}

	@Override
	public void publish(ConnectorMonitoringEvent e) {
		for (Publisher publisher : publishers) {
			publisher.publish(e);
		}
	}

	@Override
	public void publish(WebappResourceMonitoringEvent e) {
		for (Publisher publisher : publishers) {
			publisher.publish(e);
		}
	}
}
