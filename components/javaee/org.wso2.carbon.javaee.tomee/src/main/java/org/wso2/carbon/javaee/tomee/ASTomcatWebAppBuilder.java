/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.javaee.tomee;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.loader.TomcatHelper;
import org.wso2.carbon.javaee.tomee.ASGlobalListenerSupport;

public class ASTomcatWebAppBuilder extends TomcatWebAppBuilder {

    /**
     * We are over-riding the default tomee @link GlobalListenerSupport listener
     * to add wso2-specific bits such as webapp CREs.
     */
    private final ASGlobalListenerSupport globalListenerSupport;

    public ASTomcatWebAppBuilder() {
        super();
        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new ASGlobalListenerSupport(standardServer, this);

    }

    /**
     * Start operation.
     */
    @Override
    public void start() {
        globalListenerSupport.start();
    }

    /**
     * Start operation.
     */
    @Override
    public void stop() {
        globalListenerSupport.stop();
    }

    public void init(final StandardContext standardContext) {
        //init will only get called if this is a JavaEE webapp.
        // So, we don't have to re-check the CRE
        standardContext.setIgnoreAnnotations(true);
        super.init(standardContext);
    }



}
