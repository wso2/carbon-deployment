/*
 * Copyright 2004,2014 The Apache Software Foundation.
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

package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * Catalina Manager MBean client which reads session details etc...
 */
public class ManagerMBeanClient extends MBeanClient {

	private static final String MANAGER_NAME = "Catalina:type=Manager,context=*,host=*";

	private static final String[] MANAGER_ATTRIBUTES = { "activeSessions", "rejectedSessions", "expiredSessions" };

	@Override
	protected String getObjectNameQuery() {
		return MANAGER_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return MANAGER_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		AttributeList list = new AttributeList();
		list.add(new Attribute("host", objectName.getKeyProperty("host")));
		return list;
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		return objectName.getKeyProperty("context");
	}
}
