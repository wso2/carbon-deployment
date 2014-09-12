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

package org.wso2.carbon.monitoring.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.monitoring.stat.collector.ConnectorStatCollector;
import org.wso2.carbon.monitoring.stat.collector.WebappResourceStatCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a service component which listen to Monitoring Publisher and start the jmx clients.
 *
 * @scr.component immediate="true"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher" cardinality="0..n" policy="dynamic" bind="addPublisher"
 * unbind="removePublisher"
 */
public class MonitoringServiceComponent {

    private static final Log LOG = LogFactory.getLog(MonitoringServiceComponent.class);

    private ConnectorStatCollector connectorStatCollector;
    private WebappResourceStatCollector webappResourceStatCollector;


    private static List<MonitoringPublisher> monitoringPublishers = new ArrayList<MonitoringPublisher>();

    protected void addPublisher(MonitoringPublisher monitoringPublisher) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding publisher " + monitoringPublisher);
        }
        this.monitoringPublishers.add(monitoringPublisher);
    }

    protected void removePublisher(MonitoringPublisher monitoringPublisher) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing publisher " + monitoringPublisher);
        }
        this.monitoringPublishers.remove(monitoringPublisher);
    }

    protected void activate(ComponentContext context) {
        LOG.debug("Starting Periodic Monitoring Stat Collector.");
        connectorStatCollector = new ConnectorStatCollector(monitoringPublishers);
        connectorStatCollector.start();

        webappResourceStatCollector = new WebappResourceStatCollector(monitoringPublishers);
        webappResourceStatCollector.start();
    }

    protected void deactivate(ComponentContext context) {
        LOG.debug("Stopping Periodic Monitoring collector.");
        connectorStatCollector.stop();
        webappResourceStatCollector.stop();
    }
}
