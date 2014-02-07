/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.operation.mgt;

public class OperationMetaData {
    private String name;
    private boolean isControlOperation;
    private String enableMTOM;

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'controlOperation'.
     *
     * @return Value for property 'controlOperation'.
     */
    public boolean isControlOperation() {
        return isControlOperation;
    }

    /**
     * Setter for property 'controlOperation'.
     *
     * @param controlOperation Value to set for property 'controlOperation'.
     */
    public void setControlOperation(boolean controlOperation) {
        isControlOperation = controlOperation;
    }

    /**
     * Configuring enable
     *
     * @return boolean
     */
    public String getEnableMTOM() {
        return enableMTOM;
    }

    /**
     * @param enableMTOM
     */
    public void setEnableMTOM(String enableMTOM) {
        this.enableMTOM = enableMTOM;
    }
}
