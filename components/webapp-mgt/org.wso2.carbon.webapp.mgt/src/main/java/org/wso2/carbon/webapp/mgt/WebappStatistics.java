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

/**
 * Represents Web Application Statistics
 */
public class WebappStatistics {

    private int maxActiveSessions;
    private int maxSessionInactivityInterval;
    private int maxSessionLifetime;
    private int avgSessionLifetime;
    private int rejectedSessions;
    private int activeSessions;
    private long expiredSessions;

    public int getMaxActiveSessions() {
        return maxActiveSessions;
    }

    public void setMaxActiveSessions(int maxActive) {
        this.maxActiveSessions = maxActive;
    }

    public int getMaxSessionInactivityInterval() {
        return maxSessionInactivityInterval;
    }

    public void setMaxSessionInactivityInterval(int maxInactiveInterval) {
        this.maxSessionInactivityInterval = maxInactiveInterval;
    }

    public int getMaxSessionLifetime() {
        return maxSessionLifetime;
    }

    public void setMaxSessionLifetime(int maxSessionLifetime) {
        this.maxSessionLifetime = maxSessionLifetime;
    }

    public int getAvgSessionLifetime() {
        return avgSessionLifetime;
    }

    public void setAvgSessionLifetime(int avgSessionLifetime) {
        this.avgSessionLifetime = avgSessionLifetime;
    }

    public int getRejectedSessions() {
        return rejectedSessions;
    }

    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    public long getExpiredSessions() {
        return expiredSessions;
    }

    public void setExpiredSessions(long expiredSessions) {
        this.expiredSessions = expiredSessions;
    }
}
