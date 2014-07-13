package org.wso2.carbon.monitoring.api;

public class WebappResourceMonitoringEvent extends MonitoringEvent{

	private String context;
	private String host;

	private Integer errorCount;
	private Long processingTime;
	private Integer requestCount;
	private Integer activeSessions;
	private Integer rejectedSessions;
	private Long expiredSessions;
	private Integer jspCount;
	private Integer jspReloadCount;
	private Integer jspUnloadCount;
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

	public Integer getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	public Long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(Long processingTime) {
		this.processingTime = processingTime;
	}

	public Integer getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(Integer requestCount) {
		this.requestCount = requestCount;
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

	public Integer getJspUnloadCount() {
		return jspUnloadCount;
	}

	public void setJspUnloadCount(Integer jspUnloadCount) {
		this.jspUnloadCount = jspUnloadCount;
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
		       ", errorCount=" + errorCount +
		       ", processingTime=" + processingTime +
		       ", requestCount=" + requestCount +
		       ", activeSessions=" + activeSessions +
		       ", rejectedSessions=" + rejectedSessions +
		       ", expiredSessions=" + expiredSessions +
		       ", jspCount=" + jspCount +
		       ", jspReloadCount=" + jspReloadCount +
		       ", jspUnloadCount=" + jspUnloadCount +
		       ", accessCount=" + accessCount +
		       ", hitsCount=" + hitsCount +
		       ", cacheSize=" + cacheSize +
		       '}';
	}
}
