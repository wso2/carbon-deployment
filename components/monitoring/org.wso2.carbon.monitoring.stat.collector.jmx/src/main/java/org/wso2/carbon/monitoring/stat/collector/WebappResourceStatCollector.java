/*
 * Copyright 2004,2013 The Apache Software Foundation.
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

package org.wso2.carbon.monitoring.stat.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.core.publisher.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.stat.jmx.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to collect Webapp Resource stats and publish to the publisher
 */
public class WebappResourceStatCollector extends PeriodicStatCollector {

	private static final Log log = LogFactory.getLog(WebappResourceStatCollector.class);
	private static CollectorUtil collectorUtil = new CollectorUtil(log);

	private MonitoringPublisher monitoringPublisher;

	public WebappResourceStatCollector(MonitoringPublisher monitoringPublisher) {
		super();
		this.monitoringPublisher = monitoringPublisher;
	}

	public void setMonitoringPublisher(MonitoringPublisher monitoringPublisher) {
		this.monitoringPublisher = monitoringPublisher;
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

			MBeanClient jspMonitorMBeanClient = new JspMonitorMBeanClient();
			List<Result> jsps = jspMonitorMBeanClient.readAttributeValues();

			List<WebappResourceMonitoringEvent> events = createWebappResourceMonitoringEvents(webModules, managers, caches, jsps);

			for (WebappResourceMonitoringEvent event : events) {
				monitoringPublisher.publish(event);
			}
		} catch (Exception e) {
			log.error("Exception occurred while publishing webapp resource stats", e);
		}
	}

	private List<WebappResourceMonitoringEvent> createWebappResourceMonitoringEvents(List<Result> webModules, List<Result> managers, List<Result> caches, List<Result> jsps) {
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

			Result jsp = collectorUtil.getResultByCorrelator(jsps, correlator);
			collectorUtil.mapResultAttributesToPoJo(jsp, event);

			collectorUtil.mapMetaData(event);

			events.add(event);
		}

		return events;

	}
}