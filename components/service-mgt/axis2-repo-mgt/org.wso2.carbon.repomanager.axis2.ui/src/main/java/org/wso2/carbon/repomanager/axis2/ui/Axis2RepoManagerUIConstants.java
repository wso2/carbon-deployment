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
package org.wso2.carbon.repomanager.axis2.ui;

/**
 * Axis2 repo manager UI constants are defined here
 */
public class Axis2RepoManagerUIConstants {
    /**
     * The complete path leading to the selected directory
     * eg - /server/axis2services/lib
     */
    public static final String SELECTED_DIR_PATH = "selectedDirPath";

    /**
     * Reference to the selected directory name
     * eg - axis2services
     */
    public static final String DIR_NAME = "dirName";

    /**
     * The complete path to a jar that is to be deleted
     */
    public static final String DELETE_LIB_PATH = "deleteLibPath";

    /**
     * The file extension of the Axis2 artifacts the jar is being used by
     */
    public static final String SERVICE_TYPE = "deleteLibPath";

    /**
     * Reference to verify if a dependency was uploaded successfully to the repository
     */
    public static final String UPLOAD_STATUS = "uploadStatus";
}
