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

import java.io.File;

/**
 * A utility class for handling deployment engine related tasks such as check for artifact modification, etc.
 *
 * @since 5.0.0
 */
public class Utils {

    /**
     * Request: file:sample.war .
     * Response: file:/user/wso2carbon-kernel-5.0.0/deployment/webapps/sample.war
     *
     * @param path       file path to resolve
     * @param parentPath parent file path of the file
     * @return file with resolved path
     */
    public static File resolveFileURL(String path, String parentPath) {
        if (path.contains(":") && !path.startsWith("file:")) {
            throw new RuntimeException("URLs other than file URLs are not supported.");
        }
        String relativeFilePath = path;
        if (path.startsWith("file:")) {
            relativeFilePath = path.substring(5);
        }

        File file = new File(relativeFilePath);
        if (!file.isAbsolute()) {
            file = new File(parentPath, relativeFilePath);
            if (!file.isAbsolute()) {
                throw new RuntimeException("Malformed URL : " + path);
            }
        }
        return file;
    }
}
