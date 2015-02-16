/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will hold API related data
 */
public class APIDataHolder {

    private static APIDataHolder instance = new APIDataHolder();

    private Map<String, Map<String, String>> initialAPIInfoMap;

    public static APIDataHolder getInstance() {
        return instance;
    }

    public Map<String, Map<String, String>> getInitialAPIInfoMap() {
        if (initialAPIInfoMap == null) {
            initialAPIInfoMap = new HashMap<String, Map<String, String>>();
        }
        return initialAPIInfoMap;
    }

    public void setInitialAPIInfoMap(Map<String, Map<String, String>> initialAPIInfoMap) {
        this.initialAPIInfoMap = initialAPIInfoMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIDataHolder that = (APIDataHolder) o;

        if (initialAPIInfoMap != null ? !initialAPIInfoMap.equals(that.initialAPIInfoMap) : that.initialAPIInfoMap != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return initialAPIInfoMap != null ? initialAPIInfoMap.hashCode() : 0;
    }
}
