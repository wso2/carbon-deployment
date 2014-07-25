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

package org.wso2.carbon.monitoring.core.publisher.api;

/**
 * All the Monitoring Publishers should implement this interface and publish themselves as OSGI services.
 */
public interface MonitoringPublisher {

	/**
	 * Publish Webapp Call Monitoring events to the publisher.
	 * @param e The event statistics
	 */
	void publish(WebappMonitoringEvent e);

	/**
	 * Publish the Calatina Connector Monitoring events to the publisher.
	 * @param e The event statistics
	 */
	void publish(ConnectorMonitoringEvent e);

	/**
	 * Publish Webapp Resource Monitoring events to the publisher.
	 * @param e The event statistics
	 */
	void publish(WebappResourceMonitoringEvent e);
}
