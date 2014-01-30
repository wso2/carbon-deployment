/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.webapp.mgt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VersionedWebappMetadata {

    String appVersionRoot;

    //    Map<String, WebappMetadata> versionGroup;
    List<WebappMetadata> versionGroupList;

    VersionedWebappMetadata(String appRootContext) {
        this.appVersionRoot = appRootContext;
        versionGroupList = new ArrayList<WebappMetadata>();
    }

    public WebappMetadata[] getVersionGroups() {
        return versionGroupList.toArray(new WebappMetadata[versionGroupList.size()]);
    }

    public void addWebappVersion(WebappMetadata webappMetadata) {
        this.versionGroupList.add(webappMetadata);
    }

    public String getAppVersionRoot() {
        return appVersionRoot;
    }

    public void sort() {
        //TODO implement sort
        Collections.sort(versionGroupList, new Comparator<WebappMetadata>() {
            public int compare(WebappMetadata arg0, WebappMetadata arg1) {
                return arg0.getContext().compareToIgnoreCase(arg1.getContext());
            }
        });
    }
}
