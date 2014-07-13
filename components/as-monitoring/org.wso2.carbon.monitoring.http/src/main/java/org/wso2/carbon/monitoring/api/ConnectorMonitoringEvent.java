package org.wso2.carbon.monitoring.api;

/**
 * Created by chamil on 7/2/14.
 */
public class ConnectorMonitoringEvent extends MonitoringEvent {

	private long timestamp;
	private String connectorName;
	private Integer port;
	private String scheme;
	private Long bytesSent;
	private Long bytesReceived;
	private Integer errorCount;
	private Long processingTime;
	private Integer requestCount;
	private Long connectionCount;
	private Integer currentThreadCount;
	private Integer currentThreadsBusy;
	private Integer keepAliveCount;


	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public Long getBytesSent() {
		return bytesSent;
	}

	public void setBytesSent(Long bytesSent) {
		this.bytesSent = bytesSent;
	}

	public Long getBytesReceived() {
		return bytesReceived;
	}

	public void setBytesReceived(Long bytesReceived) {
		this.bytesReceived = bytesReceived;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public Long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(Long processingTime) {
		this.processingTime = processingTime;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public Long getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(Long connectionCount) {
		this.connectionCount = connectionCount;
	}

	public int getCurrentThreadCount() {
		return currentThreadCount;
	}

	public void setCurrentThreadCount(int currentThreadCount) {
		this.currentThreadCount = currentThreadCount;
	}

	public int getCurrentThreadsBusy() {
		return currentThreadsBusy;
	}

	public void setCurrentThreadsBusy(int currentThreadsBusy) {
		this.currentThreadsBusy = currentThreadsBusy;
	}

	public int getKeepAliveCount() {
		return keepAliveCount;
	}

	public void setKeepAliveCount(int keepAliveCount) {
		this.keepAliveCount = keepAliveCount;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "ConnectorMonitoringEvent{" +
		       "serverName='" + serverName + '\'' +
		       ", serverAddress='" + serverAddress + '\'' +
		       ", clusterDomain='" + clusterDomain + '\'' +
		       ", timestamp=" + timestamp +
		       ", connectorName='" + connectorName + '\'' +
		       ", port=" + port +
		       ", scheme='" + scheme + '\'' +
		       ", bytesSent=" + bytesSent +
		       ", bytesReceived=" + bytesReceived +
		       ", errorCount=" + errorCount +
		       ", processingTime=" + processingTime +
		       ", requestCount=" + requestCount +
		       ", connectionCount=" + connectionCount +
		       ", currentThreadCount=" + currentThreadCount +
		       ", currentThreadsBusy=" + currentThreadsBusy +
		       ", keepAliveCount=" + keepAliveCount +
		       ", clusterSubDomain='" + clusterSubDomain + '\'' +
		       '}';
	}
}
