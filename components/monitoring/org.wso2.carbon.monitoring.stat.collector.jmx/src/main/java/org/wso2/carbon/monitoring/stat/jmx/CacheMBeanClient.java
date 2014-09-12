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

package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * The Catalina Cache MBean Client
 */
public class CacheMBeanClient extends MBeanClient {

    private static final String CACHE_NAME = "Catalina:type=Cache,host=*,context=*";

    private static final String[] CACHE_ATTRIBUTES = {"accessCount", "cacheSize", "hitsCount"};

    @Override
    protected String getObjectNameQuery() {
        return CACHE_NAME;
    }

    @Override
    protected String[] getAttributeNames() {
        return CACHE_ATTRIBUTES;
    }

    @Override
    protected AttributeList getPropertiesFromKey(ObjectName objectName) {
        return new AttributeList();
    }

    @Override
    public String getCorrelationKey(ObjectName objectName) {
        return objectName.getKeyProperty("context");
    }
}
