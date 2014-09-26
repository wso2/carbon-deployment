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

package org.wso2.carbon.as.monitoring.collector.jmx.clients;

import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * MBean client for web modules.
 */
public class JspMonitorMBeanClient extends MBeanClient {

    private static final String JSP_MBEAN_NAME = "Catalina:type=JspMonitor,name=jsp,WebModule=*,J2EEApplication=*,J2EEServer=*";

    @Override
    protected String getObjectNameQuery() {
        return JSP_MBEAN_NAME;
    }

    @Override
    protected String[] getAttributeNames() {
        return new String[]{"jspErrorCount", "jspCount", "jspReloadCount"};
    }

    @Override
    protected AttributeList getPropertiesFromKey(ObjectName objectName) {
        return new AttributeList(0);
    }

    @Override
    public String getCorrelationKey(ObjectName objectName) {
        String name = objectName.getKeyProperty("WebModule");

        // the name is like //localhost/STRATOS_ROOT
        // we need to extract the /STRATOS_ROOT
        int lastSlashIndex = name.lastIndexOf('/');
        return name.substring(lastSlashIndex);

    }
}
