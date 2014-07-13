package org.wso2.carbon.monitoring.api;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.monitoring.collector.ConnectorStatCollector;
import org.wso2.carbon.monitoring.collector.WebappResourceStatCollector;
import org.wso2.carbon.monitoring.publisher.bam.BAMPublisher;

/**
 * Created by chamil on 6/26/14.
 */
public class MonitoringActivator implements BundleActivator {

	private PeriodicStatCollector connectorStatCollector;
	private WebappResourceStatCollector webappResourceStatCollector;
	private BAMPublisher publisher = new BAMPublisher();

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		connectorStatCollector = new ConnectorStatCollector();
		connectorStatCollector.setPublisher(publisher);
		connectorStatCollector.start();

		webappResourceStatCollector = new WebappResourceStatCollector();
		webappResourceStatCollector.setPublisher(publisher);
		webappResourceStatCollector.start();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		connectorStatCollector.stop();
		webappResourceStatCollector.stop();
	}
}
