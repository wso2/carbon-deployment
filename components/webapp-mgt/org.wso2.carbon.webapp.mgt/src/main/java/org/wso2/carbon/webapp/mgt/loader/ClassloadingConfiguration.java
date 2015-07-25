/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.mgt.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * Global ClassloadingConfiguration. Contains information specified in the webapp-classloading-environments.xml
 * Individual webapps build their own Contexts based on this global configuration.
 */
public class ClassloadingConfiguration {

    private Map<String, CLEnvironment> delegatedEnvironments;
    private Map<String, CLEnvironment> exclusiveEnvironments;

    public ClassloadingConfiguration() {
        delegatedEnvironments = new HashMap<>();
        exclusiveEnvironments = new HashMap<>();
    }

    public void addDelegatedEnvironment(String name, CLEnvironment environment) {
        delegatedEnvironments.put(name, environment);
    }

    public CLEnvironment getDelegatedEnvironment(String name) {
        return delegatedEnvironments.get(name);
    }

    public void addExclusiveEnvironment(String name, CLEnvironment environment) {
        exclusiveEnvironments.put(name, environment);
    }

    public CLEnvironment getExclusiveEnvironment(String name) {
        return exclusiveEnvironments.get(name);
    }
}
