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

package org.wso2.carbon.monitoring.core.publisher.api;

/**
 * All the Monitoring Publishers should implement this interface.
 * TODO: mention what is monitoring publisher
 */
public interface MonitoringPublisher {

    /**
     * Publish connector monitoring events to the publisher.
     *
     * @param e The webapp monitoring event statistics
     * @throws MonitoringPublisherException When unable to publish the event.
     */
    void publish(WebappMonitoringEvent e) throws MonitoringPublisherException;


    /**
     * Publish webapp resource monitoring events to the publisher.
     *
     * @param e The webapp monitoring event statistics
     * @throws MonitoringPublisherException When unable to publish the event.
     */
    void publish(WebappResourceMonitoringEvent e) throws MonitoringPublisherException;

    /**
     * Publish connector monitoring events to the publisher.
     *
     * @param e The connector monitoring event statistics
     * @throws MonitoringPublisherException When unable to publish the event.
     */
    void publish(ConnectorMonitoringEvent e) throws MonitoringPublisherException;

}

