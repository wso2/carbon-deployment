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
package org.wso2.carbon.monitoring.http.data;

public class WebappMonitoringEventData {
	private String webappName;
	private String webappOwnerTenant;
	private String webappVersion;
	private String userId;
	private long timestamp;
	private String resourcePath;
	private String browser;
	private String userAgentVersion;
	private String operatingSystem;
	private String operatingSystemVersion;
	private String country;
	private String webappType;
	private String webappDisplayName;
	private String webappContext;
	private String sessionId;
	private String httpMethod;
	private String contentType;
	private String responseContentType;
	private int responseHttpStatusCode;
	private String remoteAddress;
	private String referrer;
	private String remoteUser;
	private String authType;
	private String userAgentFamily;
	private long responseTime;
	private String serverAddress;
	private String serverName;
	private int tenantId;
	private String userTenant;
	private long requestSizeBytes;
	private long responseSizeBytes;
	private String requestHeader;
	private String responseHeader;
	private String requestPayload;
	private String responsePayload;
	private String language;
	private String deviceCategory;
	private String agentType;
	private String clusterId;

	public String getWebappName() {
		return webappName;
	}

	public void setWebappName(String webappName) {
		this.webappName = webappName;
	}

	public String getWebappOwnerTenant() {
		return webappOwnerTenant;
	}

	public void setWebappOwnerTenant(String webappOwnerTenant) {
		this.webappOwnerTenant = webappOwnerTenant;
	}

	public String getWebappVersion() {
		return webappVersion;
	}

	public void setWebappVersion(String webappVersion) {
		this.webappVersion = webappVersion;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getUserAgentVersion() {
		return userAgentVersion;
	}

	public void setUserAgentVersion(String userAgentVersion) {
		this.userAgentVersion = userAgentVersion;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getOperatingSystemVersion() {
		return operatingSystemVersion;
	}

	public void setOperatingSystemVersion(String operatingSystemVersion) {
		this.operatingSystemVersion = operatingSystemVersion;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getWebappType() {
		return webappType;
	}

	public void setWebappType(String webappType) {
		this.webappType = webappType;
	}

	public String getWebappDisplayName() {
		return webappDisplayName;
	}

	public void setWebappDisplayName(String webappDisplayName) {
		this.webappDisplayName = webappDisplayName;
	}

	public String getWebappContext() {
		return webappContext;
	}

	public void setWebappContext(String webappContext) {
		this.webappContext = webappContext;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getResponseContentType() {
		return responseContentType;
	}

	public void setResponseContentType(String responseContentType) {
		this.responseContentType = responseContentType;
	}

	public int getResponseHttpStatusCode() {
		return responseHttpStatusCode;
	}

	public void setResponseHttpStatusCode(int responseHttpStatusCode) {
		this.responseHttpStatusCode = responseHttpStatusCode;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getUserAgentFamily() {
		return userAgentFamily;
	}

	public void setUserAgentFamily(String userAgentFamily) {
		this.userAgentFamily = userAgentFamily;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getUserTenant() {
		return userTenant;
	}

	public void setUserTenant(String userTenant) {
		this.userTenant = userTenant;
	}

	public long getRequestSizeBytes() {
		return requestSizeBytes;
	}

	public void setRequestSizeBytes(long requestSizeBytes) {
		this.requestSizeBytes = requestSizeBytes;
	}

	public long getResponseSizeBytes() {
		return responseSizeBytes;
	}

	public void setResponseSizeBytes(long responseSizeBytes) {
		this.responseSizeBytes = responseSizeBytes;
	}

	public String getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}

	public String getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(String responseHeader) {
		this.responseHeader = responseHeader;
	}

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public void setDeviceCategory(String deviceCategory) {
		this.deviceCategory = deviceCategory;
	}

	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	public String getAgentType() {
		return agentType;
	}

	public String getClusterId(){
		return this.clusterId;
	}

	public void setClusterId(String clusterId){
		this.clusterId = clusterId;
	}
}
