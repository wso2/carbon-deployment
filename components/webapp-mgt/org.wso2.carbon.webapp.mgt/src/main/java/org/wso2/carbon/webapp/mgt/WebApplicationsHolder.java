/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.webapp.mgt;


import org.wso2.carbon.CarbonException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  A holder for deployed & faulty webapps
 */
@SuppressWarnings("unused")
public class WebApplicationsHolder {

    /**
     * The directory containing the webapps
     */
    private File webappsDir;

    /**
     * All successfully deployed webapps
     */
    private Map<String, WebApplication> startedWebapps =
            new ConcurrentHashMap<String, WebApplication>();

    /**
     * All undeployed webapps
     */
    private Map<String, WebApplication> stoppedWebapps =
            new ConcurrentHashMap<String, WebApplication>();

    /**
     * All faulty webapps
     */
    private Map<String, WebApplication> faultyWebapps =
            new ConcurrentHashMap<String, WebApplication>();

    /**
     * default version to context map
     * contextPath / version
     */
    private Map<String, String> appVersionMap =
            new ConcurrentHashMap<String, String>();

    public WebApplicationsHolder(File webappsDir) {
        this.webappsDir = webappsDir;
    }

    public Map<String, WebApplication> getStartedWebapps() {
        return startedWebapps;
    }

    public Map<String, WebApplication> getFaultyWebapps() {
        return faultyWebapps;

    }

    /**
     * Get started and stopped webapps
     * @return Map of started and stopped webapps
     */
    public Map<String, WebApplication> getAllWebapps() {
        HashMap<String, WebApplication> allApps = new HashMap<>();
        allApps.putAll(startedWebapps);
        allApps.putAll(stoppedWebapps);
        return allApps;
    }

    public Map<String, WebApplication> getStoppedWebapps() {
        return stoppedWebapps;
    }

    public void stopWebapp(WebApplication webapp) throws CarbonException {
        boolean stopped = webapp.stop();
        if (stopped) {
            String fileName = webapp.getWebappFile().getName();
            startedWebapps.remove(fileName);
            stoppedWebapps.put(fileName, webapp);
        }
    }

    public void undeployWebapp(WebApplication webapp) throws CarbonException {
        webapp.undeploy();
        String fileName = webapp.getWebappFile().getName();
        startedWebapps.remove(fileName);
        stoppedWebapps.remove(fileName);
        faultyWebapps.remove(fileName);
    }

    public File getWebappsDir() {
        return webappsDir;
    }

    public Map<String, String> getAppVersionMap() {
        return appVersionMap;
    }

/*    public void setAppVersionMap(Map<String, String> appVersionMap) {
        this.appVersionMap = appVersionMap;
    }*/
}

