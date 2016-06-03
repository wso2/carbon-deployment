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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is for utils methods related to web app configuration.
 */
public class WebAppConfigurationUtils {

    /**
     * This method returns the file path of a web app provided the context object of the web app.
     *
     * @param context StandardContext instance of the webapp
     * @return File path to the web app
     * @throws IOException
     */
    public static String getWebAppFilePath(StandardContext context) throws IOException {
        String docBase = context.getDocBase();
        Host host = (Host) context.getParent();
        String appBase = host.getAppBase();

        Path canonicalAppBase = Paths.get(appBase);
        if (!canonicalAppBase.isAbsolute()) {
            canonicalAppBase = Paths.get(System.getProperty("carbon.home"), appBase);
        }

        Path webAppFilePath = Paths.get(docBase);
        if (!webAppFilePath.isAbsolute()) {
            webAppFilePath = Paths.get(canonicalAppBase.toString(), docBase);
        }
        return webAppFilePath.toString();
    }
}
