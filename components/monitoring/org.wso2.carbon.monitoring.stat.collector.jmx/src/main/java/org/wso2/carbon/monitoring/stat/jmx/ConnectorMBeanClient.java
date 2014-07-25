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

import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * The Catalina Connector MBean Client
 */
public class ConnectorMBeanClient extends MBeanClient {

	private static final String CONNECTOR_NAME = "Catalina:type=Connector,port=*";
	private static final String[] CONNECTOR_ATTRIBUTES = { "port", "scheme" };

	@Override
	protected String getObjectNameQuery() {
		return CONNECTOR_NAME;
	}

	@Override
	protected String[] getAttributeNames() {
		return CONNECTOR_ATTRIBUTES;
	}

	@Override
	protected AttributeList getPropertiesFromKey(ObjectName objectName) {
		//Nothing to extract
		return new AttributeList();
	}

	@Override
	public String getCorrelationKey(ObjectName objectName) {
		return objectName.getKeyProperty("port");
	}
}