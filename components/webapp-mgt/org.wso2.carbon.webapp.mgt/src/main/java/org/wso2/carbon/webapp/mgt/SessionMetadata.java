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
 * Represents an HTTP Session
 */
public class SessionMetadata {

    private String sessionId;
    private String authType;
    private long creationTime;
    private long lastAccessedTime;
    private long maxInactiveInterval;

    public SessionMetadata() {
    }

    public SessionMetadata(WebApplication.HttpSession session) {
        this.sessionId = session.getSessionId();
        this.authType = session.getAuthType();
        this.creationTime = session.getCreationTime();
        this.lastAccessedTime = session.getLastAccessedTime();
        this.maxInactiveInterval = session.getMaxInactiveInterval();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAuthType() {
        return authType;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }
}
