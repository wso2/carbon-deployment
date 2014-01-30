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

public class ModuleMgtException extends Exception {

    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    private int level = ERROR;
    private String key = "default.error";

    public ModuleMgtException(Throwable cause, int level, String key) {
        super(cause);
        this.level = level;
        this.key = key;
    }

    public ModuleMgtException(Throwable cause, String key) {
        super(cause);
        this.key = key;
    }

    public ModuleMgtException(int level, String key) {
        this.level = level;
        this.key = key;
    }

    public ModuleMgtException(String key) {
        this.key = key;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
