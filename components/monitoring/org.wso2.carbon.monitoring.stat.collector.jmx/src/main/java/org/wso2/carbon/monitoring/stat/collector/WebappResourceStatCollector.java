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

package org.wso2.carbon.monitoring.stat.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.core.publisher.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.stat.jmx.CacheMBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.JspMonitorMBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.MBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.ManagerMBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.Result;
import org.wso2.carbon.monitoring.stat.jmx.WebModuleMBeanClient;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to collect Webapp Resource stats and publish to the publisher
 */
public class WebappResourceStatCollector extends PeriodicStatCollector {

    private static final Log LOG = LogFactory.getLog(WebappResourceStatCollector.class);
    private static CollectorUtil collectorUtil = new CollectorUtil();

    private final List<MonitoringPublisher> monitoringPublishers;

    public WebappResourceStatCollector(List<MonitoringPublisher> monitoringPublishers) {
        super();
        this.monitoringPublishers = monitoringPublishers;
    }

    @Override
    public void run() {
        try {
            MBeanClient webModuleMBeanClient = new WebModuleMBeanClient();
            List<Result> webModules = webModuleMBeanClient.readPossibleAttributeValues();

            MBeanClient managerMBeanClient = new ManagerMBeanClient();
            List<Result> managers = managerMBeanClient.readPossibleAttributeValues();

            MBeanClient cacheMBeanClient = new CacheMBeanClient();
            List<Result> caches = cacheMBeanClient.readPossibleAttributeValues();

            MBeanClient jspMonitorMBeanClient = new JspMonitorMBeanClient();
            List<Result> jsps = jspMonitorMBeanClient.readPossibleAttributeValues();

            List<WebappResourceMonitoringEvent> events = createWebappResourceMonitoringEvents(webModules, managers, caches, jsps);

            // publish the event to all the publishers
            for (WebappResourceMonitoringEvent event : events) {
                for (MonitoringPublisher publisher : monitoringPublishers) {
                    publisher.publish(event);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception occurred while publishing webapp resource stats.", e);
        }
    }

    private List<WebappResourceMonitoringEvent> createWebappResourceMonitoringEvents(
            List<Result> webModules, List<Result> managers, List<Result> caches,
            List<Result> jsps) throws AttributeMapperException {
        List<WebappResourceMonitoringEvent> events = new ArrayList<WebappResourceMonitoringEvent>();

        for (Result webModule : webModules) {
            WebappResourceMonitoringEvent event = new WebappResourceMonitoringEvent();

            String correlator = webModule.getCorrelator();
            event.setContext(correlator);

            //
            if(webModule != null) {
                collectorUtil.mapResultAttributesToPoJo(webModule, event);
            }

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