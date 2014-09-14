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

package org.wso2.carbon.as.monitoring.publisher.connector;

import org.wso2.carbon.as.monitoring.publisher.MonitoringEvent;

/**
 * Purpose of this class is to hold Catalina Connector related stats.
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
        final StringBuilder sb = new StringBuilder("ConnectorMonitoringEvent{");
        sb.append(super.toString());
        sb.append("timestamp=").append(timestamp);
        sb.append(", connectorName='").append(connectorName).append('\'');
        sb.append(", port=").append(port);
        sb.append(", scheme='").append(scheme).append('\'');
        sb.append(", bytesSent=").append(bytesSent);
        sb.append(", bytesReceived=").append(bytesReceived);
        sb.append(", errorCount=").append(errorCount);
        sb.append(", processingTime=").append(processingTime);
        sb.append(", requestCount=").append(requestCount);
        sb.append(", connectionCount=").append(connectionCount);
        sb.append(", currentThreadCount=").append(currentThreadCount);
        sb.append(", currentThreadsBusy=").append(currentThreadsBusy);
        sb.append(", keepAliveCount=").append(keepAliveCount);
        sb.append('}');
        return sb.toString();
    }
}
