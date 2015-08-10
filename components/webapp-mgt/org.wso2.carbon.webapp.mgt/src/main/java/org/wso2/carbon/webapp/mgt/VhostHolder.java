/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.mgt;

/**
 * This class is used to read virtual hostnames from webappAdmin service
 */
public class VhostHolder {

    private String[] vhosts;
    private String defaultHostName;

    public String[] getVhosts() {
        return vhosts;
    }

    public void setVhosts(String[] vhosts) {
        this.vhosts = vhosts;
    }

    public String getDefaultHostName() {
        return defaultHostName;
    }

    public void setDefaultHostName(String defaultHostName) {
        this.defaultHostName = defaultHostName;
    }
}
