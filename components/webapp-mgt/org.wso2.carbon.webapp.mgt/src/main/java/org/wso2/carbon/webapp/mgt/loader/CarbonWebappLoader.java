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

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.wso2.carbon.webapp.mgt.utils.WebAppConfigurationUtils;

import java.io.IOException;

/**
 * Customized WebappLoader for Carbon.
 */
public class CarbonWebappLoader extends WebappLoader {

    public CarbonWebappLoader() {
        super();
    }

    public CarbonWebappLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected void startInternal() throws LifecycleException {
        WebAppClassloadingContext webAppClassloadingContext;
        try {
            webAppClassloadingContext = ClassloadingContextBuilder.buildClassloadingContext(getWebappFilePath());
        } catch (Exception e) {
            throw new LifecycleException(e.getMessage(), e);
        }

        //Adding provided classpath entries, if any
        for (String repository : webAppClassloadingContext.getProvidedRepositories()) {
            addRepository(repository);
        }

        super.startInternal();

        //Adding the WebappClassloadingContext to the WebappClassloader
        ((CarbonWebappClassLoader) getClassLoader()).setWebappCC(webAppClassloadingContext);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
    }

    private String getWebappFilePath() throws IOException {
        String webappFilePath = null;
        if (getContainer() instanceof Context) {
            //Value of the following variable depends on various conditions. Sometimes you get just the webapp directory
            //name. Sometime you get absolute path the webapp directory or war file.
            Context ctx = (Context) getContainer();
            if (ctx instanceof StandardContext) {
                webappFilePath = WebAppConfigurationUtils.getWebAppFilePath((StandardContext) ctx);
            }
        }
        return webappFilePath;
    }

}
