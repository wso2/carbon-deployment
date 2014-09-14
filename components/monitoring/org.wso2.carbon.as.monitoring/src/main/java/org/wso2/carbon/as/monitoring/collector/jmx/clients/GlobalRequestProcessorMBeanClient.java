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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * The Catalina Global Request Processor MBean Client
 */
public class GlobalRequestProcessorMBeanClient extends MBeanClient {

    private static final String GLOBAL_CONTEXT_LISTENER_NAME = "Catalina:type=GlobalRequestProcessor,name=*";
    private static final String[] GLOBAL_CONTEXT_LISTENER_ATTRIBUTES = {"bytesSent", "bytesReceived", "errorCount", "processingTime", "requestCount"};

    @Override
    protected String getObjectNameQuery() {
        return GLOBAL_CONTEXT_LISTENER_NAME;
    }

    @Override
    protected String[] getAttributeNames() {
        return GLOBAL_CONTEXT_LISTENER_ATTRIBUTES;
    }

    @Override
    protected AttributeList getPropertiesFromKey(ObjectName objectName) {
        // No attributes required from the ObjectName
        AttributeList list = new AttributeList();
        String name = objectName.getKeyProperty("name");
        if (name != null) {
            name = name.replace("\"", "");
        }
        list.add(new Attribute("connectorName", name));
        return list;
    }

    @Override
    public String getCorrelationKey(ObjectName objectName) {
        String name = objectName.getKeyProperty("name");

        // the name is like http-nio-9443. This name contains 9443 even when the actual running port is 9444.
        // The ThreadPool & Connector Beans show the same behavior.
        // but the connector is just having a key property called "port" with the value 9443
        // So we will rip off the http-nio part and just get the last part as the key.
        // This key is using for correlation purposes only

        if (name != null) {
            String[] parts = name.split("-");
            if (parts.length > 0) {
                return parts[parts.length - 1].replace("\"", "");
            }
        }

        return null;
    }
}
