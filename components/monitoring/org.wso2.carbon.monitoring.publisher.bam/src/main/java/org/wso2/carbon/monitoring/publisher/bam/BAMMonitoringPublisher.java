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

package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.core.publisher.api.ConnectorMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisherException;
import org.wso2.carbon.monitoring.core.publisher.api.WebappMonitoringEvent;
import org.wso2.carbon.monitoring.core.publisher.api.WebappResourceMonitoringEvent;
import org.wso2.carbon.monitoring.publisher.bam.config.BAMPublisherConfigurationException;

/**
 * Publishes monitoring events to BAM. This is the Facade for all the Monitoring streams.
 */
public class BAMMonitoringPublisher implements MonitoringPublisher {

    private static final Log LOG = LogFactory.getLog(BAMMonitoringPublisher.class);

    private final ConnectorPublisher connectorPublisher;
    private final WebappResourcePublisher webResourcePublisher;
    private final HttpStatPublisher httpStatPublisher;

    /**
     * Instantiate BAMMonitoringPublisher instance
     * @throws BAMPublisherConfigurationException when the configuration issue detected.
     */
    public BAMMonitoringPublisher() throws BAMPublisherConfigurationException {
        connectorPublisher = new ConnectorPublisher();
        webResourcePublisher = new WebappResourcePublisher();
        httpStatPublisher = new HttpStatPublisher();
    }

    @Override
    public void publish(WebappMonitoringEvent e) throws MonitoringPublisherException {
        LOG.trace(e);
        httpStatPublisher.publish(e);
    }

    @Override
    public void publish(WebappResourceMonitoringEvent e) throws MonitoringPublisherException {
        LOG.trace(e);
        webResourcePublisher.publish(e);
    }

    @Override
    public void publish(ConnectorMonitoringEvent e) throws MonitoringPublisherException {
        LOG.trace(e);
        connectorPublisher.publish(e);
    }
}
