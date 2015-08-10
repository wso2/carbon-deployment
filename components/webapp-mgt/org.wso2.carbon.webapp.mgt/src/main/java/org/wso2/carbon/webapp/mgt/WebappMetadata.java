/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.webapp.mgt;

/*
* Represents a Web Application
*/
public class WebappMetadata {
    private String context;
    private String displayName;
    private String webappFile;
    private boolean isRunning;
    private boolean isStarted;
    private long lastModifiedTime;
    private WebappStatistics statistics;
    private boolean isFaulty;
    private String faultException;
    private String state;
    private String webappType;
    private String servletContext;
    private String serviceListPath;
    private String appVersion;
    private String contextPath;
    private boolean isCAppArtifact = false;
    private String hostName;
    private String webappKey;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWebappFile() {
        return webappFile;
    }

    public void setWebappFile(String webappFile) {
        this.webappFile = webappFile;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public WebappStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(WebappStatistics statistics) {
        this.statistics = statistics;
    }

    public boolean isFaulty() {
        return isFaulty;
    }

    public void setFaulty(boolean faulty) {
        isFaulty = faulty;
    }

    public String getFaultException() {
        return faultException;
    }

    public void setFaultException(String faultException) {
        this.faultException = faultException;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public String getWebappType() {
        return webappType;
    }

    public void setWebappType(String webappType) {
        this.webappType = webappType;
    }

    public String getServletContext() {
        return servletContext;
    }

    public void setServletContext(String servletContext) {
        this.servletContext = servletContext;
    }

    public String getServiceListPath() {
        return serviceListPath;
    }

    public void setServiceListPath(String serviceListPath) {
        this.serviceListPath = serviceListPath;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getWebappKey() {
        return webappKey;
    }

    public void setWebappKey(String webappKey) {
        this.webappKey = webappKey;
    }

    /**
     * get isCAppArtifact property
     *
     * @return boolean
     */
    public boolean isCAppArtifact() {
        return isCAppArtifact;
    }

    /**
     * This will be the place to enable service as CApp artifact
     *
     * @param isCAppArtifact
     */
    public void setCAppArtifact(boolean isCAppArtifact) {
        this.isCAppArtifact = isCAppArtifact;
    }
}
