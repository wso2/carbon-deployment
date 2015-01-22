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
 * This class represents the Resource Monitoring Statistics
 */
public class WebappResourceMonitoringEvent extends MonitoringEvent{

	private String context;
	private String host;

	private Long processingTime;
	private Integer activeSessions;
	private Integer rejectedSessions;
	private Long expiredSessions;
	private Integer jspCount;
	private Integer jspReloadCount;
	private Integer jspErrorCount;
	private Long accessCount;
	private Long hitsCount;
	private Integer cacheSize;


	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(Long processingTime) {
		this.processingTime = processingTime;
	}

	public Integer getActiveSessions() {
		return activeSessions;
	}

	public void setActiveSessions(Integer activeSessions) {
		this.activeSessions = activeSessions;
	}

	public Integer getRejectedSessions() {
		return rejectedSessions;
	}

	public void setRejectedSessions(Integer rejectedSessions) {
		this.rejectedSessions = rejectedSessions;
	}

	public Long getExpiredSessions() {
		return expiredSessions;
	}

	public void setExpiredSessions(Long expiredSessions) {
		this.expiredSessions = expiredSessions;
	}

	public Integer getJspCount() {
		return jspCount;
	}

	public void setJspCount(Integer jspCount) {
		this.jspCount = jspCount;
	}

	public Integer getJspReloadCount() {
		return jspReloadCount;
	}

	public void setJspReloadCount(Integer jspReloadCount) {
		this.jspReloadCount = jspReloadCount;
	}

	public Integer getJspErrorCount() {
		return jspErrorCount;
	}

	public void setJspErrorCount(Integer jspErrorCount) {
		this.jspErrorCount = jspErrorCount;
	}

	public Long getAccessCount() {
		return accessCount;
	}

	public void setAccessCount(Long accessCount) {
		this.accessCount = accessCount;
	}

	public Long getHitsCount() {
		return hitsCount;
	}

	public void setHitsCount(Long hitsCount) {
		this.hitsCount = hitsCount;
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		this.cacheSize = cacheSize;
	}

	@Override
	public String toString() {
		return "WebappResourceMonitoringEvent{" +
		       "serverName='" + serverName + '\'' +
		       ", serverAddress='" + serverAddress + '\'' +
		       ", clusterDomain='" + clusterDomain + '\'' +
		       ", clusterSubDomain='" + clusterSubDomain + '\'' +
		       ", context='" + context + '\'' +
		       ", host='" + host + '\'' +
		       ", processingTime=" + processingTime +
		       ", activeSessions=" + activeSessions +
		       ", rejectedSessions=" + rejectedSessions +
		       ", expiredSessions=" + expiredSessions +
		       ", jspCount=" + jspCount +
		       ", jspReloadCount=" + jspReloadCount +
		       ", jspErrorCount=" + jspErrorCount +
		       ", accessCount=" + accessCount +
		       ", hitsCount=" + hitsCount +
		       ", cacheSize=" + cacheSize +
		       '}';
	}
}
