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

import java.util.ArrayList;
import java.util.List;

public class MonitoringServiceHolder {

    private volatile static MonitoringServiceHolder monitoringServiceHolder;

    private List<MonitoringPublisher> monitoringPublishers = new ArrayList<MonitoringPublisher>();
    private ConfigurationContextService configurationContextService;

    /*
     * private constructor to avoid explicit initializations
     */
    private MonitoringServiceHolder() {

    }

    /**
     * Get the MonitoringPublisher singleton
     *
     * @return
     */
    public static MonitoringServiceHolder getInstance() {
        if (monitoringServiceHolder == null) {
            synchronized (MonitoringServiceHolder.class) {
                if (monitoringServiceHolder == null) {
                    monitoringServiceHolder = new MonitoringServiceHolder();
                }
            }
        }

        return monitoringServiceHolder;
    }

    /**
     * Get the MonitoringPublisher services
     *
     * @return
     */
    public List<MonitoringPublisher> getMonitoringPublishers() {
        return monitoringPublishers;
    }

    /**
     * Add MonitoringPublisher to the holder
     *
     * @param publisher The monitoringPublisher to be added
     */
    public void addMonitoringPublisher(MonitoringPublisher publisher) {
        monitoringPublishers.add(publisher);
    }

    /**
     * Remove MonitoringPublisher from the holder
     *
     * @param publisher The monitoringPublisher to be added
     */
    public void removeMonitoringPublisher(MonitoringPublisher publisher) {
        monitoringPublishers.remove(publisher);
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }
}
