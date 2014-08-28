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
 * All the Monitoring events should extend this base class.
 */
public class MonitoringEvent {
	protected String serverName;
	protected String serverAddress;
	protected String clusterDomain;
	protected String clusterSubDomain;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getClusterDomain() {
		return clusterDomain;
	}

	public void setClusterDomain(String clusterDomain) {
		this.clusterDomain = clusterDomain;
	}

	public void setClusterSubDomain(String clusterSubDomain) {
		this.clusterSubDomain = clusterSubDomain;
	}

	public String getClusterSubDomain() {
		return clusterSubDomain;
	}
}
