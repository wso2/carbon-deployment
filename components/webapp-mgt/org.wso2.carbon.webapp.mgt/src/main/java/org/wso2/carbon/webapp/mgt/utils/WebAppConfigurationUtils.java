/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.webapp.mgt.utils;

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;

import java.io.File;
import java.io.IOException;

public class WebAppConfigurationUtils {

    public static String getWebAppFilePath(StandardContext context) throws IOException {
        String docBase = context.getDocBase();
        Host host = (Host) context.getParent();
        String appBase = host.getAppBase();
        File canonicalAppBase = new File(appBase);
        String webAppFilePath;
        if (canonicalAppBase.isAbsolute()) {
            canonicalAppBase = canonicalAppBase.getCanonicalFile();
        } else {
            canonicalAppBase = new File(System.getProperty("carbon.home"), appBase).getCanonicalFile();
        }

        File webAppFile = new File(docBase);

        if (webAppFile.isAbsolute()) {
            webAppFilePath = webAppFile.getCanonicalPath();
        } else {
            webAppFilePath = (new File(canonicalAppBase, docBase)).getPath();
        }
        return webAppFilePath;
    }
}
