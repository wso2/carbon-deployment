package org.wso2.carbon.monitoring.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.api.PeriodicStatCollector;
import org.wso2.carbon.monitoring.api.Publisher;
import org.wso2.carbon.monitoring.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.jmx.*;

import java.util.ArrayList;
import java.util.List;


public class WebappResourceStatCollector extends PeriodicStatCollector {

	private static final Log log = LogFactory.getLog(WebappResourceStatCollector.class);
	private static CollectorUtil collectorUtil = new CollectorUtil(log);
	private Publisher publisher;

	@Override
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void run() {
		try {
			MBeanClient webModuleMBeanClient = new WebModuleMBeanClient();
			List<Result> webModules = webModuleMBeanClient.readAttributeValues();

			MBeanClient managerMBeanClient = new ManagerMBeanClient();
			List<Result> managers = managerMBeanClient.readAttributeValues();

			MBeanClient cacheMBeanClient = new CacheMBeanClient();
			List<Result> caches = cacheMBeanClient.readAttributeValues();

			List<WebappResourceMonitoringEvent> events = createWebappResourceMonitoringEvents(webModules, managers, caches);

			for (WebappResourceMonitoringEvent event : events) {
				publisher.publish(event);
			}
		} catch (Exception e) {
			log.error("Exception occurred while publishing webapp resource stats", e);
		}
	}

	private List<WebappResourceMonitoringEvent> createWebappResourceMonitoringEvents(List<Result> webModules, List<Result> managers, List<Result> caches) {
		List<WebappResourceMonitoringEvent> events = new ArrayList<WebappResourceMonitoringEvent>();

		for (Result webModule : webModules) {
			WebappResourceMonitoringEvent event = new WebappResourceMonitoringEvent();

			String correlator = webModule.getCorrelator();
			event.setContext(correlator);
			collectorUtil.mapResultAttributesToPoJo(webModule, event);

			Result manager = collectorUtil.getResultByCorrelator(managers, correlator);
			collectorUtil.mapResultAttributesToPoJo(manager, event);

			Result cache = collectorUtil.getResultByCorrelator(caches, correlator);
			collectorUtil.mapResultAttributesToPoJo(cache, event);

			collectorUtil.mapMetaData(event);

			events.add(event);
		}

		return events;

	}
}