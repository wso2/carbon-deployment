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

package org.wso2.carbon.as.monitoring.collector.jmx;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class is a base which executes any of its implementation periodically.
 */
public abstract class PeriodicStatCollector implements Runnable {

    private static final long STARTUP_DELAY = 30000;
    private static final long PERIOD = 60000;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private boolean running = false;
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        if (!running) {
            scheduledFuture = executor.scheduleAtFixedRate(this, STARTUP_DELAY, PERIOD, TimeUnit.MILLISECONDS);
            running = true;
        }
    }

    public void stop() {
        scheduledFuture.cancel(false);
        running = false;
    }

}
