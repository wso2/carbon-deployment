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
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.stat.jmx.ConnectorMBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.GlobalRequestProcessorMBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.MBeanClient;
import org.wso2.carbon.monitoring.stat.jmx.Result;
import org.wso2.carbon.monitoring.stat.jmx.ThreadPoolMBeanClient;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to retrieve Catalina Connector stats and publish to the publisher.
 */
public class ConnectorStatCollector extends PeriodicStatCollector {

    private static final Log LOG = LogFactory.getLog(ConnectorStatCollector.class);
    private static CollectorUtil collectorUtil = new CollectorUtil();
    private final List<MonitoringPublisher> monitoringPublishers;

    public ConnectorStatCollector(List<MonitoringPublisher> monitoringPublisher) {
        super();
        this.monitoringPublishers = monitoringPublisher;
    }

    /**
     * This method runs periodically
     */
    @Override
    public void run() {
        try {
            MBeanClient connectorClient = new ConnectorMBeanClient();
            List<Result> connectors = connectorClient.readPossibleAttributeValues();

            MBeanClient threadPoolClient = new ThreadPoolMBeanClient();
            List<Result> threadPools = threadPoolClient.readPossibleAttributeValues();

            MBeanClient grpClient = new GlobalRequestProcessorMBeanClient();
            List<Result> globalRequestProcessors = grpClient.readPossibleAttributeValues();

            List<ConnectorMonitoringEvent> connectorMonitoringEvents = createConnectorMonitoringEvents(connectors, globalRequestProcessors, threadPools);

            // publishing the event to all the publishers
            for (ConnectorMonitoringEvent event : connectorMonitoringEvents) {
                for (MonitoringPublisher publisher : monitoringPublishers) {
                    publisher.publish(event);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception occurred while publishing connector stats: ", e);
        }
    }

    private List<ConnectorMonitoringEvent> createConnectorMonitoringEvents(List<Result> connectors,
                                                                           List<Result> globalRequestProcessors,
                                                                           List<Result> threadPools)
            throws AttributeMapperException {
        List<ConnectorMonitoringEvent> events = new ArrayList<ConnectorMonitoringEvent>();

        for (Result connector : connectors) {
            ConnectorMonitoringEvent event = new ConnectorMonitoringEvent();

            collectorUtil.mapResultAttributesToPoJo(connector, event);
            String correlator = connector.getCorrelator();

            Result globalRequestProcessor = collectorUtil.getResultByCorrelator(globalRequestProcessors, correlator);
            collectorUtil.mapResultAttributesToPoJo(globalRequestProcessor, event);

            Result threadPool = collectorUtil.getResultByCorrelator(threadPools, correlator);
            collectorUtil.mapResultAttributesToPoJo(threadPool, event);

            event.setTimestamp(System.currentTimeMillis());
            collectorUtil.mapMetaData(event);
            events.add(event);
        }

        return events;
    }
}
