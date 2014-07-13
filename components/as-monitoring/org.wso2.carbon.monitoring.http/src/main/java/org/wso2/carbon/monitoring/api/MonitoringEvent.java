package org.wso2.carbon.monitoring.api;

/**
 * Created by chamil on 7/12/14.
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
