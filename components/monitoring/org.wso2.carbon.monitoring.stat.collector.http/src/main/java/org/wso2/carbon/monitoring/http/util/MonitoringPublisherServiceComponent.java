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

package org.wso2.carbon.monitoring.http.util;

import org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * MonitoringPublisherServiceComponent listen to the MonitoringPublishers.
 *
 * @scr.component name="org.wso2.carbon.monitoring.http.MonitoringPublisherServiceComponent"
 * @scr.reference name="org.wso2.carbon.configCtx" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="monitoring.publisher" interface="org.wso2.carbon.monitoring.core.publisher.api.MonitoringPublisher"
 * cardinality="1..n" policy="dynamic" bind="setMonitoringPublisher" unbind="unsetMonitoringPublisher"
 */
public class MonitoringPublisherServiceComponent {

    public void setMonitoringPublisher(MonitoringPublisher publisher) {
        MonitoringServiceHolder.getInstance().addMonitoringPublisher(publisher);
    }

    public void unsetMonitoringPublisher(MonitoringPublisher publisher) {
        MonitoringServiceHolder.getInstance().removeMonitoringPublisher(publisher);
    }

    public void setConfigurationContext(ConfigurationContextService configurationContextService) {
        MonitoringServiceHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    public void unsetConfigurationContext(ConfigurationContextService configurationContextService) {
        MonitoringServiceHolder.getInstance().setConfigurationContextService(null);
    }
}
