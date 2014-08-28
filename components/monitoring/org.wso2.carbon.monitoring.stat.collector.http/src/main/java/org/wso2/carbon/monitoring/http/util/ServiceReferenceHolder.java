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

package org.wso2.carbon.monitoring.http.util;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.monitoring.http.ServiceReferenceHolder"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher" cardinality="1..1" policy="dynamic" bind="setPublisher"
 * unbind="removePublisher"
 */
public class ServiceReferenceHolder {

	private static ConfigurationContextService contextService;

	private static MonitoringPublisher monitoringPublisher;

	public ConfigurationContext getServerConfigContext() {
		return contextService.getServerConfigContext();
	}

	protected void setConfigurationContext(ConfigurationContextService contextService) {
		ServiceReferenceHolder.contextService = contextService;
	}

	protected void unsetConfigurationContext(ConfigurationContextService config) {
		ServiceReferenceHolder.contextService = null;
	}

	protected void setPublisher(MonitoringPublisher monitoringPublisher) {
		ServiceReferenceHolder.monitoringPublisher = monitoringPublisher;
	}

	protected void removePublisher(MonitoringPublisher monitoringPublisher) {
		ServiceReferenceHolder.monitoringPublisher = monitoringPublisher;
	}

	public MonitoringPublisher getMonitoringPublisher(){
		return monitoringPublisher;
	}
}
