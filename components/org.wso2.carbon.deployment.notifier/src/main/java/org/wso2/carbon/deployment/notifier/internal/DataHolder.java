/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.deployment.notifier.internal;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * Deployment notifier DataHolder.
 *
 * @since 5.0.0
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();

    private CarbonRuntime carbonRuntime;
    private ConfigProvider configProvider;

    public static DataHolder getInstance() {
        return instance;
    }

    /**
     * This method used within this bundle (carbon.core) scope to get the currently held carbonRuntime service instance
     * within this holder.
     *
     * @return this will return the carbonRuntime service instance.
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is called by the relevant service component that acquires the carbonRuntime service
     * instance and will be stored for future look-ups.
     *
     * @param carbonRuntime the carbonRuntime to be stored with this holder
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }

    /**
     * This method is called to set the config provider service.
     *
     * @param configProvider the configProvider to be stored with this holder.
     */
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * This method is used to get the config provider service.
     *
     * @return this will return the configProvider service instance.
     */
    public ConfigProvider getConfigProvider() {
        return configProvider;
    }
}
