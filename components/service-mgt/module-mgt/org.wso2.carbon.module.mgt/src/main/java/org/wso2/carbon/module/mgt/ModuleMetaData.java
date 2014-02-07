/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.module.mgt;

import org.apache.axis2.util.Utils;


public class ModuleMetaData {
    private String moduleId;
    private String moduleVersion;
    private String modulename;
    private String description;
    private boolean engagedGlobalLevel;
    private boolean engagedServiceGroupLevel;
    private boolean engagedServiceLevel;
    private boolean engagedOperationLevel;
    private boolean managedModule;

    public ModuleMetaData() {
    }       

    public ModuleMetaData(String name, String moduleVersion) {
        this.modulename = name;
        this.moduleVersion = moduleVersion;
        this.moduleId = Utils.getModuleName(name,moduleVersion);
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getModulename() {
        return modulename;
    }

    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEngagedGlobalLevel() {
        return engagedGlobalLevel;
    }

    public void setEngagedGlobalLevel(boolean engagedGlobalLevel) {
        this.engagedGlobalLevel = engagedGlobalLevel;
    }

    public boolean isEngagedServiceGroupLevel() {
        return engagedServiceGroupLevel;
    }

    public void setEngagedServiceGroupLevel(boolean engagedServiceGroupLevel) {
        this.engagedServiceGroupLevel = engagedServiceGroupLevel;
    }

    public boolean isEngagedServiceLevel() {
        return engagedServiceLevel;
    }

    public void setEngagedServiceLevel(boolean engagedServiceLevel) {
        this.engagedServiceLevel = engagedServiceLevel;
    }

    public boolean isEngagedOperationLevel() {
        return engagedOperationLevel;
    }

    public void setEngagedOperationLevel(boolean engagedOperationLevel) {
        this.engagedOperationLevel = engagedOperationLevel;
    }

    public boolean isManagedModule() {
        return managedModule;
    }

    public void setManagedModule(boolean managedModule) {
        this.managedModule = managedModule;
    }

}
