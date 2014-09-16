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
import org.wso2.carbon.as.monitoring.collector.jmx.clients.ConnectorMBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.GlobalRequestProcessorMBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.MBeanClient;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.Result;
import org.wso2.carbon.as.monitoring.collector.jmx.clients.ThreadPoolMBeanClient;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.publisher.connector.ConnectorMonitoringEvent;
import org.wso2.carbon.as.monitoring.publisher.connector.ConnectorPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs periodically to retrieve Catalina Connector stats and publish to the publisher.
 */
public class ConnectorStatCollector extends PeriodicStatCollector {

    private static final Log LOG = LogFactory.getLog(ConnectorStatCollector.class);
    private static CollectorUtil collectorUtil = new CollectorUtil();
    private ConnectorPublisher publisher;

    public ConnectorStatCollector() {
        super();
        try {
            publisher = new ConnectorPublisher();

            // No point of running this thread, if the publisher is disabled.
            if (!publisher.isPublishable()) {
                this.stop();
            }
        } catch (BAMPublisherConfigurationException e) {
            LOG.error("Connector monitoring will be disabled due to bad configuration.", e);
            this.stop();
        }

    }

    /**
     * This method runs periodically
     */
    @Override
    public void run() {
        try {

            if (publisher == null || !publisher.isPublishable()) {
                return;
            }

            MBeanClient connectorClient = new ConnectorMBeanClient();
            List<Result> connectors = connectorClient.readPossibleAttributeValues();

            MBeanClient threadPoolClient = new ThreadPoolMBeanClient();
            List<Result> threadPools = threadPoolClient.readPossibleAttributeValues();

            MBeanClient grpClient = new GlobalRequestProcessorMBeanClient();
            List<Result> globalRequestProcessors = grpClient.readPossibleAttributeValues();

            List<ConnectorMonitoringEvent> connectorMonitoringEvents = createConnectorMonitoringEvents(connectors, globalRequestProcessors, threadPools);

            // publishing the event to all the publishers
            for (ConnectorMonitoringEvent event : connectorMonitoringEvents) {
                publisher.publish(event);

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
            if (globalRequestProcessor != null) {
                collectorUtil.mapResultAttributesToPoJo(globalRequestProcessor, event);
            }

            Result threadPool = collectorUtil.getResultByCorrelator(threadPools, correlator);
            if (threadPool != null) {
                collectorUtil.mapResultAttributesToPoJo(threadPool, event);
            }

            event.setTimestamp(System.currentTimeMillis());
            collectorUtil.mapMetaData(event);
            events.add(event);
        }

        return events;
    }
}
