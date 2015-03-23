/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;

import java.io.File;
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
        WebappClassloadingContext webappClassloadingContext;
        try {
            webappClassloadingContext = ClassloadingContextBuilder.buildClassloadingContext(getWebappFilePath());
        } catch (Exception e) {
            throw new LifecycleException(e.getMessage(), e);
        }

        //Adding provided classpath entries, if any
        for (String repository : webappClassloadingContext.getProvidedRepositories()) {
            addRepository(repository);
        }

        super.startInternal();

        //Adding the WebappClassloadingContext to the WebappClassloader
        ((CarbonWebappClassLoader) getClassLoader()).setWebappCC(webappClassloadingContext);
    }

    @Override
    protected void stopInternal() throws LifecycleException {

        super.stopInternal();
    }

    //TODO Refactor
    private String getWebappFilePath() throws IOException {
        String webappFilePath = null;
        if (getContainer() instanceof Context) {

            //Value of the following variable depends on various conditions. Sometimes you get just the webapp directory
            //name. Sometime you get absolute path the webapp directory or war file.
            Context ctx = (Context) getContainer();
            String docBase = ctx.getDocBase();

            Host host = (Host) ctx.getParent();
            String appBase = host.getAppBase();
            File canonicalAppBase = new File(appBase);
            if (canonicalAppBase.isAbsolute()) {
                canonicalAppBase = canonicalAppBase.getCanonicalFile();
            } else {
                canonicalAppBase =
                        new File(System.getProperty("carbon.home"), appBase)
                                .getCanonicalFile();
            }

            File webappFile = new File(docBase);
            if (webappFile.isAbsolute()) {
                webappFilePath = webappFile.getCanonicalPath();
            } else {
                webappFilePath = (new File(canonicalAppBase, docBase)).getPath();
            }
        }
        return webappFilePath;
    }

}
