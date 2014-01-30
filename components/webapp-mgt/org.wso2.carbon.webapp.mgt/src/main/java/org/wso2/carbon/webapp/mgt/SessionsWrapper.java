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

import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.Pageable;

import java.util.List;

/**
 * Wraps a collection of Webapp sessions
 */
public class SessionsWrapper implements Pageable {
    private SessionMetadata[] sessions;
    private int numberOfPages;


    private String webappFileName;
    private int numberOfActiveSessions;

    public String getWebappFileName() {
        return webappFileName;
    }

    public void setWebappFileName(String webappFileName) {
        this.webappFileName = webappFileName;
    }

    public int getNumberOfActiveSessions() {
        return numberOfActiveSessions;
    }

    public void setNumberOfActiveSessions(int numberOfActiveSessions) {
        this.numberOfActiveSessions = numberOfActiveSessions;
    }

    public SessionsWrapper(List<SessionMetadata> sessions) {
        this.sessions = sessions.toArray(new SessionMetadata[sessions.size()]);
    }

    public SessionsWrapper() {
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public <T> void set(List<T> items) {
        this.sessions = items.toArray(new SessionMetadata[items.size()]);
    }

    public SessionMetadata[] getSessions() {
        return CarbonUtils.arrayCopyOf(sessions);
    }
}
