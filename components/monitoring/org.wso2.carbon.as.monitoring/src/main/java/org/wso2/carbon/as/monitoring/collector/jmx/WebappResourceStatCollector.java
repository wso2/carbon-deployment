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

package org.wso2.carbon.as.monitoring.collector.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.CacheMBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.JspMonitorMBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.MBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.ManagerMBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.Result;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.WebModuleMBeanClient;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.publisher.webappresource.WebappResourceMonitoringEvent;
import org.wso2.carbon.as.monitoring.publisher.webappresource.WebappResourcePublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to collect Webapp Resource stats and publish to the publisher
 */
public class WebappResourceStatCollector extends PeriodicStatCollector {

    private static final Log LOG = LogFactory.getLog(WebappResourceStatCollector.class);
    private static CollectorUtil collectorUtil = new CollectorUtil();

    private WebappResourcePublisher publisher;

    public WebappResourceStatCollector() {
        super();
        try {
            publisher = new WebappResourcePublisher();
        } catch (BAMPublisherConfigurationException e) {
            LOG.error("WebappResource monitoring will be disabled due to bad configuration.", e);
            this.stop();
        }
    }

    @Override
    public void run() {
        try {
            if (publisher == null || !publisher.isPublishable()) {
                return;
            }

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
                publisher.publish(event);
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

            collectorUtil.mapResultAttributesToPoJo(webModule, event);

            Result manager = collectorUtil.getResultByCorrelator(managers, correlator);
            if (manager != null) {
                collectorUtil.mapResultAttributesToPoJo(manager, event);
            }

            Result cache = collectorUtil.getResultByCorrelator(caches, correlator);
            if (cache != null) {
                collectorUtil.mapResultAttributesToPoJo(cache, event);
            }

            Result jsp = collectorUtil.getResultByCorrelator(jsps, correlator);
            if (jsp != null) {
                collectorUtil.mapResultAttributesToPoJo(jsp, event);
            }
            collectorUtil.mapMetaData(event);

            events.add(event);
        }

        return events;

    }
}